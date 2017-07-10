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

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

/**
 * Test class for {@link PersonFields}
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 29.04.2017 uh initial
 */
public class CooperationFieldsTest {

   /**
    * cooperation name (data field 110)
    */
   @Test
   public void headingCooperationName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)10000074-5", null, "110", "a", "USA");
      TestHelper.addSubField(testDataField, "b", "Interagency Agricultural Projections Committee", "FOO");
      CooperationFields.headingCooperationName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("preferred");
      assertTrue("The preferred term is mandatory", (result != null));
      assertTrue("The preferred term  has to be unique", (result.size() == 1));
      assertTrue("The preferred term should be 'USA. Interagency Agricultural Projections Committee. FOO'.", result.contains("USA. Interagency Agricultural Projections Committee. FOO"));   }
   
   /**
    * Check alternative names (data field 410)
    */
   @Test
   public void tracingCooperationName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)10000074-5", null, "410", "a", "USA");
      TestHelper.addSubField(testDataField, "b", "Department of Agriculture", "Interagency Agricultural Projections Committee");
      TestHelper.addSubField(testDataField, "g", "USA");
      testDataField.storeUnique("preferred", "exists"); // Mock for marc field 110
      CooperationFields.tracingCooperationName(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Cornell Univ' should exist", result.contains("USA. Department of Agriculture. Interagency Agricultural Projections Committee (USA)"));
   }

   /**
    * Check related terms and related ids (data field 510)
    */
   @Test
   public void relatedCooperationlName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)191-0", null, "510", "a", "Consejo Superior de Investigaciones Científicas");
      TestHelper.addSubField(testDataField, "0", "(DE-588)36416-2", "http://d-nb.info/gnd/36416-2");
      TestHelper.addSubField(testDataField, "g", "TEST", "Test");
      CooperationFields.relatedCooperationName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("related");
      assertTrue("A related Name is expected", (result != null));
      assertTrue("A related term should to be 'Consejo Superior de Investigaciones Científicas'", result.contains("Consejo Superior de Investigaciones Científicas (TEST) (Test)"));
      result = testDataField.getFieldValues("relatedIds");
      assertTrue("Related ids are expected", (result != null));
      assertTrue("One related id should to be '(DE-588)36416-2'", result.contains("(DE-588)36416-2"));
   }
   
   /**
    * Alternative names from other Systems (data field 710)
    */
   @Test
   public void linkingEntryCooperationName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)333-5", null, "710", "a", "联合国");
      TestHelper.addSubField(testDataField, "0", "(isil) foo bar", "http://anywhere.edu");
      CooperationFields.linkingEntryCooperationName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym '联合国' should exist", result.contains("联合国"));
      result = testDataField.getFieldValues("sameAs");
      assertTrue("The alternative id '(isil) foo bar' should exist", result.contains("(isil) foo bar"));
      assertFalse("The alternative URL to the other system should not be storred", result.contains("http://anywhere.edu"));
   }

}
