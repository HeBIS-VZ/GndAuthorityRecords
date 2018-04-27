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
package de.hebis.it.hds.gnd.out.resolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;

import de.hebis.it.hds.gnd.out.AuthorityBean;

/**
 * Get the authority informations from the repository.<br>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-06-02 uh initial version
 */
public class OnlineAuthorityResolver extends AuthorityResolver {
   private static final Logger          LOG                  = LogManager.getLogger(OnlineAuthorityResolver.class);
   protected SolrClient                 server               = null;
   protected Map<String, AuthorityBean> gndCache             = new HashMap<>();
   private DocumentObjectBinder         documentObjectBinder = new DocumentObjectBinder();

   /**
    * Initialize a new Finder and connect to the default Solr core
    * 
    */
   public OnlineAuthorityResolver() {
      init(model.getProperty("BaseURL"));
   }

   /**
    * Initialize a new Finder and connect to the given Solr core
    * 
    * @param baseUrl URL to identify the core to use eg. "http://host:8983/solr/core"
    */
   public OnlineAuthorityResolver(String baseUrl) {
      init(baseUrl);
   }

   /**
    * Connecting Solr
    * 
    * @param baseUrl URL to identify the core to use eg. "http://host:8983/solr/core"
    */
   @Override
   public void init(String baseUrl) {
      if (baseUrl == null) throw new RuntimeException("Parameter \"BaseURL\" is missing.");
      server = new HttpSolrClient.Builder(baseUrl).build();
   }

   /**
    * Get GND data for the given id<br>
    * This function uses the internal cache {@link #getAuthorityBean(String, boolean) to avoid network traffic
    * 
    * @param recordId The complete Id mostly prefixed with a ISIL. Eg. '(DE-588)' for the GND
    * @return a authority bean representing the authority record or null if the id is unknown.
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   @Override
   public AuthorityBean getAuthorityBean(String recordId) throws AuthorityRecordException {
      return this.getAuthorityBean(recordId, false);
   }

   /**
    * Get GND data for the given id<br>
    * This function may use the internal cache to avoid network traffic
    * 
    * @param recordId The complete Id mostly prefixed with a ISIL. Eg. '(DE-588)' for the GND
    * @param ignoreCache If TRUE the internal cache (HashMap) won't be used. This ensures to receive the newest version but will slow down the indexing process.
    * @return a authority bean representing the authority record or null if the id is unknown.
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   public AuthorityBean getAuthorityBean(String recordId, boolean ignoreCache) throws AuthorityRecordException {
      if (server == null) return null;
      if (recordId == null) throw new NullPointerException("The id is mandatory");
      AuthorityBean ret;
      synchronized (gndCache) {
         ret = gndCache.get(recordId);
         if (ret != null) {
            return ret;
         }
         ret = doRealTimeGet(recordId);
         gndCache.put(recordId, ret);
      }
      return ret;
   }

   /**
    * Clears the internal cache
    */
   public void flushGndCache() {
      gndCache = new HashMap<>();
   }

   /**
    * Do real time get.
    *
    * @param documentId the id of the authority record
    * @return a authority bean representing the authority record or null if the id does not exist.
    * @throws AuthorityRecordException
    */
   private AuthorityBean doRealTimeGet(String documentId) throws AuthorityRecordException {
      QueryResponse response = null;
      SolrQuery rtg_query = new SolrQuery();
      rtg_query.setRequestHandler("/get");
      rtg_query.set("fl", "fullrecord");
      rtg_query.setFields("id", "preferred", "synonyms");
      rtg_query.set("id", documentId);
      try {
         response = server.query(rtg_query);
         if (response.getStatus() != 0) throw new SolrServerException("Response state: " + response.getStatus());
      } catch (SolrServerException | IOException e) {
         e.printStackTrace();
         throw new AuthorityRecordException("Solr query \"" + rtg_query.toString() + "\" can't be executed.", e);
      }
      // Workaround: RTG does not allow to call response.getBeans(AuthorityBean.class);
      NamedList<Object> result = response.getResponse();
      if (result == null) throw new AuthorityRecordException("Solr query \"" + rtg_query.toString() + "\" has no result.");
      SolrDocument doc = (SolrDocument) result.get("doc");
      if ((doc == null) || (doc.size() == 0)) {
         if (LOG.isDebugEnabled()) LOG.debug("Solr query \"" + rtg_query.toString() + "\" No doc found.");
         return null;
      }
      return documentObjectBinder.getBean(AuthorityBean.class, doc);
   }

   /**
    * Minimal Test and usage example
    * 
    * @param args cmd line parameters
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   public static void main(String[] args) throws AuthorityRecordException {
      OnlineAuthorityResolver me = new OnlineAuthorityResolver();
      me.getAuthorityBean(args[0]);
      AuthorityBean data = me.getAuthorityBean("(DE-588)113582781");
      System.out.println(data.toString());
   }
}
