package de.hebis.it.gvi.gnd;

import static org.junit.Assert.*;

import org.junit.Test;

import de.hebis.it.hds.gnd.out.GndBean;

/**
 * Validiert minimale Anforderungen an eine GndBean
 * @author Uwe 
 * 08.03.2017
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
