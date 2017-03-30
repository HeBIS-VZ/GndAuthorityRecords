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
import java.util.HashMap;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

/**
 * Representation of marcXML datafield with some methods to help processing subfields.
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class DataField extends HashMap<String, List<String>> {
   private static final long   serialVersionUID = 1L;
   private final static Logger LOG              = LogManager.getLogger(DataField.class);
   public String               recordId;
   public SolrInputDocument    solrDoc;

   /**
    * Preconfigure a new DataField
    * 
    * @param recordId The Id of the record
    * @param doc The SolrDocument to write in.
    */
   public DataField(String recordId, SolrInputDocument doc) {
      super();
      this.recordId = recordId;
      solrDoc = doc;
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

}
