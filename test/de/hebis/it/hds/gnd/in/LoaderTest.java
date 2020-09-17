/*
 * Copyright 2016, 2017 by HeBIS (www.hebis.de).
 * 
 * This file is part of HeBIS project Gnd4Index.
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
package de.hebis.it.hds.gnd.in;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import data.TestData;

/**
 * Tests to validate the import of the authority records
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-26 uh initial version
 */
public class LoaderTest {
   private static Loader loader2Test = null;

   /**
    * Get a loader for all tests in this class.<br>
    * 
    */
   @BeforeClass
   public static void init() {
      // URL to Solr Server is only local accessible. 
      loader2Test = new Loader("http://zantafino.hebis.uni-frankfurt.de:3001/solr/GND_01");
   }

   /**
    * Loading topic terms<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadTopic() {
      loader2Test.load(TestData.getURI4Term());
      assertTrue(true); // dummy
   }

   /**
    * Loading organizations and companies<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadOrganisation() {
      loader2Test.load(TestData.getURI4Organisation());
      assertTrue(true); // dummy
   }

   /**
    * Loading conferences and events<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadConference() {
      loader2Test.load(TestData.getURI4Conference());
      assertTrue(true); // dummy
   }

   /**
    * Loading geographic terms<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadGeographic() {
      loader2Test.load(TestData.getURI4Geographic());
      assertTrue(true); // dummy
   }

   /**
    * Loading not individualized persons<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadPerson() {
      loader2Test.load(TestData.getURI4Person());
      assertTrue(true); // dummy
   }

   /**
    * Loading individualized persons<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadIndividulizedPerson() {
      loader2Test.load(TestData.getURI4IndividulizedPerson());
      assertTrue(true); // dummy
   }

   /**
    * Loading works<br>
    * The quality of the imported data is untested.
    */
   @Test
   public void loadWork() {
      loader2Test.load(TestData.getURI4Work());
      assertTrue(true); // dummy
   }

   /**
    * Loading deletions and redirections<br>
    * The quality of the imported data is untested.
    */
//   @Test
   public void loadRedirect() {
      loader2Test.load(TestData.getURI4Redirect());
      assertTrue(true); // dummy
   }

   /**
    * Loading all kind from OAI update<br>
    * The quality of the imported data is untested.
    */
 //  @Test
   public void loadUpdate() {
      loader2Test.load(TestData.getURI4UpdateData());
      assertTrue(true); // dummy
   }
   
   
   // TODO
   // toTest.load(TestData.getLocal());
   // toTest.load(TestData.getLibrary());

}
