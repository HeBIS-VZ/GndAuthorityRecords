/*
 * Copyright 2016, 2017 by HeBIS (www.hebis.de).
 * 
 * This file is part of HeBIS project Gnd4Index.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the code.  If not, see http://www.gnu.org/licenses/agpl>.
 */
package de.hebis.it.hds.gnd.in;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.common.SolrInputDocument;

import de.hebis.it.hds.gnd.Model;
import de.hebis.it.hds.gnd.in.subfields.CooperationFields;
import de.hebis.it.hds.gnd.in.subfields.DataField;
import de.hebis.it.hds.gnd.in.subfields.GeneralFields;
import de.hebis.it.hds.gnd.in.subfields.GeoFields;
import de.hebis.it.hds.gnd.in.subfields.MeetingFields;
import de.hebis.it.hds.gnd.in.subfields.PersonFields;
import de.hebis.it.hds.gnd.in.subfields.TitleFields;
import de.hebis.it.hds.gnd.in.subfields.TopicFields;

/**
 * Function to parse/convert a single Marc21-XML authority record<br>
 * The converted data will be returned as a {@link SolrInputDocument}.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-17 uh initial
 *
 */
public class MarcXmlParser implements Function<List<String>, Boolean> {
   private final static Logger          LOG                = LogManager.getLogger(MarcXmlParser.class);
   private final static XMLInputFactory srf                = XMLInputFactory.newInstance();
   private static final String[]        unusedfields       = { "001", "003", "005", "008", "024", "040", "043", "065", "079", "089", "336", "339", "372", "375", "377", "380", "382", "383", "384", "548", "667", "670", "672", "675", "677", "678", "679", "680", "682", "692", "885", "912", "913" };
   private static final List<String>    dataFieldsToIgnore = Arrays.asList(unusedfields);
   private final static AtomicInteger   counter            = new AtomicInteger(1);
   private String                       recordId           = null;
   private SolrClient                   solrClient         = null;

   /**
    * Define where to store the parsed records.
    * 
    * @param solrClient A solrj client to the designated index.
    */
   public MarcXmlParser(SolrClient solrClient) {
      this.solrClient = solrClient;
   }

   /**
    * Parse the XML and store the relevant Data into an SolrInputDocument.<br>
    * 
    */
   @Override
   public Boolean apply(List<String> recordAsListOfLines) {
      if (recordAsListOfLines == null) {
         LOG.debug("NULL record received. If this is an OAI update, its normal");
         return true;
      }
      if (recordAsListOfLines.size() < 3) {
         LOG.warn("Unusable record received");
         return false;
      }
      StringBuilder fullrecord = new StringBuilder();
      // concat lines, omit unneeded whitespace characters and replace weird SOS ans EOS characters
      for (String line : recordAsListOfLines) {
         fullrecord.append(line.trim().replace("&#152;", "&lt;").replace("&#156;", "&gt;"));
      }
      parse(fullrecord.toString());
      return true;
   }

