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
 * Methods for persons
 * <dl>
 * <dt>Referenced definitions:</dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 04.04.2017 uh initial
 */
public class PersonFields {
   private final static Logger LOG = LogManager.getLogger(PersonFields.class);

   /**
    * Personal name &lt;datafield tag="100"&gt;.<br>
    * Subfields will be stored in the form "$a $b &lt;$c&gt;. (schema:prefered)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void personalName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      StringBuilder fullName = buildFormatedName(dataField);
      if ((fullName != null) && (fullName.length() > 0)) {
         dataField.storeUnique("preferred", fullName.toString());
      }
   }

   /**
    * Alternative names &lt;datafield tag="400"&gt;.<br>
    * Subfields will be stored in the form "$a $b &lt;$c&gt;. (schema:synonyms)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void tracingPersonalName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      StringBuilder fullName = buildFormatedName(dataField);
      if ((fullName != null) && (fullName.length() > 0)) {
         dataField.storeMultiValued("synonyms", fullName.toString());
      }
      // is a 2nd pass required?
      if ("navi".equals(dataField.getSub9SubField('4'))) {
         if (LOG.isDebugEnabled()) LOG.debug(dataField.getRecordId() + ": Real name in synonyms found.");
         dataField.replaceUnique("look4me", "true");
      }
   }

   /**
    * Related personal names &lt;datafield tag="500"&gt;.<br>
    * Subfield '$0' into (schema:relatedIds)<br>
    * Subfield '$a' into (schema:related)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void relatedPersonalName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("0", "relatedIds", true, "https?://d-nb.info.*"); // dismiss redundant URI
      dataField.storeValues("a", "related", true, null);
   }

   /**
    * Alternative names in other systems &lt;datafield tag="700"&gt;.<br>
    * Subfield '$a' is taken as alias. (schema:synonyms)<br>
    * Optional trailing informations "ABC%DE3..." will be removed. Result: "ABC"
    * 
    * @param dataField The content of the data field
    */
   public static void linkingEntryPersonalName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      String altName = dataField.getFirstValue("a");
      if (altName != null) dataField.storeMultiValued("synonyms", altName.replaceAll("%DE.*", ""));
      dataField.storeValues("0", "sameAs", true, "http.+"); // no URLs

   }

   private static StringBuilder buildFormatedName(DataField dataField) {
      // name
      String name = dataField.getFirstValue("a");
      if (name == null) {
         LOG.warn(dataField.getRecordId() + ": Field 100 without $a.");
         return null;
      }
      StringBuilder fullName = new StringBuilder();
      fullName.append(name);
      // nummeration
      String numeration = dataField.getFirstValue("b");
      if (numeration != null) {
         fullName.append(' ');
         fullName.append(numeration);
      }
      // title(s)
      List<String> titles = dataField.get("c");
      if (titles != null) {
         for (Object title : titles) {
            fullName.append(" <");
            fullName.append((String) title);
            fullName.append('>');
         }
      }
      return fullName;
   }

}
