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

import static org.junit.Assert.assertFalse;
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
    * Check geoname (data field 151)
    */
   @Test
   public void headingGeoName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)6560-2", null, "151", "a", "Uschlag");
      GeoFields.headingGeoName(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("preferred");
      assertTrue("The preferred term is mandatory", (result != null));
      assertTrue("The preferred term  has to be unique", (result.size() == 1));
      assertTrue("The preferred term should be 'Uschlag'.", result.contains("Uschlag"));
   }
   
   /**
    * Check alternative geonames (data field 451)
    */
   @Test
   public void tracingGeoName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)7343-X", null, "451", "a", "Imperio do Brazil", "Brasilien");
      GeoFields.tracingGeoName(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Brasilien' should exist", result.contains("Brasilien"));
   }

   /**
    * Check related geonames and related ids (data field 551)
    */
   @Test
   public void relatedGeoName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)6560-2", null, "551", "a", "Staufenberg");
      TestHelper.addSubField(testDataField, "0", "(DE-588)2001843-5", "http://d-nb.info/gnd/2001843-5");
      GeoFields.relatedGeoName(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("related");
      assertTrue("A related term is expected", (result != null));
      assertTrue("Exact one related term is expected", (result.size() == 1));
      assertTrue("The related term should to be 'Staufenberg'", result.contains("Staufenberg"));
      result = testDataField.getFieldValues("relatedIds");
      assertTrue("Related ids are expected", (result != null));
      assertTrue("Exact one related is is expected", (result.size() == 1));
      assertTrue("The related id should to be 'foo'", result.contains("(DE-588)2001843-5"));
   }

   /**
    * Alternative names from other Systems (data field 751)
    */
   @Test
   public void linkingEntrylGeoName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)84416-0", null, "751", "a", "کرمانشاه", "");
      GeoFields.linkingEntryGeoName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'کرمانشاه' should exist", result.contains("کرمانشاه"));

      TestHelper.addSubField(testDataField, "0", "(isil) foo bar", "http://anywhere.edu");
      PersonFields.linkingEntryPersonalName(testDataField);
      result = testDataField.getFieldValues("sameAs");
      assertTrue("The altrnative id '(isil) foo bar' should exist", result.contains("(isil) foo bar"));
      assertFalse("The altrnative URL to the other system should not be storred", result.contains("http://anywhere.edu"));
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
