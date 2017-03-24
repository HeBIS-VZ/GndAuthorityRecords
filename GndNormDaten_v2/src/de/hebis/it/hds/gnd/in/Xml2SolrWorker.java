package de.hebis.it.hds.gnd.in;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

/**
 * Callable to parse/convert a Marc21-XML record into an SolrInputDocument. 
 * @author Reh, Uwe 2017-03-17
 *
 */
public class Xml2SolrWorker implements Callable<SolrInputDocument> {
   private final static Logger          LOG                 = LogManager.getLogger(Xml2SolrWorker.class);
   private final static XMLInputFactory srf                 = XMLInputFactory.newInstance();
   private static final Integer[]       fieldList           = { 35, 79, 83, 150, 550 };
   private static final List<Integer>   dataFieldsToProcess = Arrays.asList(fieldList);
   private List<String>                 recordAsListOfLines;
   private String                       recordId            = null;

   /**
    * Store the rawdata via the constructor
    * @param data The record as a list of strings
    */
   public Xml2SolrWorker(List<String> data) {
      recordAsListOfLines = data;
   }

   /**
    * Start the conversion.
    */
   @Override
   public SolrInputDocument call() throws Exception {
      StringBuilder fullrecord = new StringBuilder();
      for (String line : recordAsListOfLines) {
         fullrecord.append(line.trim());
      }
      return parse(fullrecord.toString());
   }

