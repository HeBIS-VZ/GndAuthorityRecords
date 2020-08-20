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
    * @return The found id, or the last known id if 035$a is empty
    */
   public static String id(DataField dataField) {
      String testId = dataField.getFirstValue("a");
      if ((testId == null) || testId.isEmpty()) {
         LOG.trace("Conversion warning: Datafield 035 has no $a. Skipping");
      }
      else if (dataField.getRecordId() == null) {
         if (LOG.isTraceEnabled()) LOG.trace(testId + ": as first id found");
         dataField.setRecordId(testId);
      }
      else if (testId.startsWith("(DE-588)")) { // get rid of previous minor (not gnd) ids
         if (LOG.isTraceEnabled()) {
            LOG.trace(testId + ": replaces previous found id: " + dataField.getRecordId());
         }
         dataField.storeMultiValued("sameAs", dataField.getRecordId()); // remember all ids
         dataField.setRecordId(testId);
      }
      else {
         if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": store additional id: " + testId);
         dataField.storeMultiValued("sameAs", testId);
      }
      return dataField.getRecordId(); // return a copy of the primary id
   }

   /**
    * Trust level, my be used to ignore automatic generated records. &lt;datafield tag="042"&gt;.<br>
    * Subfield '$a':
    * <ul>
    * <li>"gnd1" (GND-Datensatz authentifiziert von einem GND-Verbund- oder Fachredaktion)</li>
    * <li>"gnd2" (GND-Datensatz authentifiziert von einer lokalen GND-Redaktion)</li>
    * <li>"gnd3" (GND-Datensatz authentifiziert von geschultem GND-Personal)</li>
    * <li>"gnd4" (GND-Datensatz authentifiziert von ungeschultem GND-Personal)</li>
    * <li>"gnd5" (GND-Datensatz authentifiziert von einem sonstigen GND-Anwender)</li>
    * <li>"gnd6" (GND-Datensatz stammt aus Altbestand und ist maschinell eingespielt worden)</li>
    * <li>"gnd7" (GND-Datensatz ist maschinell aus Metadaten erstellt worden)</li>
    * <li>"gndz" (gesperrter GND -Datensatz, Änderungen sind nicht möglich)</li>
    * <li>evtl. weitere Werte aus der MARC Authentication Action Code List</li>
    * </ul>
    * 
    * @param dataField The content of the data field
    */
   public static void qualityLevel(DataField dataField) {
      dataField.storeValues("a", "qualityLevel", false, null);
   }

   /**
    * Type of the authority record. &lt;datafield tag="075"&gt;.<br>
    * Subfield '$b' ($2="gndgen") :
    * <ul>
    * <li>"p" = Person (individualisiert)</li>
    * <li>"n" = Personenname (nicht individualisiert)</li>
    * <li>"b" = Körperschaft</li>
    * <li>"f" = Kongresse</li>
    * <li>"g" = Geografikum</li>
    * <li>"s" = Sachbegriff</li>
    * <li>"u" = Werk</li>
    * </ul> see https://www.dnb.de/gndgeneraltype<br>
    * more specivic types ($2="gndspec") are omitted
    * 
    * 
    * @param dataField The content of the data field
    */
   public static void type(DataField dataField) {
      String norm = dataField.getFirstValue("2");
      if ((norm == null) || norm.equals("gndgen")) {
       dataField.storeValues("b", "authorityType", false, null);
      }
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
      String subf9 = dataField.getSub9SubField('d');
      if ((subf9 != null) && !subf9.isEmpty()) {
         ddc = subf9.substring(0, 1) + ":" + ddc;
      }
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": Store dewey: " + ddc);
      dataField.storeMultiValued("ddc", ddc);
   }

   /**
    * 
    * @param dataField
    */
   public static void controllInfos(DataField dataField) {
      // TODO Auto-generated method stub

   }

}
