/**
 * 
 */
package de.hebis.it.hds.gnd;

import de.hebis.it.hds.gnd.out.AutorityRecordFileFinder;
import de.hebis.it.hds.gnd.out.AutorityRecordFinder;

/**
 * Static helper for other tests.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 17.09.2020 uh initial
 */
public class TestHelper {
   static AutorityRecordFinder gndprop = new AutorityRecordFileFinder("test/data/GndSynonyms.prop");

   /**
    * Get default testdata
    * 
    * @return
    */
   public static AutorityRecordFinder getAuthorityData() {
      return gndprop;
   }

}
