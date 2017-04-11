/*
 * Copyright 2016, 2017 by HeBIS (www.hebis.de).
 * 
 * This file is part of HeBIS HdsToolkit.
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
package de.hebis.it.hds.gnd.in.subfields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

/**
 * Representation of marcXML datafield with some methods to help processing subfields.
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class DataField extends HashMap<String, List<String>> {
   private static final long   serialVersionUID = 1L;
   private final static Logger LOG              = LogManager.getLogger(DataField.class);
   private String              recordId;
   private SolrInputDocument   solrDoc;

   /**
    * Preconfigure a new DataField
    * 
    * @param recordId The Id of the record
    * @param doc The SolrDocument to write in.
    */
   public DataField(String recordId, SolrInputDocument doc) {
      super();
      init(recordId, doc);
   }

   /**
    * Reuse a DataField with a new Id
    * 
    * @param newRecordId The Id of the record
    * @param dataField The DataField to reuse.
    */
   public DataField(String newRecordId, DataField dataField) {
      super();
      if (dataField == null) {
         init(newRecordId, null);
      }
      else {
         init(newRecordId, dataField.solrDoc);
      }
   }

   private void init(String newRecordId, SolrInputDocument doc) {
      if (newRecordId != null) recordId = newRecordId;
      solrDoc = (doc == null) ? new SolrInputDocument() : doc;     
   }
   /**
    * Store the value(s) of an (repeated)subfield into the solr document
    * 
    * @param subFieldCode Code of the subfield to select
    * @param fieldName Name of the field to store in (schema:...)
    * @param repeatable Is the subfield be repeatable
    * @param regExFilter Optional regex to skip unwanted entries
    */
   public void storeValues(String subFieldCode, String fieldName, boolean repeatable, String regExFilter) {
      List<String> subFieldList = get(subFieldCode);
      if (subFieldList == null) return;
      if (!repeatable && (subFieldList.size() > 1)) {
         LOG.warn("Conversion warning at " + recordId + ". The Subfield " + get("tag").get(0) + "$" + subFieldCode + " schouldn't be repeatable.");
         solrDoc.addField(fieldName, subFieldList.get(0)); // use just the first value
         return;
      }
      for (String value : subFieldList) {
         if ((regExFilter != null) && value.matches(regExFilter)) {
            if (LOG.isTraceEnabled()) LOG.trace(recordId + ": skip \"" + value + "\".");
            continue;
         }
         if (LOG.isTraceEnabled()) LOG.trace(recordId + ": store \"" + value + "\" to solr field " + fieldName);
         solrDoc.addField(fieldName, value);
      }
   }

   /**
    * Helper to extract the first occurrence of the subfield
    * 
    * @param subFieldCode Code of the subfield to select
    * @return The first value of the subfield or NULL, if the subfield isn't provided or empty.
    */
   public String getFirstValue(String subFieldCode) {
      List<String> subFieldList = get(subFieldCode);
      if ((subFieldList == null) || subFieldList.isEmpty()) return null;
      return subFieldList.get(0);
   }

   /**
    * Helper to extract the first occurrence of the subfield
    * 
    * @param subFieldCode Code of the subfield to select
    * @return The value list of the (repeated) subfield. If the subfield isn't provided a empty list will returned.
    */
   public List<String> getValues(String subFieldCode) {
      List<String> subFieldList = get(subFieldCode);
      if (subFieldList == null) subFieldList = new ArrayList<>();
      return subFieldList;
   }

   /**
    * Pack the XML subfield value(s) into an List (internal representation)
    * 
    * @param rawreader The stax reader
    */
   public void addSubField(XMLStreamReader rawreader) {
      String key = rawreader.getAttributeValue(null, "code");
      if (LOG.isTraceEnabled()) LOG.trace("Read subfield : " + key);
      List<String> values = get(key);
      if (values == null) {
         values = new ArrayList<>();
         put(key, values);
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
    * Get a DNB pseudo subfield.<br>
    * This extension to 'normal' fields is coded by an leading character and colon to the value.
    * 
    * @param subFieldCode Identifier of the extended subfield. (mostly '4' or '9')
    * @param extentionCode Additional identifier.
    * @return The stripped value or NULL if the requested pseudo subfield isn'present.
    */
   public String getPseudoSubField(String subFieldCode, char extentionCode) {
      String startPattern = extentionCode + ":";
      for (String subf9 : getValues(subFieldCode)) {
         if (subf9.startsWith(startPattern)) return subf9.substring(2);
      }
      return null;
   }

   /**
    * Build a list of variable amount of arguments.
    * 
    * @param data One or more strings
    * @return A list of the given strings
    */
   public List<String> newList(String... data) {
      return Arrays.asList(data);
   }

   /**
    * Hide the underlying document and avoid duplicate entries.
    * 
    * @param fieldName Name of the search field as defined in schema.xml
    * @param value The data to add. NULL is ignored
    */
   public void storeMultiValued(String fieldName, String value) {
      if (value == null) return;
      SolrInputField field = solrDoc.getField(fieldName);
      if (field == null) {
         if (LOG.isTraceEnabled()) LOG.trace("First entry in field \"" + fieldName);
         solrDoc.addField(fieldName, value);
         return;
      }
      for (Object entry : field.getValues()) {
         if (value.equals(entry)) return; // skip doublets
      }
      field.addValue(value, 1);
   }

   /**
    * Hide the underlying document and check the 'unique' constraint
    * 
    * @param fieldName Name of the search field as defined in schema.xml
    * @param value The data to add. NULL is ignored
    */
   public void storeUnique(String fieldName, String value) {
      if (value == null) return;
      storeUnique(fieldName, value, false);
   }

   /**
    * Hide the underlying document and check the 'unique' constraint<br>
    * If the field is already used, the new value will be ignored.
    * 
    * @param fieldName Name of the search field as defined in schema.xml
    * @return The values stored in the search field or NULL if the field does not exist.
    */
   public Collection<Object> getFieldValues(String fieldName) {
      return solrDoc.getFieldValues(fieldName);
   }

   /**
    * Hide the underlying document and check the 'unique' constraint<br>
    * If the field is already used, the old entry will be replaced with the new value.
    * 
    * @param fieldName Name of the search field as defined in schema.xml
    * @param value The data to add
    */
   public void replaceUnique(String fieldName, String value) {
      storeUnique(fieldName, value, true);

   }

   /**
    * @return The Id of the current record (stored in this DataField)
    */
   public String getRecordId() {
      return recordId;
   }

   /**
    * Generalize {@link #storeUnique(String, String)} and {@link #replaceUnique(String, String)}
    * 
    * @param fieldName Name of the search field as defined in schema.xml
    * @param value The data to add
    * @param override TRUE: the old value will be replaced. FALSE: the new value will only written if the field was empty.
    */
   private void storeUnique(String fieldName, String value, boolean override) {
      if (solrDoc.containsKey(fieldName)) {
         if (!override) {
            LOG.error(recordId + "# Attempt to store multiple values into unique field: " + fieldName);
            return;
         }
         if (LOG.isDebugEnabled()) LOG.debug(recordId + "# Replace value of unique field: " + fieldName);
         solrDoc.remove("id");
      }
      solrDoc.addField(fieldName, value);
   }
}
