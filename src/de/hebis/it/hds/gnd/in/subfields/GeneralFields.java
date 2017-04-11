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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods for general subfields
 * <dl>
 * <dt>Referenced definitions:</dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class GeneralFields {
   private final static Logger LOG = LogManager.getLogger(GeneralFields.class);

   /**
    * Id(s) &lt;datafield tag="035"&gt;.<br>
    * The first id found in '$a' and '$0' which starts with "(DE-588)" will be used as primary id. (schema:id)<br>
    * Other ids are stored as reference. (schema:sameAs)
    * 
    * @param dataField The content of the data field
    * @return The found id or NULL if 035$a is empty
    */
   public static String id(DataField dataField) {
      String testId = dataField.getFirstValue("a");
      if ((testId == null) || testId.isEmpty()) {
         LOG.trace("Conversion warning: Datafield 035 has no $a. Skipping");
         return dataField.getRecordId();
      }
      if (dataField.getRecordId() == null) { 
         if (LOG.isTraceEnabled()) LOG.trace(testId + ": as first id found");
         dataField = new DataField(testId, dataField);
         dataField.storeUnique("id", testId); // set document id
      }
      else if (testId.startsWith("(DE-588)")) { // get rid of previous minor (not gnd) ids
         if (LOG.isTraceEnabled()) {
            LOG.trace(testId + ": replaces previous found id: " + dataField.getRecordId());
            LOG.trace(testId + ": store additional id: " + dataField.getRecordId());
         }
         dataField.storeMultiValued("sameAs", dataField.getRecordId()); // remember all ids
         dataField.replaceUnique("id", testId);
         dataField = new DataField(testId, dataField);
      }
      else {
         if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": store additional id: " + testId);
         dataField.storeMultiValued("sameAs", testId); 
      }   
      return dataField.getRecordId(); // return a copy of the primary id
   }

   /**
    * Type of the record. &lt;datafield tag="079"&gt;.<br>
    * Subfield '$b' is taken as type. (schema:authorityType)<br>
    * Subfield '$c' is taken as level of trust. (schema:qualityLevel)<br>
    * ('1' is the best, over '3' should only used on own risk)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void type(DataField dataField) {
      dataField.storeValues("b", "authorityType", false, null);
      dataField.storeValues("c", "qualityLevel", false, null);
   }

   /**
    * Dewey Decimal Classification (DDC) &lt;datafield tag="083"&gt;.<br>
    * Since the mapping to DDC may not be perfect the DNB introduced the subfield "$9d".<br>
    * The level of similarity is coded with a cipher [1..4], where '4' is "perfect match" and '1' is a "could be".<br>
    * 
    * Subfield '$a' is taken as DDC. (schema:ddc)<br>
    * If subfield '$9d' exist the level will stored with the DDC in the form "level:ddc" (e.g. "2:123.45")<br>
    * 
    * @param dataField The content of the data field
    */
   public static void dewey(DataField dataField) {
      String ddc = dataField.getFirstValue("a");
      if (ddc == null) return;
      String subf9 = dataField.getPseudoSubField("9", 'd');
      if ((subf9 != null) && !subf9.isEmpty()) {
         ddc = subf9.substring(0, 1) + ":" + ddc;
      }
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": Store dewey: " + ddc);
      dataField.storeMultiValued("ddc", ddc);
   }

}
