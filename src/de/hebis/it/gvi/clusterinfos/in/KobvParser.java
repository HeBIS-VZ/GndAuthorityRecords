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
package de.hebis.it.gvi.clusterinfos.in;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

/**
 * Function to parse/convert a single Marc21-XML authority record<br>
 * The converted data will be returned as a {@link SolrInputDocument}.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-07-17 uh initial
 *
 */
public class KobvParser implements Function<List<String>, Boolean> {
   private final static Logger          LOG                = LogManager.getLogger(KobvParser.class);
   private HashMap<String, String> clearing = null;

   /**
    * Define where to store the parsed records.
    * 
    * @param clusterMap Map to detect ambiguous mappings.
    */
   public KobvParser(HashMap<String, String> clusterMap) {
      clearing = clusterMap;
   }

   /**
    * Parse the clusterinfo and store for each member the key into the HashMap.<br>
    * 
    */
   @Override
   public Boolean apply(List<String> recordAsListOfLines) {
      if (recordAsListOfLines == null) {
         LOG.debug("NULL record received. If this is an OAI update, its normal");
         return true;
      }
      if (recordAsListOfLines.size() < 4) {
         LOG.warn("Unusable record received"); // less than two members in cluster
         return false;
      }
      String clusterKey = null;
      for (String line : recordAsListOfLines) {
         if (clusterKey == null) clusterKey = line.trim().split(" ")[0];
         else tryPut(line.trim(), clusterKey); 
      }
      return true;
   }

   private void tryPut(String titleId, String clusterKey) {
      if (titleId.length() < 5) return; // simplest garbage disposer
      if (clearing.get(titleId) != null) {
         LOG.warn("Title: " + titleId + " is member of two clusters: " + clearing.get(titleId) + " and " + clusterKey);
         return;
      }
      else clearing.put(titleId, clusterKey);    
   }

}
