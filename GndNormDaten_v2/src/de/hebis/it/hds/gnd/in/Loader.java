package de.hebis.it.hds.gnd.in;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import de.hebis.it.gndexpander.data.TestData;

/**
 * Import program for GND authority record files provided by the DNB.<br/>
 * The input files need to bee formated as marc21-XML.<br/>
 * The processed records will be send to an solr index.
 * 
 * @author Reh, Uwe 2017-03-17
 * 
 **/
public class Loader extends Thread {
   
   /** The Constant LOG. */
   static final Logger LOG    = LogManager.getLogger(Loader.class);
   
   /** The server. */
   private SolrClient  server = null;

   /**
    * Instantiates a new loader.
    *
    * @param url2Solr the url 2 solr
    */
   public Loader(String url2Solr) {
      server = new HttpSolrClient.Builder(url2Solr).build();
      if (server == null) throw new RuntimeException("Can't initialize solrj client.");
      LOG.debug("Solrj client is connected to " + url2Solr);
   }

   /**
    * Load.
    *
    * @param marcXmlFile the marc xml file
    */
   public void load(URI marcXmlFile) {
      LOG.debug("Starting with " + marcXmlFile.toString());
      BlockingQueue<Future<SolrInputDocument>> out = Loader.queueFactory(10);
      // Start producer as thread 
      XMLFileReader reader = new XMLFileReader(marcXmlFile, out);
      reader.run();
      // Start consuming
      while (true) {
         SolrInputDocument doc = getData(out, reader);
         if (doc == null) break;
         LOG.debug("Send document:" + doc.get("id") + " to Solr.");
         try {
            server.add(doc);
         } catch (SolrServerException | IOException e) {
            LOG.error("Faild sending document:" + doc.get("id"), e);
         }
      }
      LOG.debug("Finished with " + marcXmlFile.toString());
   }

   /**
    * Helper to get results from a output queue.<br/>
    * The response of method may be delayed for two reasons.
    * <ul>
    * <li>The next Future isn't ready yet.</li>
    * <li>The queue is empty, but the 'inputThread' is still alive and may produce new futures.</li>
    * </ul>
    *
    * @param outputQueue The outputQueue
    * @param inputThread the input thread
    * @return A SolrDocument representing an authority record. Or NULL if no more data is left.
    */
   public static SolrInputDocument getData(BlockingQueue<Future<SolrInputDocument>> outputQueue, Thread inputThread) {
      SolrInputDocument data;
      try {
         while (true) {
            while (inputThread.isAlive() && (outputQueue.isEmpty())) {
               Thread.sleep(10);
            }
            if (!outputQueue.isEmpty()) {
               data = outputQueue.take().get();
               return data;
            }
            if (inputThread.isAlive()) continue;
            return null;
         }
      } catch (InterruptedException | ExecutionException e) {
         e.printStackTrace(System.err);
      }
      return null;
   }

   /**
    * Factory for a new queue.
    * 
    * @param queuelength the capacity of the queue.
    * @return The new blocking queue
    */
   public static BlockingQueue<Future<SolrInputDocument>> queueFactory(int queuelength) {
      return new ArrayBlockingQueue<>(queuelength);
   }

   /**
    * The main method.
    *
    * @param args the arguments
    */
   public static void main(String args[]) {
      Loader me = new Loader("http://zantafino.hebis.uni-frankfurt.de:3001/solr/GND_01");
      me.load(TestData.getURI4Term());
   }
}
