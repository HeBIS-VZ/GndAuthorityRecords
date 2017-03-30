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
package de.hebis.it.gvi.hds.gnd.out;

import static org.junit.Assert.*;

import org.junit.Test;

import de.hebis.it.hds.gnd.out.GndBean;

/**
 * Validiert minimale Anforderungen an eine GndBean
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-08 uh First try
 *
 */
public class GndBeanTest {

   /**
    * Der Konstruktor muss eine Id erhalten, die anderen Parameter dürfen 'null' sein.
    */
   @Test
   public void testKonstruktor() {
      try {
         new GndBean(null, null, null, null);
         fail("Die Id einer GndBean darf nicht 'null' sein");
      } catch (Exception e) {
         // Alles OK, den 'null' ist als erster Parameter nicht erlaubt.
      }
   }

   /**
    * Werden Defaultwerte gesetzt.
    */
   @Test
   public void testDefaultwerte() {
      GndBean test = new GndBean("a", null, null, null);
      assertEquals("a", test.getId());
      assertTrue(test.getPreferred().isEmpty());
      assertTrue(test.getSynonyms().isEmpty());
      assertTrue(test.getRelations().isEmpty());
   }
}
