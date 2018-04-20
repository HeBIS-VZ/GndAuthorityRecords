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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Export the authority informations from the repository to a property file<br>
 * The lines of the file are defined as "&lt;ID&gt; = &lt;preferred&gt;(&lt;seperator&gt;&lt;synonym&gt;)*"<br> 
 * Eg. "(DE-588)100000355 = Amman, Reiner!_#_!Amman, Reinerius"
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-06-18 uh initial version
 */
public class AutorityRecordFileWriter extends AutorityRecordSolrFinder {
   private static final Logger LOG              = LogManager.getLogger(AutorityRecordFileWriter.class);
   String                      propertyFilePath = null;
   private String              seperator        = config.getProperty("EntrySeperator", "!_#_!");
   private PrintWriter         out              = null;
   private int                 count            = 0;
   private int                 maxCount         = Integer.MAX_VALUE;

   /**
    * Exports all entries from the Solr repository to the default file
    */
   public void generateSynonymFile() {
      generateSynonymFile(config.getProperty("PropertyFilePath"));
   }

   /**
    * Exports all entries from the Solr repository to the given file
    * 
    * @param filePath Fully qualified name of the output file or 'null' for console output.
    */
   public void generateSynonymFile(String filePath) {
      if (filePath == null) {
         out = new PrintWriter(System.out);
      } else try {
         out = new PrintWriter(filePath);
      } catch (FileNotFoundException e) {
         LOG.error("Can't open file:" + filePath, e);
         System.exit(-1);
      }
      printFileHeader();
      listAllEntries();
      out.close();
   }

   /**
    * Loop over all entries in the repository to write them to the output file
    */
   private void listAllEntries() {
      String cursorMark = "*";
      String nextCursorMark = null;
      QueryResponse rsp = null;
      SolrQuery query = new SolrQuery("id:*");
      query.setRows(Integer.valueOf(config.getProperty("StepSizeForExport", "100")));
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
            printOut(entry);
            if ((maxCount != Integer.MAX_VALUE) && (count >= maxCount)) return; // optional exit for debug purposes
         }
         out.flush();
      } while (!cursorMark.equals(nextCursorMark));
   }

   /**
    * Write a header with short explanation to the file
    */
   private void printFileHeader() {
      out.println("# Synonym file for authority records, exported from the project: \"GndAuthorityRecords\".");
      out.println("# see: https://github.com/HeBIS-VZ/GndAuthorityRecords");
      out.println("# Format:");
      out.println("# Id of the authority record = <preferred notation>" + seperator + "<1st synonym>" + seperator + "..." + seperator + "<last synonym>");
      out.println("# ");
   }

   /**
    * Write the representation of the given entry to the file.
    * 
    * @param entry The data to write
    */
   private void printOut(AuthorityBean entry) {
      StringBuilder line = new StringBuilder(entry.id);
      line.append(" = ");
      line.append(entry.preferred);
      if (entry.synonyms != null) {
         for (String synonym : entry.synonyms) {
            line.append(seperator);
            line.append(synonym.replace("", "").replace("", ""));
         }
      }
      out.println(line.toString());
      count++;
   }

   /**
    * Exports the synonym file
    * 
    * @param args no cmd line parameters is used 
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   public static void main(String[] args) throws AuthorityRecordException {
      System.out.println("\n\nExport of authority synonyms.");
      AutorityRecordFileWriter me = new AutorityRecordFileWriter();
      String maxCount = config.getProperty("MaxLinesToProcess");
      if (maxCount != null)  {
         me.maxCount = Integer.valueOf(maxCount);
         System.out.println("\tThe export process will stop after " + maxCount + " lines."); 
      }
      String fpath = config.getProperty("PropertyFilePath");
      if (fpath != null) System.out.println("\tThe output will be written to " + fpath);
      System.out.println("");
      me.generateSynonymFile(config.getProperty("PropertyFilePath"));
      System.out.println("\nFinished\n");
   }
}
