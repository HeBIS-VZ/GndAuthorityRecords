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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hebis.it.hds.tools.streams.TextBlockSpliterator;

/**
 * !!! Prelimary !!! Import of clusterinfos provided by KOBV<br>
 * The expected fileformat is:
 * 
 * <pre>
 *   clusterkey {
 *      Id of 1st doublet title
 *      Id of 2nd doublet title
 *      ...
 *      Id of last doublet title
 *   }
 * </pre>
 * 
 * The processed info for each member will be send to the gnd index as:
 * 
 * <dl>
 * <dh>id</dh>
 * <dd>id of doublet title</dd> <dh>preferred</dh>
 * <dd>clusterkey</dd> <dh>synonymes</dh>
 * <dd>list of all members (ids) in this cluster</dd>
 * </dl>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-07-17 uh first try
 * 
 **/
public class ImportKobvInfos {
   /** The Constant LOG. */
   private static final Logger            LOG          = LogManager.getLogger(ImportKobvInfos.class);
   private static final Predicate<String> startpattern = Pattern.compile("\\{").asPredicate();
   private static final Predicate<String> endpattern   = Pattern.compile("}").asPredicate();

   /**
    * Load.
    *
    * @param doubletInfoFile
    */
   public void load(URI doubletInfoFile) {
      HashMap<String, String> clearing = new HashMap<>();
      LOG.debug("Starting with " + doubletInfoFile.toString());
      Path path2InputFile = Paths.get(doubletInfoFile);
      Stream<String> lineStream;
      try {
         lineStream = Files.lines(path2InputFile);
      } catch (IOException e) {
         LOG.fatal("Fehler beim Lesen der Eingabedatei: " + path2InputFile.toString());
         throw new RuntimeException(e);
      }
      // group the lines sequentiell
      Stream<List<String>> clusterInfoStream = TextBlockSpliterator.toTextBlocks(lineStream, startpattern, endpattern, false);
      // process the data. map and consume
      clusterInfoStream.map(new KobvParser(clearing)).forEach(x -> {
         if (!x) {
            LOG.error("Unexpected error in parsing.");
            System.exit(-2);
         }
      });
      LOG.error("Finished with " + doubletInfoFile.toString());
      System.out.println(clearing.toString().replace(',', '\n'));
   }

   @SuppressWarnings("javadoc")
   public static void main(String[] args) throws URISyntaxException {
      ImportKobvInfos me = new ImportKobvInfos();
//      me.load(new URI("file:///tmp/gvi_dups.txt"));
       me.load(new URI("file:///work2/GND/Kobv/gvi_dups.txt"));
      // me.load(new URI("file:///C:/work/rawdata/gvi_dups.txt"));
   }

}
