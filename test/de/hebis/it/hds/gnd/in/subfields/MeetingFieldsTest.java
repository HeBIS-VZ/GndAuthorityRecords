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
 * @version 28.04.2017 uh initial
 */
public class MeetingFieldsTest {

   /**
    * name of the meeting (data field 111)
    */
   @Test
   public void headingMeetingName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)1-2", null, "111", "a", "Conference of Non-Nuclear Weapon States");
      MeetingFields.headingMeetingName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("preferred");
      assertTrue("The preferred term is mandatory", (result != null));
      assertTrue("The preferred term  has to be unique", (result.size() == 1));
      assertTrue("The preferred term should be 'Conference of Non-Nuclear Weapon States'.", result.contains("Conference of Non-Nuclear Weapon States"));   }

   /**
    * Check alternative names (data field 411)
    */
   @Test
   public void tracingMeetingName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)10-3", null, "411", "a", "Congrès des économistes de langue française");
      MeetingFields.tracingMeetingName(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Congrès des économistes de langue française' should exist", result.contains("Congrès des économistes de langue française"));
   }

   /**
    * Check related terms and related ids (data field 511)
    */
   @Test
   public void relatedMeetinglName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)3-6", null, "511", "a", "Consfătuire de Sudură şi Încercări de Metale");
      TestHelper.addSubField(testDataField, "0", "(DE-588)1216918-3", "http://d-nb.info/gnd/1216918-3");
      MeetingFields.relatedMeetingName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("related");
      assertTrue("A related Name is expected", (result != null));
      assertTrue("A related term should to be 'Consfătuire de Sudură şi Încercări de Metale'", result.contains("Consfătuire de Sudură şi Încercări de Metale"));
      result = testDataField.getFieldValues("relatedIds");
      assertTrue("Related ids are expected", (result != null));
      assertTrue("One related id should to be '(DE-588)1216918-3'", result.contains("(DE-588)1216918-3"));
   }
   
   /**
    * Alternative names from other Systems (data field 711)
    */
   @Test
   public void linkingEntryMeetingName() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)5246-2", null, "711", "a", "国際歴史学会議");
      TestHelper.addSubField(testDataField, "0", "(isil) foo bar", "http://anywhere.edu");
      MeetingFields.linkingEntryMeetingName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym '国際歴史学会議' should exist", result.contains("国際歴史学会議"));
      result = testDataField.getFieldValues("sameAs");
      assertTrue("The alternative id '(isil) foo bar' should exist", result.contains("(isil) foo bar"));
      assertFalse("The alternative URL to the other system should not be storred", result.contains("http://anywhere.edu"));
   }

}
