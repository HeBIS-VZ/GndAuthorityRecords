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
public class TitleFieldsTest {

   /**
    * Title (data field 130)
    */
   @Test
   public void headingTitle() {
      DataField testDataField = TestHelper.dataFieldFactory("DE-588)112539398X", null, "130", "a", "Born Loose");
      TitleFields.headingTitle(testDataField);
      Collection<Object> result = testDataField.getFieldValues("preferred");
      assertTrue("The preferred term is mandatory", (result != null));
      assertTrue("The preferred term  has to be unique", (result.size() == 1));
      assertTrue("The preferred term should be 'Born Loose'.", result.contains("Born Loose"));   }

   /**
    * Check alternative titles (data field 430)
    */
   @Test
   public void tracingTitle() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)4000196-9", null, "430", "a", "Deutsches Abrogans", "Keronisches Glossar");
      TitleFields.tracingTitle(testDataField);
      // check side effects
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym 'Keronisches Glossar' should exist", result.contains("Keronisches Glossar"));
   }

   /**
    * Check related titles and related ids (data field 530)
    */
   @Test
   public void relatedTitle() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)4001191-4", null, "530", "a", "Wilhelmszyklus");
      TestHelper.addSubField(testDataField, "0", "(DE-588)4189913-1", "http://d-nb.info/gnd/4189913-1");
      TitleFields.relatedTitle(testDataField);
      Collection<Object> result = testDataField.getFieldValues("related");
      assertTrue("A related Name is expected", (result != null));
      assertTrue("A related term should to be 'Wilhelmszyklus'", result.contains("Wilhelmszyklus"));
      result = testDataField.getFieldValues("relatedIds");
      assertTrue("Related ids are expected", (result != null));
      assertTrue("One related id should to be '(DE-588)4189913-1'", result.contains("(DE-588)4189913-1"));
   }
   
   /**
    * Alternative titles from other Systems (data field 730)
    */
   @Test
   public void linkingEntryTitle() {
      DataField testDataField = TestHelper.dataFieldFactory("(DE-588)4032444-8", null, "730", "a", "&#152;ال&#156; قرآن");
      TestHelper.addSubField(testDataField, "0", "(isil) foo bar", "http://anywhere.edu");
      TitleFields.linkingEntryTitle(testDataField);
      Collection<Object> result = testDataField.getFieldValues("synonyms");
      assertTrue("A synonym is expected", (result != null));
      assertTrue("The synonym '&#152;ال&#156; قرآن' should exist", result.contains("&#152;ال&#156; قرآن"));
      result = testDataField.getFieldValues("sameAs");
      assertTrue("The alternative id '(isil) foo bar' should exist", result.contains("(isil) foo bar"));
      assertFalse("The alternative URL to the other system should not be storred", result.contains("http://anywhere.edu"));
   }

}