   /**
    * Parse the XML and store the relevant Data into an SolrInputDocument.
    * @param xmlRecord The marc21-XML record as String
    * @return The new document or NULL if  the xmlRecord was corrupted. 
    */
   private SolrInputDocument parse(String xmlRecord) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("fullrecord", xmlRecord);
      XMLStreamReader rawreader = null;
      HashMap<String, List<String>> dataField = null;
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
                        if (dataFieldsToProcess.contains(Integer.valueOf(tagId))) {
                           if (LOG.isTraceEnabled()) LOG.trace("Found field : " + tagId);
                           dataField = new HashMap<>();
                           dataField.put("tag", newList(tagId));
                           dataField.put("ind1", newList(rawreader.getAttributeValue(null, "ind1")));
                           dataField.put("ind2", newList(rawreader.getAttributeValue(null, "ind2")));
                        } else {
                           if (LOG.isTraceEnabled()) LOG.trace("Skip field : " + tagId);
                        }
                        break;
                     case "subfield":
                        if (dataField == null) break; // Only if the field matters
                        addSubField(dataField, rawreader);
                        break;
                  }
                  break;
               case XMLStreamConstants.END_ELEMENT:
                  if ("datafield".equals(rawreader.getLocalName())) {
                     if (dataField != null) { // // Only if the field matters
                        if (LOG.isTraceEnabled()) LOG.trace("Process field.");
                        evalDataField(dataField, doc);
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
      return doc;
   }

   /**
    * Interpreting single marc-datafields
    * 
    * @param dataField internal representation of the authority record
    * @param doc SolrInputDocument to fill in
    * @return FALSE if the parser has found a Problem, otherwise TRUE
    */
   private boolean evalDataField(HashMap<String, List<String>> dataField, SolrInputDocument doc) {
      List<String> subf = dataField.get("tag");
      if ((subf == null) || subf.isEmpty()) {
         LOG.warn("XML Data error: Category without the attribute 'tag'.");
         return false;
      }
      switch (subf.get(0)) {
         case "035": // The id(s) of the record
            subf = dataField.get("a");
            if ((subf == null) || subf.isEmpty()) {
               LOG.trace("Conversion warning: Datafield 035 has no $a. Skipping");
               break;
            }
            String id = subf.get(0);
            if (id.startsWith("(DE-588)")) { // get rid of minor (not gnd) ids
               if (doc.containsKey("id")) doc.remove("id");
               recordId = id;
            }
            if (recordId == null) recordId = id;
            doc.addField("id", id); // set document id
            doc.addField("sameAs", id); // remember all ids
            break;
         case "079": // type of authority record
            copyValues(dataField, "b", doc, "authorityType", false, null);
            copyValues(dataField, "c", doc, "qualityLevel", false, null);
            break;
         case "083": // DDC
            String ddc = getFirstValue(dataField, "a");
            if (ddc == null) break;
            for (String subf9 : getValues(dataField, "9")) {
               if (subf9.startsWith("d:")) {
                  char determinationKey = subf9.charAt(2);
                  ddc = determinationKey + ':' + ddc;
               }
            }
            doc.addField("ddc", ddc);
            break;
         case "150": // This term/topic
            copyValues(dataField, "a", doc, "preferred", false, null);
            break;
         case "550": // related term
            copyValues(dataField, "0", doc, "relatedIds", true, "http://d-nb.info.*");
            copyValues(dataField, "a", doc, "related", true, null);
            break;
         default:
            throw new RuntimeException("Coding error: No rule to parse marc:" + dataField.get("tag").get(0));
      }
      return true;

   }

   /**
    * Helper to copy value(s) of an subfield to an document field
    * 
    * @param dataField The internal representation of the marc category
    * @param subFieldCode Code of the subfield to select
    * @param doc The SolrDocument to fill
    * @param fieldName Name of the field to fill
    * @param repeatable Should the subfield be repeatable
    * @param regExFilter Optional regex to skip unwanted entries
    */
   private void copyValues(HashMap<String, List<String>> dataField, String subFieldCode, SolrInputDocument doc, String fieldName, boolean repeatable, String regExFilter) {
      List<String> subFieldList = dataField.get(subFieldCode);
      if (subFieldList == null) return;
      if (!repeatable && (subFieldList.size() > 1)) {
         LOG.warn("Conversion warning at " + recordId + ". The Subfield " + dataField.get("tag").get(0) + "$" + subFieldCode + " schouldn't be repeatable.");
         doc.addField(fieldName, subFieldList.get(0)); // use just the first value
         return;
      }
      for (String value : subFieldList) {
         if ((regExFilter != null) && value.matches(regExFilter)) continue;
         doc.addField(fieldName, value);
      }
   }

   /**
    * Helper to extract the first occurrence of the subfield
    * 
    * @param dataField The internal representation of the marc category
    * @param subFieldCode Code of the subfield to select
    * @return The first value of the subfield or NULL, if the subfield isn't provided or empty.
    */
   private String getFirstValue(HashMap<String, List<String>> dataField, String subFieldCode) {
      List<String> subFieldList = dataField.get(subFieldCode);
      if ((subFieldList == null) || subFieldList.isEmpty()) return null;
      return subFieldList.get(0);
   }

   /**
    * Helper to extract the first occurrence of the subfield
    * 
    * @param dataField The internal representation of the marc category
    * @param subFieldCode Code of the subfield to select
    * @return The value list of the (repeated) subfield. If the subfield isn't provided a empty list will returned.
    */
   private List<String> getValues(HashMap<String, List<String>> dataField, String subFieldCode) {
      List<String> subFieldList = dataField.get(subFieldCode);
      if (subFieldList == null) subFieldList = new ArrayList<>();
      return subFieldList;
   }

   /**
    * Pack the XML subfield value(s) into an List (internal representation)
    * 
    * @param dataField The internal representation of the record
    * @param rawreader The stax reader
    */
   private void addSubField(HashMap<String, List<String>> dataField, XMLStreamReader rawreader) {
      String key = rawreader.getAttributeValue(null, "code");
      if (LOG.isTraceEnabled()) LOG.trace("Read subfield : " + key);
      List<String> values = dataField.get(key);
      if (values == null) {
         values = new ArrayList<>();
         dataField.put(key, values);
      }
      String value;
      try {
         value = rawreader.getElementText();
      } catch (XMLStreamException e) {
         LOG.error("Data failure: A marc subfield may not have subtags.");
         throw new RuntimeException(e);
      }
      if (LOG.isTraceEnabled()) LOG.trace("Subfield : " + key + " is \"" + value + "\"");
      values.add(value);
   }

   /**
    * Utility: Pack the given strings into a list.
    * 
    * @param data
    * @return
    */
   private static List<String> newList(String... data) {
      return Arrays.asList(data);
   }

}
