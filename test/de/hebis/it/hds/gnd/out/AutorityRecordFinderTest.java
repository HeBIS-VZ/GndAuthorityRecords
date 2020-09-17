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
package de.hebis.it.hds.gnd.out;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.hebis.it.hds.gnd.TestHelper;

/**
 * Assuming an existing repository this test validates basic attributes of a result.<br>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-05-11 uh first try
 *
 */
public class AutorityRecordFinderTest {
   private static final String orwell  = "(DE-588)118590359";

   /**
    * Check the connection to the repository and the existence of an entry to George Orwell.
    *
    * @throws AuthorityRecordException indicates a problem while retrieving data from repository
    */
   @Test
   public void checkOrwellFromSolr() throws AuthorityRecordException {
      AutorityRecordFinder   finder  = TestHelper.getAuthorityData();
      AuthorityBean data = finder.getAuthorityBean(orwell);
      assertEquals("The found id must be equal to the queried.", orwell, data.id);  
      assertNotNull("The preferred textual naming is mandatory.", data.preferred);
      assertNotNull("This entry should have synonyms.", data.synonyms);
   }

   /**
    * Check the property file and the existence of an entry to George Orwell.
    *
    * @throws AuthorityRecordException indicates a problem while retrieving data from repository
    */
   @Test
   public void checkOrwellFromFile() throws AuthorityRecordException {
      AutorityRecordFinder   finder  = TestHelper.getAuthorityData();
      AuthorityBean data = finder.getAuthorityBean(orwell);
      assertEquals("The found id must be equal to the queried.", orwell, data.id);
      assertNotNull("The preferred textual naming is mandatory.", data.preferred);
      assertNotNull("This entry should have synonyms.", data.synonyms);
   }

}
