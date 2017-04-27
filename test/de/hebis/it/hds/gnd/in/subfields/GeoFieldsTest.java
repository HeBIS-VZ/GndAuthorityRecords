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

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

/**
 * Test class for {@link GeoFields}
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class GeoFieldsTest {
   /**
    * Coordinates to be converted (data field 034)
    */
   @Test
   public void convertCoordinates() {
      // degree coded
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "034", "0", "http://sws.geonames.org/3103556");
      TestHelper.addSubField(testDataField, "d", "E 015 59 15");
      TestHelper.addSubField(testDataField, "e", "E015 59 15");
      TestHelper.addSubField(testDataField, "f", "N 0540025");
      TestHelper.addSubField(testDataField, "g", "N 054 00    25");
      TestHelper.addSubField(testDataField, "9", "A:agx");
      GeoFields.coordinates(testDataField);
      Collection<Object> result = testDataField.getFieldValues("coordinates");
      assertTrue("Coordinates (degree) should exist", (result != null));
      assertTrue("Coordinates (degree) should be '54.6944, 15.9875'.", result.contains("54.6944, 15.9875"));
      // reference
      result = testDataField.getFieldValues("sameAs");
      assertTrue("URL to geonames should exist.", (result != null));
      assertTrue("The URL should be 'http://sws.geonames.org/3103556'.", result.contains("http://sws.geonames.org/3103556"));
   }

   /**
    * Decimal coded coordinates with the need to compute the center (data field 034)
    */
   @Test
   public void centeredCoordinates() {
      // decimal coded
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "034", "0", "http://sws.geonames.org/3103556");
      TestHelper.addSubField(testDataField, "d", "E015.987500");
      TestHelper.addSubField(testDataField, "e", "E015.987502");
      TestHelper.addSubField(testDataField, "f", "N054.694400");
      TestHelper.addSubField(testDataField, "g", "N054.694402");
      TestHelper.addSubField(testDataField, "9", "A:dgx");
      GeoFields.coordinates(testDataField);
      Collection<Object> result = testDataField.getFieldValues("coordinates");
      assertTrue("Coordinates (decimal) should exist", (result != null));
      assertTrue("Coordinates (decimal) should be '54.694401, 15.987501'.", result.contains("54.694401, 15.987501"));
      // reference
      result = testDataField.getFieldValues("sameAs");
      assertTrue("URL to geonames should exist.", (result != null));
      assertTrue("The URL should be 'http://sws.geonames.org/3103556'.", result.contains("http://sws.geonames.org/3103556"));
   }

   /**
    * Other representation of the coordinates (data field 034)
    */
   @Test
   public void sameAsCoordinates() {
      // degree coded
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "034", "0", "http://sws.geonames.org/3103556");
      TestHelper.addSubField(testDataField, "d", "E 015 59 15");
      TestHelper.addSubField(testDataField, "e", "E015 59 15");
      TestHelper.addSubField(testDataField, "f", "N 0540025");
      TestHelper.addSubField(testDataField, "g", "N 054 00    25");
      TestHelper.addSubField(testDataField, "9", "A:agx");
      GeoFields.coordinates(testDataField);
      // reference
      Collection<Object> result = testDataField.getFieldValues("sameAs");
      assertTrue("URL to geonames should exist.", (result != null));
      assertTrue("The URL should be 'http://sws.geonames.org/3103556'.", result.contains("http://sws.geonames.org/3103556"));
   }

   /**
    * Detect coding schema
    */
   @Test
   public void getCoding() {
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "034", "9", "A:agx");
      assertTrue("'a' is expected.", ('a' == GeoFields.getCoding(testDataField)));
      testDataField = TestHelper.dataFieldFactory("testid", null, "034", "9", "A:d");
      assertTrue("'d' is expected.", ('d' == GeoFields.getCoding(testDataField)));
      testDataField = TestHelper.dataFieldFactory("testid", null, "034", "d", "N 054 00'25");
      assertTrue("'a' should be detectet.", ('a' == GeoFields.getCoding(testDataField)));
      testDataField = TestHelper.dataFieldFactory("testid", null, "034", "d", "N 054.0025");
      assertTrue("'d' should be detectet.", ('d' == GeoFields.getCoding(testDataField)));
   }
}
