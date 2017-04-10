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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

/**
 * Test class for {@link GeneralFields}
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class GeneralFieldsTest {
   /**
    * Check repeated document ids. (data field 035)
    */
   @Test
   public void id() {
      // check regular results
      String recordId = null;
      DataField testDataField = TestHelper.dataFieldFactory(recordId, null, "035", "a", "(DE-101)interalId");
      recordId = GeneralFields.id(testDataField);
      assertEquals("The first id must fit.", "(DE-101)interalId", recordId);
      testDataField = TestHelper.dataFieldFactory(recordId, testDataField, "035", "a", "(DE-588)gndId");
      recordId = GeneralFields.id(testDataField);
      assertEquals("The gndId id must override.", "(DE-588)gndId", recordId);
      testDataField = TestHelper.dataFieldFactory(recordId, testDataField, "035", "a", "(foobar)anyId");
      recordId = GeneralFields.id(testDataField);
      assertEquals("The gndId id must remain.", "(DE-588)gndId", recordId);
      testDataField = TestHelper.dataFieldFactory(recordId, testDataField, "035", "z", "(DE-588_3)oldId"); // ignore
      recordId = GeneralFields.id(testDataField);
      assertEquals("The gndId id must remain.", "(DE-588)gndId", recordId);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("id");
      assertTrue("The Id is mandatory", (result != null));
      assertTrue("The Id has to be unique", (result.size() == 1));
      assertTrue("The gndId id must win.", result.contains("(DE-588)gndId"));
      result = testDataField.getFieldValues("sameAs");
      assertTrue("Alternate ids are expected.", (result != null));
      assertTrue("Two alternate ids are expected.", (result.size() == 2));
      assertFalse("Wrong subfield evaluated.", result.contains("(DE-588_3)oldId"));
   }

   /**
    * Check info's about the type of the authority record. (data field 035)
    */
   @Test
   public void type() {
      DataField testDataField = TestHelper.dataFieldFactory("test", null, "079", "b", "s");
      TestHelper.addSubField(testDataField, "c", "9");
      GeneralFields.type(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("authorityType");
      assertTrue("The type should to be 's'", result.contains("s"));
      result = testDataField.getFieldValues("qualityLevel");
      assertTrue("The qualitiy level should be '9'.", result.contains("9"));
   }

   /**
    * Check ddc and it's quality level (data field 083)
    */
   @Test
   public void dewey() {
      // ddc without qualifier
      DataField testDataField = TestHelper.dataFieldFactory("test", null, "083", "a", "999");
      GeneralFields.dewey(testDataField);
      Collection<Object> result = testDataField.getFieldValues("ddc");
      assertTrue("The ddc should to be '999'", result.contains("999"));
      // ddc with qualifier
      TestHelper.addSubField(testDataField, "9", "d:4");
      GeneralFields.dewey(testDataField);
      result = testDataField.getFieldValues("ddc");
      assertTrue("The qualified ddc should to be '4:999'", result.contains("4:999"));
   }

}