   /**
    * Real start of parsing.
    * 
    * @param xmlRecord The marc21-XML record as String
    */
   private void parse(String xmlRecord) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("fullrecord", xmlRecord);
      XMLStreamReader rawreader = null;
      DataField dataField = null;
      recordId = null;
      char recordType = 'n'; // New/Normal
      try {
         rawreader = srf.createXMLStreamReader(new ByteArrayInputStream(xmlRecord.getBytes("UTF-8")), "UTF-8");
      } catch (Exception e) {
         LOG.error("Can't create the stax parser.");
         throw new RuntimeException(e);
      }
      try {
         while (rawreader.hasNext()) {
            switch (rawreader.getEventType()) {
               case XMLStreamConstants.START_ELEMENT:
                  switch (rawreader.getLocalName()) {
                     case "leader":
                        if (LOG.isTraceEnabled()) LOG.trace("Process leader.");
                        recordType = readTypeFromLeader(rawreader);
                        break;
                     case "datafield":
                        String tagId = rawreader.getAttributeValue(null, "tag");
                        if (recordType == 'n') { // Normal authority record
                           if (dataFieldsToIgnore.contains(tagId)) {
                              if (LOG.isTraceEnabled()) LOG.trace("Skip unused field : " + tagId);
                              break;
                           }
                        } else { // Control record, only the '682' data field needs to be evaluated
                           if (!"682".equals(tagId)) {
                              if (LOG.isTraceEnabled()) LOG.trace("Skip unused field in control record : " + tagId);
                              break;
                           }
                        }
                        // only if used
                        if (LOG.isTraceEnabled()) LOG.trace("Found field : " + tagId);
                        dataField = new DataField(recordId, doc);
                        dataField.put("tag", dataField.newList(tagId));
                        dataField.put("ind1", dataField.newList(rawreader.getAttributeValue(null, "ind1")));
                        dataField.put("ind2", dataField.newList(rawreader.getAttributeValue(null, "ind2")));
                        break;
                     case "subfield":
                        if (dataField == null) break; // Only if the field matters
                        dataField.addSubField(rawreader);
                        break;
                  }
                  break;
               case XMLStreamConstants.END_ELEMENT:
                  if ("datafield".equals(rawreader.getLocalName())) {
                     if (dataField != null) { // // Only if the field matters
                        if (LOG.isTraceEnabled()) LOG.trace("Process field.");
                        eval(dataField);
                        dataField = null;
                     }
                  }
                  break;
            }
            rawreader.next();
         }
         rawreader.close();
      } catch (XMLStreamException e) {
         throw new RuntimeException("Data error in XML file.", e);
      }
      if (LOG.isTraceEnabled()) LOG.trace("Index record");
      addToIndex(doc, xmlRecord);
   }

   /**
    * @param doc
    */
   private boolean addToIndex(SolrInputDocument doc, String marcXml) {
      if (LOG.isTraceEnabled()) LOG.trace("New Document: " + doc.toString());
      String docId = (String) doc.getFieldValue("id");
      if (docId == null) {
         LOG.warn("No Id found. ID:" + docId);
         return false;
      }
      if (doc.getFieldValue("preferred") == null) {
         LOG.warn(docId + ": No preferred naming found in marcXml. ID:" + docId);
         return false;
      }
      if (LOG.isDebugEnabled()) {
         if (doc.getFieldValues("coordinates") != null) {
            for (Object coordinate : doc.getFieldValues("coordinates")) {
               LOG.debug(docId + ": Coordinates found [" + (String) coordinate + "].");
            }
         }
      }
      try {
         solrClient.add(doc);
      } catch (SolrServerException | IOException e) {
         LOG.warn("Failed sending document:" + docId + " to " + solrClient.toString(), e);
      }
      if (LOG.isTraceEnabled()) LOG.trace("Record is send.");
      int counterNow = counter.getAndIncrement();
      if (LOG.isTraceEnabled() && (counterNow % 10000 == 0)) LOG.info("Records processed: " + counterNow);
      return true;
   }

   /**
    * 
    * @param rawreader
    * @return
    */
   private char readTypeFromLeader(XMLStreamReader rawreader) {
      // TODO Auto-generated method stub
      // TODO Dummy for upcoming functions for deletions and redirections.
      return 'n';
   }

   /**
    * Interpret a single marcXML 'datafield'
    * 
    * @param dataField internal representation of the authority record
    * @return FALSE if the parser has found a Problem, otherwise TRUE
    */
   public boolean eval(DataField dataField) {
      String subFieldId = dataField.getFirstValue("tag");
      if ((subFieldId == null) || subFieldId.isEmpty()) {
         LOG.warn("XML Data error: Category without the attribute 'tag'.");
         return false;
      }
      switch (subFieldId) {
         case "034": // geographic coordinates
            GeoFields.coordinates(dataField);
            break;
         case "035": // The id(s) of the record
            recordId = GeneralFields.id(dataField);
            break;
         case "042": // Trust level of authority record
            GeneralFields.qualityLevel(dataField);
            break;
         case "075": // type of authority record
            GeneralFields.type(dataField);
            break;
         case "083": // DDC
            GeneralFields.dewey(dataField);
            break;
         case "100": // Personal name
            PersonFields.headingPersonalName(dataField);
            break;
         case "110": // Cooperation name
            CooperationFields.headingCooperationName(dataField);
            break;
         case "111": // Meeting name
            MeetingFields.headingMeetingName(dataField);
            break;
         case "130": // Title name
            TitleFields.headingTitle(dataField);
            break;
         case "150": // This term/topic
            TopicFields.headingTopicalTerm(dataField);
            break;
         case "151": // This term/topic
            GeoFields.headingGeoName(dataField);
            break;
         case "260": // This complex term/topic
            TopicFields.complexSeeReferenceTerm(dataField);
            break;
         case "400": // Alternative name
         case "500": // Related personal name
            PersonFields.tracingPersonalName(dataField);
            break;
         case "410": // Alternative cooperation name
            CooperationFields.tracingCooperationName(dataField);
            break;
         case "411": // Alternative meeting name
            MeetingFields.tracingMeetingName(dataField);
            break;
         case "430": // Alternative title
            TitleFields.tracingTitle(dataField);
            break;
         case "450": // Alternative term/topic
            TopicFields.tracingTopicalTerm(dataField);
            break;
         case "451": // Alternative geoname
            GeoFields.tracingGeoName(dataField);
            break;
         case "510": // Related cooperation
            CooperationFields.relatedCooperationName(dataField);
            break;
         case "511": // Related meeting
            MeetingFields.relatedMeetingName(dataField);
            break;
         case "530": // Related uniform title
            TitleFields.relatedTitle(dataField);
            break;
         case "550": // Related term
            TopicFields.relatedTopicalTerm(dataField);
            break;
         case "551": // Related geographic name
            GeoFields.relatedGeoName(dataField);
            break;
         case "682": // Infos for control records TODO
            GeneralFields.controllInfos(dataField);
            break;
         case "700": // Alternative name for person in other system
            PersonFields.linkingEntryPersonalName(dataField);
            break;
         case "710": // Alternative cooperation name in other system
            CooperationFields.linkingEntryCooperationName(dataField);
            break;
         case "711": // Alternative meeting name in other system
            MeetingFields.linkingEntryMeetingName(dataField);
            break;
         case "730": // Alternative title in other system
            TitleFields.linkingEntryTitle(dataField);
            break;
         case "750": // Alternative name for topic in other system
            TopicFields.linkingEntryTopicalTerm(dataField);
            break;
         case "751": // Alternative name for topic in other system
            GeoFields.linkingEntryGeoName(dataField);
            break;
         default:
            LOG.warn("No Rule for " + recordId + " : " + subFieldId);
      }
      return true;

   }

   public static void main(String[] args) {
      /* @formatter:off */
      String testmarc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "  <record xmlns=\"http://www.loc.gov/MARC21/slim\" type=\"Authority\">\n" + 
            "    <leader>00000nz  a2200000n  4500</leader>\n" + 
            "    <controlfield tag=\"001\">98018116X</controlfield>\n" + 
            "    <controlfield tag=\"003\">DE-101</controlfield>\n" + 
            "    <controlfield tag=\"005\">20170601133703.0</controlfield>\n" + 
            "    <controlfield tag=\"008\">060619n||azznnaabn           | ana    |c</controlfield>\n" + 
            "    <datafield tag=\"024\" ind1=\"7\" ind2=\" \">\n" + 
            "      <subfield code=\"a\">http://d-nb.info/gnd/7531248-7</subfield>\n" + 
            "      <subfield code=\"2\">uri</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"034\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"d\">E 000 00 00</subfield>\n" + 
            "      <subfield code=\"e\">E 000 00 00</subfield>\n" + 
            "      <subfield code=\"f\">N 049 19 21</subfield>\n" + 
            "      <subfield code=\"g\">N 049 19 21</subfield>\n" + 
            "      <subfield code=\"2\">geonames</subfield>\n" + 
            "      <subfield code=\"0\">http://sws.geonames.org/2968325</subfield>\n" + 
            "      <subfield code=\"9\">A:agx</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"034\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"d\">E000.000000</subfield>\n" + 
            "      <subfield code=\"e\">E000.000000</subfield>\n" + 
            "      <subfield code=\"f\">N049.322500</subfield>\n" + 
            "      <subfield code=\"g\">N049.322500</subfield>\n" + 
            "      <subfield code=\"2\">geonames</subfield>\n" + 
            "      <subfield code=\"0\">http://sws.geonames.org/2968325</subfield>\n" + 
            "      <subfield code=\"9\">A:dgx</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">(DE-101)98018116X</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">(DE-588)7531248-7</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"z\">(DE-588c)7531248-7</subfield>\n" + 
            "      <subfield code=\"9\">v:zg</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"040\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">DE-255</subfield>\n" + 
            "      <subfield code=\"9\">r:DE-255</subfield>\n" + 
            "      <subfield code=\"b\">ger</subfield>\n" + 
            "      <subfield code=\"d\">1165</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"043\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"c\">XA-FR</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"079\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">g</subfield>\n" + 
            "      <subfield code=\"b\">g</subfield>\n" + 
            "      <subfield code=\"c\">1</subfield>\n" + 
            "      <subfield code=\"q\">s</subfield>\n" + 
            "      <subfield code=\"v\">gik</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"083\" ind1=\"0\" ind2=\"4\">\n" + 
            "      <subfield code=\"z\">2</subfield>\n" + 
            "      <subfield code=\"a\">4422</subfield>\n" + 
            "      <subfield code=\"9\">t:2009-12-16</subfield>\n" + 
            "      <subfield code=\"2\">22/ger</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"151\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Villers-sur-Mer</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"667\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">off(DE-101)*dezimale Koordinaten am 1.6.2017 korrigiert. Alte, falsche, Umrechnung war: E000.7E4000 - E000.7E4000 / N049.322639 - N049.322639</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"670\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Dict. nat. communes</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"679\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Ort im Dép. Calvados</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"913\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"S\">swd</subfield>\n" + 
            "      <subfield code=\"i\">g</subfield>\n" + 
            "      <subfield code=\"a\">Villers-sur-Mer</subfield>\n" + 
            "      <subfield code=\"0\">(DE-588c)7531248-7</subfield>\n" + 
            "    </datafield>\n" + 
            "  </record>\n" + 
            ""; 
      // String testmarc = "<record type=\"Authority\"><leader>00000nz  a2200000o  4500</leader><controlfield tag=\"001\">159615674</controlfield><controlfield tag=\"003\">DE-101</controlfield><controlfield tag=\"005\">20110324153550.0</controlfield><controlfield tag=\"008\">110324n||aznnnabbn           | aba    |c</controlfield><datafield tag=\"024\" ind1=\"7\" ind2=\" \"><subfield code=\"a\">http://d-nb.info/gnd/159615674</subfield><subfield code=\"2\">uri</subfield></datafield><datafield tag=\"035\" ind1=\" \" ind2=\" \"><subfield code=\"a\">(DE-101)159615674</subfield></datafield><datafield tag=\"035\" ind1=\" \" ind2=\" \"><subfield code=\"a\">(DE-588)159615674</subfield></datafield><datafield tag=\"035\" ind1=\" \" ind2=\" \"><subfield code=\"z\">(DE-588a)159615674</subfield><subfield code=\"9\">v:zg</subfield></datafield><datafield tag=\"040\" ind1=\" \" ind2=\" \"><subfield code=\"a\">DE-12</subfield><subfield code=\"9\">r:DE-12</subfield><subfield code=\"b\">ger</subfield><subfield code=\"d\">9010</subfield></datafield><datafield tag=\"079\" ind1=\" \" ind2=\" \"><subfield code=\"a\">g</subfield><subfield code=\"b\">n</subfield><subfield code=\"c\">6</subfield><subfield code=\"q\">f</subfield></datafield><datafield tag=\"100\" ind1=\"0\" ind2=\" \"><subfield code=\"a\">100% Orange</subfield></datafield><datafield tag=\"400\" ind1=\"0\" ind2=\" \"><subfield code=\"a\">Hundred per cent Orange</subfield></datafield><datafield tag=\"400\" ind1=\"0\" ind2=\" \"><subfield code=\"a\">Hundert Prozent Orange</subfield></datafield><datafield tag=\"400\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Oikawa, Kenji</subfield><subfield code=\"9\">4:nawi</subfield><subfield code=\"w\">r</subfield><subfield code=\"i\">Wirklicher Name</subfield><subfield code=\"e\">Wirklicher Name</subfield></datafield><datafield tag=\"400\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Takeuchi, Mayuko</subfield><subfield code=\"9\">4:nawi</subfield><subfield code=\"w\">r</subfield><subfield code=\"i\">Wirklicher Name</subfield><subfield code=\"e\">Wirklicher Name</subfield></datafield><datafield tag=\"913\" ind1=\" \" ind2=\" \"><subfield code=\"S\">pnd</subfield><subfield code=\"i\">5</subfield><subfield code=\"a\">100% Orange</subfield><subfield code=\"0\">(DE-588a)159615674</subfield></datafield></record>";
      // String testmarc =  "<record type=\"Authority\"><leader>00000nz  a2200000o  4500</leader><controlfield tag=\"001\">158098269</controlfield><controlfield tag=\"003\">DE-101</controlfield><controlfield tag=\"005\">20110324125821.0</controlfield><controlfield tag=\"008\">110324n||aznnnabbn           | aba    |c</controlfield><datafield tag=\"024\" ind1=\"7\" ind2=\" \"><subfield code=\"a\">http://d-nb.info/gnd/158098269</subfield><subfield code=\"2\">uri</subfield></datafield><datafield tag=\"035\" ind1=\" \" ind2=\" \"><subfield code=\"a\">(DE-101)158098269</subfield></datafield><datafield tag=\"035\" ind1=\" \" ind2=\" \"><subfield code=\"a\">(DE-588)158098269</subfield></datafield><datafield tag=\"035\" ind1=\" \" ind2=\" \"><subfield code=\"z\">(DE-588a)158098269</subfield><subfield code=\"9\">v:zg</subfield></datafield><datafield tag=\"040\" ind1=\" \" ind2=\" \"><subfield code=\"a\">DE-12</subfield><subfield code=\"9\">r:DE-12</subfield><subfield code=\"b\">ger</subfield><subfield code=\"d\">9010</subfield></datafield><datafield tag=\"079\" ind1=\" \" ind2=\" \"><subfield code=\"a\">g</subfield><subfield code=\"b\">n</subfield><subfield code=\"c\">6</subfield><subfield code=\"q\">f</subfield></datafield><datafield tag=\"100\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Allen, M. E.</subfield></datafield><datafield tag=\"400\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Allen, Max [Wirkl. Name]</subfield><subfield code=\"9\">4:nawi</subfield><subfield code=\"w\">r</subfield><subfield code=\"i\">Wirklicher Name</subfield><subfield code=\"e\">Wirklicher Name</subfield></datafield><datafield tag=\"400\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Allen, Eleanor [Wirkl. Name]</subfield><subfield code=\"9\">4:nawi</subfield><subfield code=\"w\">r</subfield><subfield code=\"i\">Wirklicher Name</subfield><subfield code=\"e\">Wirklicher Name</subfield></datafield><datafield tag=\"913\" ind1=\" \" ind2=\" \"><subfield code=\"S\">pnd</subfield><subfield code=\"i\">a</subfield><subfield code=\"a\">Allen, M. E.</subfield><subfield code=\"0\">(DE-588a)158098269</subfield></datafield></record>";
      /* @formatter:on   */
      MarcXmlParser me = new MarcXmlParser(getDefaultClient()); 
      me.parse(testmarc);
   }

   private static SolrClient getDefaultClient() {
      String baseSolrURL = Model.getModel().getProperty("BaseURL");
      return new ConcurrentUpdateSolrClient.Builder(baseSolrURL).withQueueSize(100).withThreadCount(100).build();
   }
}
