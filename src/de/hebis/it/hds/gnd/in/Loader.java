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
package de.hebis.it.hds.gnd.in;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import de.hebis.it.hds.gnd.Model;
import de.hebis.it.hds.tools.streams.TextBlockSpliterator;

/**
 * Import program for GND authority record files provided by the DNB.<br>
 * The input files need to bee formated as marc21-XML.<br>
 * The processed records will be send to an solr index.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-17 uh First try
 * 
 **/
public class Loader {

   /** The Constant LOG. */
   private static final Logger            LOG          = LogManager.getLogger(Loader.class);
   /** Start with a record tag which has attributes. (the OAI-Format has enclosing record tags without attributes) */
   private static final Predicate<String> startpattern = Pattern.compile(".*<record[^>].*").asPredicate();
   private static final Predicate<String> endpattern   = Pattern.compile(".*</record.*").asPredicate();
   private static Model                   config       = Model.getModel();
   private SolrClient                     server       = null;

   /**
    * Instantiates a new loader.
    *
    * @param baseSolrURL The URL to the repository (solr core)
    */
   public Loader(String baseSolrURL) {
      if (baseSolrURL == null) throw new RuntimeException("No URL to solr server provided.");
      if (LOG.isDebugEnabled()) {
         server = new HttpSolrClient.Builder(baseSolrURL).build();
      }
      else {
         server = new ConcurrentUpdateSolrClient.Builder(baseSolrURL).withQueueSize(1000).withThreadCount(10).build();
      }
      if (server == null) throw new RuntimeException("Can't initialize the solrj client.");
      LOG.debug("SolrWriter is connected to " + baseSolrURL);
   }

   /**
    * Load.
    *
    * @param marcXmlFile the marc xml file
    */
   public void load(URI marcXmlFile) {
      LOG.debug("Starting with " + marcXmlFile.toString());
      Path path2InputFile = Paths.get(marcXmlFile);
      Stream<String> lineStream;
      try {
         lineStream = Files.lines(path2InputFile);
      }
      catch (IOException e) {
         LOG.fatal("Fehler beim Lesen der Eingabedatei: " + path2InputFile.toString());
         throw new RuntimeException(e);
      }
      // group the lines. TODO find better code
      Stream<List<String>> marcXmlStream = TextBlockSpliterator.toTextBlocks(lineStream, startpattern, endpattern, true);
      // process the data. map and consume
      marcXmlStream.map(new MarcXmlParser(server)).forEach(x -> {
         if (!x) System.err.println("Fail");
      });
      LOG.info("Finished with " + marcXmlFile.toString());
      try {
         server.commit();
         server.close();
      }
      catch (SolrServerException | IOException e) {
         LOG.error("Failed sending final commit for:" + marcXmlFile.toString() + " to " + server.toString(), e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Read authority records (marcXML) from file
    * 
    * @param args List of files as URI or fully qualified path
    * @throws URISyntaxException if a given URI is syntactically wrong
    */
   public static void main(String[] args) throws URISyntaxException {
      if (args.length == 0) printHelp("At least one URI or filepath is needed.");
      Loader me = new Loader(config.getProperty("BaseURL"));
      for (String filepath : args) {
         System.out.println("GndLoader: process " + filepath);
         if (filepath.toLowerCase().startsWith("file")) me.load(new URI(filepath));
         else if (filepath.charAt(0) == '/') me.load(new URI("file:" + filepath));
         else printHelp("Dont't know how to handle \"" + filepath + "\".");
      }
      System.out.println("\n--- Fertsch --\n");
   }

   private static void printHelp(String msg) {
      System.out.println("\n\nLoader");
      System.out.println("  Msg: " + msg);
      System.out.println("  Params: List of URIs or absolut Filenames to import.");
      System.out.println("  Example: java Loader file:/home/user/git/GndNormDaten/test/de/hebis/it/hds/gnd/in/data/Ts.mrc.xml /home/user/git/GndNormDaten/test/de/hebis/it/hds/gnd/in/data/Tp.mrc.xml");
      System.exit(-1);
   }
}
