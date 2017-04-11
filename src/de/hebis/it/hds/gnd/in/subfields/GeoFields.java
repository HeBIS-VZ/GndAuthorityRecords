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
 * Methods for geographical subfields
 * <dl>
 * <dt>Referenced definitions:
 * <dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 31.03.2017 uh initial
 */
public class GeoFields {
   private final static Logger LOG = LogManager.getLogger(GeneralFields.class);

   /**
    * Coordinates. &lt;datafield tag="034"&gt;.<br>
    * A center will computed from the subfields $d, $e, $f, $g and $9A. ans stored as WGS86 (schema:coordinates)<br>
    * Subfield '$0' is taken as additional id. (schema:sameAs)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void coordinates(DataField dataField) {
      boolean calculateMidPoint = true; // assume that min and max values are different
      String minLon = dataField.getFirstValue("d");
      if ((minLon == null) || minLon.isEmpty()) {
         LOG.info(dataField.getRecordId() + ": Subfield $d is missing, skip evaluation");
         return;
      }
      String maxLon = dataField.getFirstValue("e");
      if ((maxLon == null) || maxLon.isEmpty() || maxLon.equals(minLon)) {
         if (LOG.isDebugEnabled()) LOG.debug(dataField.getRecordId() + ": Max value is missed ore equal.");
         calculateMidPoint = false;
      }
      String minLat = dataField.getFirstValue("f");
      if ((minLat == null) || minLat.isEmpty()) {
         LOG.info(dataField.getRecordId() + ": Subfield $f is missing, skip evaluation");
         return;
      }
      String maxLat = dataField.getFirstValue("g");
      char codingSchema = getCoding(dataField);
      if (codingSchema == '?') { // unknown
         LOG.info(dataField.getRecordId() + ": Subfield $9a is missing and format could not detected, skip evaluation");
         return;
      }
      Double longitute = toNormalizedDecimal(minLon, codingSchema);
      Double latitute = toNormalizedDecimal(minLat, codingSchema);
      if (calculateMidPoint) {
         longitute  = (longitute + toNormalizedDecimal(maxLon, codingSchema)) / 2;
         latitute  = (latitute + toNormalizedDecimal(maxLat, codingSchema)) / 2;
      }
      dataField.storeMultiValued("coordinates", latitute.toString() + ' ' + longitute.toString());  
      // optional URI
      String sameAs = dataField.getFirstValue("0");
      if (sameAs != null) dataField.storeMultiValued("sameAs", sameAs);
   }

   private static Double toNormalizedDecimal(String in, char codingSchema) {
      String data = in.replaceAll("[^\\w.]", "");
      if (codingSchema == 'a') { // convert degree minutes and seconds to decimal
         int dataLength = data.length();
         if (dataLength < 5) return null; // to short to convert
         String degree = data.substring(0, dataLength-4);
         int minutes = Integer.valueOf(data.substring(dataLength-4, dataLength-2));
         int seconds = Integer.valueOf(data.substring(dataLength-2, dataLength));
         int decimal = (minutes * 60 + seconds) * 10000 / 36; 
         data = degree + '.' + decimal;
      }
      data = data.replaceFirst("[eEnNoO]0?", "");
      data = data.replaceFirst("[sSwW]0?", "-");
      return Double.parseDouble(data);
   }

   static char getCoding(DataField dataField) {
      String coding = dataField.getPseudoSubField("9", 'A');
      if ((coding != null) && !coding.isEmpty()) return coding.charAt(0);
      // Try to detect the coding
      String minLon = dataField.getFirstValue("d");
      if (LOG.isDebugEnabled()) LOG.debug(dataField.getRecordId() + ": Subfield $9A is missing, try to detect.");
      if (minLon.contains(" ")) { // formatting spaces may an indicate a coding in degrees
         if (minLon.indexOf('.') < 0) return 'a'; // no decimal dot; seems to be coded as degrees
      }
      int pos = minLon.indexOf('.');
      if (pos > 2) { // decimal point as indicator for decimal
         if ((pos + 1 < minLon.length() && (0 > minLon.indexOf('.', pos + 1)))) {
            return 'd'; // only one decimal point, seems to be OK
         }
      }
      return '?';

   }
}
