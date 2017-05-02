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
 * Test class for {@link TopicFields}
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class TopicFieldsTest {
   /**
    * Check preferred term (data field 150)
    */
   @Test
   public void headingTopicalTerm() {
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "150", "a", "TopTop");
      TopicFields.headingTopicalTerm(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("preferred");
      assertTrue("The preferred term is mandatory", (result != null));
      assertTrue("The preferred term  has to be unique", (result.size() == 1));
      assertTrue("The preferred term should be 'TopTop'.", result.contains("TopTop"));
   }

   /**
    * complex terms and ids (data field 260)
    */
   @Test
   public void complexSeeReferenceTerm() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)4153935-7", null, "260", "a", "Feldforschung");
      TestHelper.addSubField(testDataField, "0", "foo", "(DE-588)4016674-0", "https://d-nb.info/gnd/4016674-0");
      TopicFields.complexSeeReferenceTerm(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A complex term is expected", (result != null));
      assertTrue("The complex term 'Feldforschung' should exist", result.contains("Feldforschung"));
      result = testDataField.getFieldValues("seeAlso");
      assertTrue("Similar ids are expected", (result != null));
      assertTrue("The related id '(DE-588)4016674-0' should be storred", result.contains("(DE-588)4016674-0"));
      assertFalse("The related id 'https://d-nb.info/gnd/4016674-0' should be dismissed", result.contains("https://d-nb.info/gnd/4016674-0"));
   }

   /**
    * Check alternative terms (data field 450)
    */
   @Test
   public void tracingTopicalTerm() {
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "450", "a", "AlsoTop");
      TopicFields.tracingTopicalTerm(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'AlsoTop' should exist", result.contains("AlsoTop"));
   }

   /**
    * Check related terms and related ids (data field 550)
    */
   @Test
   public void relatedTopicalTerm() {
      DataField testDataField = TestHelper.dataFieldFactory("testid", null, "550", "a", "AlsoTop");
      TestHelper.addSubField(testDataField, "0", "foo", "http://d-nb.info/gnd/4027242-4", "https://d-nb.info/gnd/4027242-4");
      TopicFields.relatedTopicalTerm(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("related");
      assertTrue("A related term is expected", (result != null));
      assertTrue("Exact one related term is expected", (result.size() == 1));
      assertTrue("The related term should to be 'AlsoTop'", result.contains("AlsoTop"));
      result = testDataField.getFieldValues("relatedIds");
      assertTrue("Related ids are expected", (result != null));
      assertTrue("Exact one related is is expected", (result.size() == 1));
      assertTrue("The related id should to be 'foo'", result.contains("foo"));
   }

   /**
    * Alternative names from other Systems (data field 750)
    */
   @Test
   public void linkingEntrylTopicalTerm() {
      DataField testDataField = TestHelper.dataFieldFactory("simpleCase", null, "750", "a", "OtherTerm");
      PersonFields.linkingEntryPersonalName(testDataField);
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'OtherTerm' should exist", result.contains("OtherTerm"));

      TestHelper.addSubField(testDataField, "0", "(isil) foo bar", "http://anywhere.edu");
      PersonFields.linkingEntryPersonalName(testDataField);
      result = testDataField.getFieldValues("sameAs");
      assertTrue("The altrnative id '(isil) foo bar' should exist", result.contains("(isil) foo bar"));
      assertFalse("The altrnative URL to the other system should not be storred", result.contains("http://anywhere.edu"));
   }

}
