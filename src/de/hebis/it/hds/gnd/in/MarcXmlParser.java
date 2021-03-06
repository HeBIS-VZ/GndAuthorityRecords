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
import java.text.Normalizer;
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
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.common.SolrInputDocument;

import de.hebis.it.hds.gnd.EvalDataFieldException;
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
      parse(Normalizer.normalize(fullrecord.toString(), Normalizer.Form.NFC));
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
      @SuppressWarnings("unused")
      char recordType = 'n'; // New/Normal
      try {
         rawreader = srf.createXMLStreamReader(new ByteArrayInputStream(xmlRecord.getBytes("UTF-8")), "UTF-8");
      }
      catch (Exception e) {
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
                     case "controlfield":
                        if ("001".equals(rawreader.getAttributeValue(null, "tag"))) {
                           recordId = "PPN: " + rawreader.getElementText();
                        }
                        break;
                     case "datafield":
                        String tagId = rawreader.getAttributeValue(null, "tag");
                           if (dataFieldsToIgnore.contains(tagId)) {
                              if (LOG.isTraceEnabled()) LOG.trace("Skip unused field : " + tagId);
                              break;
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
                        try {
                           evalDataField(dataField);
                        }
                        catch (EvalDataFieldException e) {
                           LOG.warn("Conversion error in \"" + recordId + "\": " + e.getMessage());
                        }
                        dataField = null;
                     }
                  }
                  break;
            }
            rawreader.next();
         }
         rawreader.close();
      }
      catch (XMLStreamException e) {
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
      }
      catch (Exception e) {
         LOG.warn("Failed sending document:" + docId + " to " + solrClient.toString(), e);
      }
      if (LOG.isDebugEnabled()) {
         int logStepSize = 10000;
         if (LOG.isTraceEnabled()) logStepSize = 10;
         int counterNow = counter.getAndIncrement();
         if ((counterNow % logStepSize == 0)) LOG.debug("Records processed: " + counterNow);
      }
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
    */
   public void evalDataField(DataField dataField) throws EvalDataFieldException {
      String subFieldId = dataField.getFirstValue("tag");
      if ((subFieldId == null) || subFieldId.isEmpty()) {
         LOG.warn("XML Data error: Category without the attribute 'tag'.");
         return;
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
      return;

   }

   public static void main(String[] args) {
      /* @formatter:off */
      String testmarc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "  <record type=\"Authority\">\n" + 
            "    <leader>00000nz  a2200000oc 4500</leader>\n" + 
            "    <controlfield tag=\"001\">1168028205</controlfield>\n" + 
            "    <controlfield tag=\"003\">DE-101</controlfield>\n" + 
            "    <controlfield tag=\"005\">20180928093751.0</controlfield>\n" + 
            "    <controlfield tag=\"008\">180928n||aznnnbabn           | ana    |c</controlfield>\n" + 
            "    <datafield tag=\"024\" ind1=\"7\" ind2=\" \">\n" + 
            "      <subfield code=\"a\">1168028205</subfield>\n" + 
            "      <subfield code=\"0\">http://d-nb.info/gnd/1168028205</subfield>\n" + 
            "      <subfield code=\"2\">gnd</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"034\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"d\">E 034 38 00</subfield>\n" + 
            "      <subfield code=\"e\">E 034 38 00</subfield>\n" + 
            "      <subfield code=\"f\">N 110 55 00</subfield>\n" + 
            "      <subfield code=\"g\">N 110 55 00</subfield>\n" + 
            "      <subfield code=\"2\">wikiped</subfield>\n" + 
            "      <subfield code=\"9\">A:agx</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"034\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"d\">E106.666666</subfield>\n" + 
            "      <subfield code=\"e\">E106.666666</subfield>\n" + 
            "      <subfield code=\"f\">N010.750000</subfield>\n" + 
            "      <subfield code=\"g\">N010.750000</subfield>\n" + 
            "      <subfield code=\"2\">wikiped</subfield>\n" + 
            "      <subfield code=\"9\">A:dgx</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">(DE-101)1168028205</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"035\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">(DE-588)1168028205</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"040\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">DE-Mar1</subfield>\n" + 
            "      <subfield code=\"c\">DE-Mar1</subfield>\n" + 
            "      <subfield code=\"9\">r:DE-576</subfield>\n" + 
            "      <subfield code=\"b\">ger</subfield>\n" + 
            "      <subfield code=\"d\">1764</subfield>\n" + 
            "      <subfield code=\"e\">rda</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"042\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">gnd3</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"043\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"c\">XB-CN</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"075\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"b\">g</subfield>\n" + 
            "      <subfield code=\"2\">gndgen</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"075\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"b\">gin</subfield>\n" + 
            "      <subfield code=\"2\">gndspec</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"079\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">g</subfield>\n" + 
            "      <subfield code=\"q\">f</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"151\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Hangu Pass</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"451\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Hanguguan</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"451\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">函谷关</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"550\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"0\">(DE-101)041214544</subfield>\n" + 
            "      <subfield code=\"0\">(DE-588)4121454-7</subfield>\n" + 
            "      <subfield code=\"0\">https://d-nb.info/gnd/4121454-7</subfield>\n" + 
            "      <subfield code=\"a\">Pass</subfield>\n" + 
            "      <subfield code=\"g\">Geografie</subfield>\n" + 
            "      <subfield code=\"4\">obin</subfield>\n" + 
            "      <subfield code=\"4\">https://d-nb.info/standards/elementset/gnd#broaderTermInstantial</subfield>\n" + 
            "      <subfield code=\"w\">r</subfield>\n" + 
            "      <subfield code=\"i\">Oberbegriff instantiell</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"670\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Wikipedia</subfield>\n" + 
            "      <subfield code=\"b\">Stand: 28.09.2018</subfield>\n" + 
            "      <subfield code=\"u\">https://en.wikipedia.org/wiki/Hangu_Pass</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"675\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">GeoNames</subfield>\n" + 
            "    </datafield>\n" + 
            "    <datafield tag=\"677\" ind1=\" \" ind2=\" \">\n" + 
            "      <subfield code=\"a\">Pass, welcher die oberen \"Yellow River\" and \"Wei\" Täler von der nordchinesischen Ebene trennt</subfield>\n" + 
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
