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
 * @version 30.03.2017 uh initial
 */
public class PersonFieldsTest {

   /**
    * personal name (data field 100)
    */
   @Test
   public void personalName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)100000096", null, "100", "a", "Ambrosius");
      TestHelper.addSubField(testDataField, "b", "III");
      TestHelper.addSubField(testDataField, "c", "de Lombez", "other title");
      PersonFields.personalName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("preferred");
      assertTrue("A name is expected", (result != null));
      assertTrue("The combined name 'Ambrosius III <de Lombez> <other title>' should exist", result.contains("Ambrosius III <de Lombez> <other title>"));
   }
   
   /**
    * Check related terms and related ids (data field 500)
    */
   @Test
   public void relatedPersonalName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)100000193", null, "500", "a", "Bauer, Heinrich Gottfried");
      TestHelper.addSubField(testDataField, "0", "(DE-101)121453839", "(DE-588)121453839", "http://d-nb.info/gnd/121453839");
      TestHelper.addSubField(testDataField, "9", "4:bezf", "v:Sohn");
      PersonFields.relatedPersonalName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("related");
      assertTrue("A related Name is expected", (result != null));
      assertTrue("A related term should to be 'Bauer, Heinrich Gottfried'", result.contains("Bauer, Heinrich Gottfried"));
      result = testDataField.getFieldValues("relatedIds");
      assertTrue("Related ids are expected", (result != null));
      assertTrue("One related id should to be '(DE-588)121453839'", result.contains("(DE-588)121453839"));
   }
   
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

      TestHelper.addSubField(testDataField, "0", "(isil) foo bar", "http://anywhere.edu");
      PersonFields.linkingEntryPersonalName(testDataField);
      result = testDataField.getFieldValues("sameAs");
      assertTrue("The altrnative id '(isil) foo bar' should exist", result.contains("(isil) foo bar"));
      assertFalse("The altrnative URL to the other system should not be storred", result.contains("http://anywhere.edu"));

      testDataField = TestHelper.dataFieldFactory("withAdditionalCoding", null, "700", "a", "Jon Doh%DE3-1-2");
      PersonFields.linkingEntryPersonalName(testDataField);
      result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Jon Doh' should exist", result.contains("Jon Doh"));
   }

}
