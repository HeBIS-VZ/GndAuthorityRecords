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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Convert of clusterinfos provided by KOBV<br>
 * The expected fileformat is:
 * 
 * <pre>
 * <clusters>
 *   <request>
 *     <requestId>(DE-601)86024573X</requestId>
 *       <duplicateId>(DE-601)86024573X</duplicateId>
 *       ...
 *   </request>
 *   ...
 * </clusters>
 * </pre>
 * 
 * The processed info for each member will be send to the gnd index as:
 * 
 * <dl>
 * <dh>id</dh>
 * <dd>requestId</dd> <dh>preferred</dh>
 * <dd>requestId</dd> <dh>synonymes</dh>
 * <dd>list of all members (duplicateId) in this cluster</dd>
 * </dl>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-07-17 uh first try
 * 
 **/
public class XML2Synonym {
   /** The Constant LOG. */
   private static final Logger LOG           = LogManager.getLogger(XML2Synonym.class);
   private static final String idPattern     = "requestId";
   private static final String memberPattern = "duplicateId";
   private final Set<String> proof = new LinkedHashSet<>(15000000);

   private class Cluster extends HashMap<String, Set<String>> {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      String      id;
      Set<String> dubletts;

      public Cluster(String id) {
         this.id = id;
         dubletts = new HashSet<>();
      }
   }

   /**
    * Load.
    *
    * @param doubletInfoFile
    * @throws IOException 
    */
   public void load(URI doubletInfoFile) throws IOException {
      Cluster cluster = null;
      LOG.debug("Starting with " + doubletInfoFile.toString());
      BufferedReader clusterinfo = new BufferedReader(new FileReader(doubletInfoFile.getPath())) ;
      String line = null;
      while ((line = clusterinfo.readLine()) != null) {
         if (line.contains(memberPattern)) {
            String dublett = extract(line, memberPattern);
            if (proof.contains(dublett)) LOG.warn(dublett + " ist mehrfach verzeichnet.");
            else proof.add(dublett);
            cluster.dubletts.add(dublett);
         }
         else if (line.contains(idPattern)) {
            if (cluster != null) process(cluster);
            cluster = new Cluster(extract(line, idPattern));
         }
      }
      if (cluster != null) process(cluster);
      clusterinfo.close();
      LOG.error("Finished with " + doubletInfoFile.toString());
   }

   private void process(Cluster cluster) {
      System.out.print(cluster.id);
      for (String dublett : cluster.dubletts) {
         System.out.print(", ");
         System.out.print(dublett);
      }
      System.out.print("\n");
   }
   
   private String extract(String line, String pattern) {
      int pos = line.indexOf(pattern);
      pos = line.indexOf('>', pos) + 1; 
      return line.substring(pos, line.indexOf('<', pos));
   }

   @SuppressWarnings("javadoc")
   public static void main(String[] args) throws URISyntaxException, IOException {
      XML2Synonym me = new XML2Synonym();
      me.load(new URI("file:///tmp/gvi_dups.txt"));
      // me.load(new URI("file:///work2/GND/Kobv/gvi_dups.txt"));
      // me.load(new URI("file:///C:/work/rawdata/gvi_dups.txt"));
   }

}
