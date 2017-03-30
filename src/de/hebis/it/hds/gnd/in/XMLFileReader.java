package de.hebis.it.hds.gnd.in;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import de.hebis.it.hds.tools.concurrent.WaitingNamedExecutorService;
import de.hebis.it.hds.tools.streams.TextBlockSpliterator;


/**
 * Splits the marc21-XML file in single records and process them concurrently.<br/>
 * The converted records are channeld via an buffered queue.<br/>
 * 
 * @author Reh, Uwe 2017-03-17
 * 
 **/
public class XMLFileReader extends Thread {
   static final Logger                      LOG         = LogManager.getLogger(XMLFileReader.class);
   BlockingQueue<Future<SolrInputDocument>> out;
   Path                                     dateipfad;
   BufferedReader                           rawreader;
   WaitingNamedExecutorService              executor    = new WaitingNamedExecutorService("GndXmlConverter", 1, 1);
   private Consumer<List<String>>           blockparser = null;

   /**
    * Initialize the Thread
    * 
    * @param file URI of the input file
    * @param outputQueue Queue to deposit the result
    */
   public XMLFileReader(URI file, BlockingQueue<Future<SolrInputDocument>> outputQueue) {
      out = outputQueue;
      dateipfad = Paths.get(file);
      // Define consumer as closure
      blockparser = block -> {
         if (block.isEmpty()) {
            LOG.warn("Block ist leer");
            return;
         }
         Callable<SolrInputDocument> xmlParser = new Xml2SolrWorker(block);
         Future<SolrInputDocument> ergebnisplatzhalter = executor.submit(xmlParser);
         try {
            out.put(ergebnisplatzhalter);
            if (LOG.isTraceEnabled()) LOG.trace("Ein neuer Datensatz wurde in Warteschlange eingetragen.");
         } catch (InterruptedException e) {
            LOG.warn(e.toString());
         }
      };
   }

   /**
    * Start of the Fileprocessing
    */
   @Override
   public void run() {
      Predicate<String> startpattern = Pattern.compile(".*<record.*").asPredicate();
      Predicate<String> endpattern = Pattern.compile(".*</record.*").asPredicate();
      Stream<List<String>> tbs = null;

      try {
         // Mapper von Stream<String> zu Stream<List<String>> !! Achtung Stream muss sequenziell bleiben.
         tbs = TextBlockSpliterator.toTextBlocks(Files.lines(dateipfad), startpattern, endpattern, false);
      } catch (IOException e) {
         LOG.fatal("Fehler beim Lesen der Eingabedatei: " + dateipfad.toString(), e);
      }
      // Strom der Titeldaten bearbeiten.
      // tbs.collect(BlockParser::new, BlockParser::accept, BlockParser::combiner);
      tbs.forEach(blockparser);
      executor.shutdown();
   }

}
