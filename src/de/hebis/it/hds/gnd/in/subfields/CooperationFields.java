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
 * Methods for 'cooperation' subfields
 * <dl>
 * <dt>Referenced definitions:</dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 28.04.2017 uh initial
 */
public class CooperationFields {
   private final static Logger LOG = LogManager.getLogger(CooperationFields.class);

   /**
    * Name of the cooperation &lt;datafield tag="110"&gt;.<br>
    * see: {@link GenericFields#heading(DataField)}
    * 
    * @param dataField The content of the data field
    */

   public static void headingCooperationName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeUnique("preferred", buildFormatedName(dataField));
   }

   /**
    * Alternative names &lt;datafield tag="410"&gt;.<br>
    * see: {@link GenericFields#tracing(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void tracingCooperationName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeMultiValued("synonyms", buildFormatedName(dataField));
  }

   /**
    * Related names &lt;datafield tag="510"&gt;.<br>
    * see: {@link GenericFields#related(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void relatedCooperationName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("0", "relatedIds", true, "https?://d-nb.info.*"); // dismiss redundant URI
      dataField.storeMultiValued("related", buildFormatedName(dataField));
   }

   /**
    * Alternative names in other systems &lt;datafield tag="710"&gt;.<br>
    * see: {@link GenericFields#linkingEntry(DataField, String)}
    * 
    * @param dataField The content of the data field
    */
   public static void linkingEntryCooperationName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      GenericFields.linkingEntry(dataField, null);
      dataField.storeMultiValued("synonyms", buildFormatedName(dataField));
      dataField.storeValues("0", "sameAs", true, "http.+"); // no URLs
   }

   private static String buildFormatedName(DataField dataField) {
      // name
      String name = dataField.getFirstValue("a");
      if (name == null) {
         LOG.info(dataField.getRecordId() + ": No $a. in field " + dataField.getFirstValue("tag"));
         name = "";
      }
      StringBuilder fullName = new StringBuilder(name);
      List<String> titles = dataField.get("b");
      if (titles != null) {
         for (Object title : titles) {
            fullName.append(". ");
            fullName.append((String) title);
         }
      }
      List<String> addenums = dataField.get("g");
      if (addenums != null) {
         for (Object addenum : addenums) {
            fullName.append(" (");
            fullName.append((String) addenum);
            fullName.append(")");
         }
      }
      return (fullName.length() == 0) ? null : fullName.toString();
   }
}
