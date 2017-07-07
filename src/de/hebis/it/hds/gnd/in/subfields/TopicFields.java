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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods for 'topic' subfields
 * <dl>
 * <dt>Referenced definitions:</dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class TopicFields {
   private final static Logger LOG = LogManager.getLogger(TopicFields.class);

   /**
    * Topic term &lt;datafield tag="150"&gt;.<br>
    * see: {@link GenericFields#heading(DataField)}
    * 
    * @param dataField The content of the data field
    */

   public static void headingTopicalTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeUnique("preferred", buildFormatedName(dataField));
   }

   /**
    * Complex See Reference-Subject &lt;datafield tag="260"&gt;.<br>
    * see: {@link GenericFields#complexSeeReference(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void complexSeeReferenceTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("0", "seeAlso", true, "https?://d-nb.info.*"); // dismiss redundant URI
      dataField.storeMultiValued("synonyms", buildFormatedName(dataField));

   }

   /**
    * Alternative terms &lt;datafield tag="450"&gt;.<br>
    * see: {@link GenericFields#tracing(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void tracingTopicalTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeMultiValued("synonyms", buildFormatedName(dataField));
   }

   /**
    * Related terms &lt;datafield tag="550"&gt;.<br>
    * see: {@link GenericFields#related(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void relatedTopicalTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("0", "relatedIds", true, "https?://d-nb.info.*"); // dismiss redundant URI
      dataField.storeMultiValued("related", buildFormatedName(dataField));
      }

   /**
    * Alternative names in other systems &lt;datafield tag="750"&gt;.<br>
    * see: {@link GenericFields#linkingEntry(DataField, String)}
    * 
    * @param dataField The content of the data field
    */
   public static void linkingEntryTopicalTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeMultiValued("synonyms", buildFormatedName(dataField));
      dataField.storeValues("0", "sameAs", true, "http.+"); // no URLs
      }
   
   private static String buildFormatedName(DataField dataField) {
      // name
      String name = dataField.getFirstValue("a");
      if (name == null) {
         LOG.trace(dataField.getRecordId() + ": No $a. in field " + dataField.getFirstValue("tag"));
         for (String refId : dataField.getValues("0")) { // Refers other authority record?
            if (refId.startsWith("(DE-588)")) {
               dataField.replaceUnique("look4me", "true");
               break;
            }
         }
         return null;
      }
      StringBuilder fullName = new StringBuilder(name);
      // context
      List<String> contexts = dataField.get("g");
      if (contexts != null) {
         for (Object context : contexts) {
            fullName.append(" <");
            fullName.append((String) context);
            fullName.append('>');
         }
      }
      return fullName.toString();
   }
}
