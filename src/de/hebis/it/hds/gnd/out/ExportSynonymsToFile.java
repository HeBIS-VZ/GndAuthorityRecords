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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import de.hebis.it.hds.gnd.out.resolver.AuthorityRecordException;
import de.hebis.it.hds.gnd.out.resolver.OnlineAuthorityResolver;
import net.openhft.chronicle.map.ChronicleMapBuilder;

/**
 * Export the authority informations from the repository to a property file<br>
 * The lines of the file are defined as "&lt;ID&gt; = &lt;preferred&gt;(&lt;seperator&gt;&lt;synonym&gt;)*"<br>
 * Eg. "(DE-588)100000355 = Amman, Reiner!_#_!Amman, Reinerius"
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-06-18 uh initial version
 */
public class ExportSynonymsToFile extends OnlineAuthorityResolver {
   private static final Logger        LOG              = LogManager.getLogger(ExportSynonymsToFile.class);
   String                             propertyFilePath = null;
   protected static Map<String, String> synonymMap       = null;
   private String                     partSeparator    = model.getProperty("GroupSeparator", "\u001D");
   private String                     synonymSeparator = model.getProperty("EntrySeperator", "\u001F");
   private int                        count            = 0;
   private int                        maxCount         = Integer.MAX_VALUE;

   /**
    * Exports all entries from the Solr repository to the given file
    * 
    * @param filePath Fully qualified name of the output file or 'null' for console output.
    */
   public void openSynonymMap(String filePath) {
      ChronicleMapBuilder<String, String> lookupBuilder = ChronicleMapBuilder.of(String.class, String.class);
      lookupBuilder.averageKeySize(50); //  to calculate the needed space.
      lookupBuilder.averageValueSize(1000); // to calculate the needed space.
      lookupBuilder.entries(3000000); // 3Mio entries expected
      try {
         synonymMap = lookupBuilder.createOrRecoverPersistedTo(new File(filePath));
      } catch (IOException e) {
         throw new RuntimeException("Can't open synonym map: " + filePath, e);
      }
   }

   /**
    * Loop over all entries in the repository to write them to the output file
    */
   private void listAllEntries() {
      String cursorMark = "*";
      String nextCursorMark = null;
      QueryResponse rsp = null;
      SolrQuery query = new SolrQuery("id:*");
      query.setRows(Integer.valueOf(model.getProperty("StepSizeForExport", "100")));
      query.setSort(SortClause.asc("id"));
      do {
         // start with '*' in the first iteration, then use the last position
         if (nextCursorMark != null) {
            cursorMark = nextCursorMark;
         }
         // use the last position as new start value.
         query.set("cursorMark", new String[] { cursorMark });
         if (LOG.isTraceEnabled()) {
            LOG.trace(query.toString());
         }
         // execute the query
         try {
            rsp = server.query(query);
            if (rsp.getStatus() != 0) throw new SolrServerException("Responsestatus: " + rsp.getStatus());
         } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("The index can't eval \"" + query.toString() + "\".", e);
         }
         nextCursorMark = rsp.getNextCursorMark();
         // get the results of partial results
         List<AuthorityBean> partialResults = rsp.getBeans(AuthorityBean.class);
         if (LOG.isTraceEnabled()) LOG.trace(partialResults.size() + " records in this packet");
         // loop over the results
         for (AuthorityBean entry : partialResults) {
            if (LOG.isTraceEnabled()) LOG.trace("Bearbeite:  " + entry.id);
            putToMap(entry);
            if ((maxCount != Integer.MAX_VALUE) && (count >= maxCount)) return; // optional exit for debug purposes
         }
      } while (!cursorMark.equals(nextCursorMark));
   }

 
   /**
    * Write the representation of the given entry to the file.
    * 
    * @param entry The data to write
    */
   private void putToMap(AuthorityBean entry) {
      StringBuilder line = new StringBuilder();
      line.append(entry.preferred);
      line.append(partSeparator);
      if (entry.synonyms != null) {
         for (String synonym : entry.synonyms) {
            line.append(synonym.replace("", "").replace("", ""));
            line.append(synonymSeparator);
         }
         line.setLength(line.length()-1); // remove last synonymSeparator
      }
      synonymMap.put(entry.id, line.toString());
      count++;
   }

   /**
    * Exports the synonym file
    * 
    * @param args no cmd line parameters is used
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   public static void main(String[] args) throws AuthorityRecordException {
      System.out.println("\n\nExport authority synonyms to file");
      ExportSynonymsToFile me = new ExportSynonymsToFile();
      String maxCount = model.getProperty("MaxLinesToProcess");
      if (maxCount != null) {
         me.maxCount = Integer.valueOf(maxCount);
         System.out.println("\tThe export process will stop after " + maxCount + " lines.");
      }
      String filePath = model.getProperty("SynonymMap");
      if (filePath == null) throw new RuntimeException("Parameter 'SynonymMap' is not defined.");
      filePath = model.addDefaultDir(filePath);
      System.out.println("\tThe output will be written to " + filePath);
      
      System.out.println("");
      me.openSynonymMap(filePath);
      if (synonymMap.isEmpty()) me.listAllEntries();
      else System.out.println("Nothing to do. Map: " + filePath + " was already build.");
      System.out.println("\nFinished\n");
   }
}
