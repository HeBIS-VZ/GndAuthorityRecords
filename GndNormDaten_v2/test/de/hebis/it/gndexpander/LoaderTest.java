package de.hebis.it.gndexpander;

import static org.junit.Assert.*;

import org.junit.Test;

import de.hebis.it.gndexpander.data.TestData;

public class LoaderTest {

   @Test
   public void test() {
      Loader toTest = new Loader("http://zantafino.hebis.uni-frankfurt.de:3001/solr/GND_01");
      toTest.load(TestData.getURI4Term());
   }

}
