package de.hebis.it.hds.gnd.out;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;

/**
 * Erste Implementierung für eine Solr-Backend.<br/>
 * @author uwe
 *
 */
public class Finder implements FinderInterface {
   private static SolrClient  server = null;
   
 /**
  * Initialize a new Finder and connect to the given Solr core
  * @param baseUrl URL to identify the core to use eg. "http://host:8983/solr/core"  
  */
   public Finder(String baseUrl) {
      super();
      server = new HttpSolrClient.Builder(baseUrl).build();      
   }

   @Override
   public GndBean getGndBean(String normDataId) {
      if (server == null) return null;
      if (normDataId == null) throw new NullPointerException("Die Id der Normdatensatzes ist ein Pflichtparameter");
      SolrDocument doc = getGND(normDataId);
      if (doc == null) return null;
      return new GndBean((String) doc.getFieldValue("id"), (String) doc.getFieldValue("preferred"), doc.getFieldValues("synonyms"), doc.getFieldValues("related"));
   }

   @Override
   public FinderStatus getStatus() {
      return (server != null) ? FinderStatus.READY : FinderStatus.DOWN;
   }

   @Override
   public String getStatusText() {
      if (server != null) {
         return "Die Vebindung zum Solr Server besteht.";
      }
      return "Die Verbindung zum Solr Server konnte nicht aufgebaut werden.";
   }

   private SolrDocument getGND(String documentId) {
      QueryResponse response = null;
      SolrQuery rtg_query = new SolrQuery();
      rtg_query.setRequestHandler("/get");
      rtg_query.set("fl", "fullrecord");
      rtg_query.setFields("id", "preferred", "synonyms");
      rtg_query.set("id", documentId);
      // RealTimeGet ausführen.
      try {
         response = server.query(rtg_query);
         if (response.getStatus() != 0) throw new SolrServerException("Responsestatus: " + response.getStatus());
      } catch (SolrServerException | IOException e) {
         e.printStackTrace();
         throw new RuntimeException("HDS-Index kann Suche \"" + rtg_query.toString() + "\" kann nicht ausgeführen.", e);
      }
      // Antwort auswerten
      NamedList<Object> result = response.getResponse();
      return (SolrDocument) result.get("doc");
   }
   
   

}
