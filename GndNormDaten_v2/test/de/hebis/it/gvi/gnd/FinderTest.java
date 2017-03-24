package de.hebis.it.gvi.gnd;

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

import de.hebis.it.gvi.gnd.Finder;
import de.hebis.it.gvi.gnd.GndBean;
import de.hebis.it.gvi.gnd.interfaces.FinderStatus;

/**
 * Überprüft ob der {@link Finder} geforderten Eigenschaften aufweist.<br/>
 * Hier wird implizit die Anbindung zu einem SolrBackend geprüft
 * 
 * @author Uwe 14.03.2017
 *
 */
public class FinderTest {
   private static final String baseUrl = "http://zantafino.hebis.uni-frankfurt.de:3001/solr/GND_01";
   private static final String orwell  = "(DE-588)118590359";
   private static Finder   finder  = null;

   /**
    * Aufbau der Datenverbindung zum Backend (SOLR).<br/>
    * Hinterlegen der Testdaten im Backend<br/>
    * 
    * @throws IOException
    * @throws SolrServerException
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
