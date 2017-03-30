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
import java.util.function.Function;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import de.hebis.it.hds.gnd.in.subfields.DataField;
import de.hebis.it.hds.gnd.in.subfields.GeneralFields;
import de.hebis.it.hds.gnd.in.subfields.TopicFields;

/**
 * Callable to parse/convert a single Marc21-XML authority record<br>
 * The converted data will be returned as a {@link SolrInputDocument}.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-17 uh initial
 *
 */
public class MarcXmlParser implements Function<List<String>, Boolean> {
   private final static Logger          LOG                 = LogManager.getLogger(MarcXmlParser.class);
   private final static XMLInputFactory srf                 = XMLInputFactory.newInstance();
   private static final Integer[]       fieldList           = { 35, 79, 83, 150, 550 };
   private static final List<Integer>   dataFieldsToProcess = Arrays.asList(fieldList);
   private String                       recordId            = null;
   private SolrClient                   solrClient          = null;

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
      StringBuilder fullrecord = new StringBuilder();
      // concat lines and omit unneeded whitespace characters
      for (String line : recordAsListOfLines) {
         fullrecord.append(line.trim());
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
                     case "datafield":
                        String tagId = rawreader.getAttributeValue(null, "tag");
                        if (dataFieldsToProcess.contains(Integer.valueOf(tagId))) { // only if used
                           if (LOG.isTraceEnabled()) LOG.trace("Found field : " + tagId);
                           dataField = new DataField(recordId, doc);
                           dataField.put("tag", dataField.newList(tagId));
                           dataField.put("ind1", dataField.newList(rawreader.getAttributeValue(null, "ind1")));
                           dataField.put("ind2", dataField.newList(rawreader.getAttributeValue(null, "ind2")));
                        } else {
                           if (LOG.isTraceEnabled()) LOG.trace("Skip field : " + tagId);
                        }
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
      try {
         if (LOG.isDebugEnabled()) LOG.debug("Nwe Document: " + doc.toString());
         solrClient.add(doc);
      } catch (SolrServerException | IOException e) {
         LOG.warn("Failed sending document:" + doc.get("id") + " to " + solrClient.toString(), e);
      }
      if (LOG.isTraceEnabled()) LOG.trace("Record is send.");
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
         case "035": // The id(s) of the record
            recordId = GeneralFields.id(dataField);
            break;
         case "079": // type of authority record
            GeneralFields.type(dataField);
            break;
         case "083": // DDC
            GeneralFields.dewey(dataField);
            break;
         case "150": // This term/topic
            TopicFields.headingTopicalTerm(dataField);
            break;
         case "550": // related term
            TopicFields.tracingTopicalTerm(dataField);
            break;
         default:
            throw new RuntimeException("Coding error: No rule to parse marc:" + dataField.get("tag").get(0));
      }
      return true;

   }

}
