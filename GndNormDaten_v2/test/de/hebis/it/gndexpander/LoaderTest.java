package de.hebis.it.gndexpander;

import static org.junit.Assert.*;

import org.junit.Test;

import de.hebis.it.gndexpander.data.TestData;
import de.hebis.it.hds.gnd.in.Loader;

public class LoaderTest {

   @Test
   public void test() {
      Loader toTest = new Loader("http://zantafino.hebis.uni-frankfurt.de:3001/solr/GND_01");
      toTest.load(TestData.getURI4Term());
   }

}
