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
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-02-28 uh First try
 */
public class Finder implements FinderInterface {
   private static SolrClient server = null;

   /**
    * Initialize a new Finder and connect to the given Solr core
    * 
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
