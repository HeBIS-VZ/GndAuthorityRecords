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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.junit.BeforeClass;
import org.junit.Test;

import de.hebis.it.hds.gnd.out.Finder;
import de.hebis.it.hds.gnd.out.FinderStatus;
import de.hebis.it.hds.gnd.out.GndBean;

/**
 * Überprüft ob der {@link Finder} geforderten Eigenschaften aufweist.<br>
 * Hier wird implizit die Anbindung zu einem SolrBackend geprüft
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-14 uh First try
 *
 */
public class FinderTest {
   private static final String baseUrl = "http://zantafino.hebis.uni-frankfurt.de:3001/solr/GND_01";
   private static final String orwell  = "(DE-588)118590359";
   private static Finder   finder  = null;

   /**
    * Aufbau der Datenverbindung zum Backend (SOLR).<br>
    * Hinterlegen der Testdaten im Backend<br>
    * 
    * @throws IOException Indicates a Problem with the HTTP connection
    * @throws SolrServerException Indicates a Problem with the data
    * 
    */
   @BeforeClass
   public static void init() throws SolrServerException, IOException {
      SolrClient  server = new HttpSolrClient.Builder(baseUrl).build();
      finder  = new Finder(baseUrl);
      server.add(ExampleSolrDocs.examples);
      server.commit();
      server.close();
   }

   /**
    * 
    */
   @Test
   public void basics() {
      assertEquals("Hat der Finder eine Verbindung zum Solr Index.", FinderStatus.READY, finder.getStatus());
      assertNotNull("Es muss immer eine textuelle Info zum Status geben.", finder.getStatusText());
      assertFalse("Der Statustext darf nicht leer sein.", finder.getStatusText().isEmpty());
   }
   /**
    * 
    */
   @Test
   public void falscheParameter() {
      try {
         assertNull("Eine unbekannte Id muss zum Rückgabewert 'null' führen.", finder.getGndBean("unbekannt"));
         assertNull("\"\" als Id muss zum Rückgabewert 'null' führen.", finder.getGndBean(""));
         assertNull("'null' als Id muss zum Rückgabewert 'null' führen.", finder.getGndBean(null));
      } catch (Exception e) {
         // Die Fehlersituationen werden scheinbar schon intern abgefangen -> OK
      }
   }
   
   /**
    * 
    */
   @Test
   public void checkOrwell() {
      GndBean data = finder.getGndBean(orwell);
      assertNotNull("Die vollständige ID \"" + orwell + "\" muss einen Treffer finden.", data);
      assertEquals("Die Id im Datensatz muss dem Aufruf entsprechen.", orwell, data.getId());
      assertNotNull("Die bevorzugte Beschreibung muss existieren.", data.getPreferred());
      assertNotNull("Die Collection der Synonyme muss existieren.", data.getSynonyms());
      assertNotNull("Die Collection der Relationen muss existieren.", data.getRelations());
   }

}
