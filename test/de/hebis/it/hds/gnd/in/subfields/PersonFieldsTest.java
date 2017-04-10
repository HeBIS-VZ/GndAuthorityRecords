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
 * Test class for {@link PersonFields}
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class PersonFieldsTest {

   /**
    * Alternative names from other Systems (data field 700)
    */
   @Test
   public void linkingEntryPersonalName() {
      DataField testDataField = TestHelper.dataFieldFactory("simpleCase", null, "700", "a", "Jon Doh");
      PersonFields.linkingEntryPersonalName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Jon Doh' should exist", result.contains("Jon Doh"));

      testDataField = TestHelper.dataFieldFactory("withAdditionalCoding", null, "700", "a", "Jon Doh%DE3-1-2");
      PersonFields.linkingEntryPersonalName(testDataField);
      result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Jon Doh' should exist", result.contains("Jon Doh"));
   }

}
