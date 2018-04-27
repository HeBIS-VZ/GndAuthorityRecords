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
package de.hebis.it.gvi.clusterinfos.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hebis.it.hds.gnd.Model;
import net.openhft.chronicle.map.ChronicleMapBuilder;

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
public class XML2SynonymMap {
   /** The Constant LOG. */
   private static final Logger        LOG           = LogManager.getLogger(XML2SynonymMap.class);
   private static final Model         gndModel      = Model.getModel();
   private static final String        idPattern     = "requestId";
   private static final String        memberPattern = "duplicateId";
   private static Map<String, String> lookupMap     = null;
   private final Set<String>          proof         = new LinkedHashSet<>(15000000);
   private String                     sourceFile    = null;
   private String                     mapFile       = null;
   private int                        counter       = 0;


    public XML2SynonymMap() {
      this(null, null);
   }

   public XML2SynonymMap(String path2sourceFile, String path2mapFile) {
      sourceFile = getFilePath(path2sourceFile, "ClusterSourceFile");
      mapFile = getFilePath(path2mapFile, "ClusterMap");
      ChronicleMapBuilder<String, String> lookupBuilder = ChronicleMapBuilder.of(String.class, String.class);
      lookupBuilder.averageKey("(DE-602)gbv_860246728+"); // example to calculate the needed space.
      lookupBuilder.averageValue("(DE-602)gbv_860246728+"); // example to calculate the needed space.
      lookupBuilder.entries(70000000); // 70Mio entries expected
      try {
         lookupMap = lookupBuilder.createOrRecoverPersistedTo(new File(mapFile));
      } catch (IOException e) {
         throw new RuntimeException("Can't open cachefile: " + mapFile, e);
      }
   }

   public void convert() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
      if (!lookupMap.isEmpty()) {
         LOG.warn("ClusterMap: " + mapFile + " with " + lookupMap.size() + " entries found. ->exit");
         return;
      }
        LOG.info(LocalDateTime.now().toString() + " Build cache with data from: " + sourceFile);
         XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(sourceFile));
         String clusterKey = null;
         String clusterMember = null;
         while (streamReader.hasNext()) {
            if (streamReader.next() == XMLStreamReader.START_ELEMENT) {
               switch (streamReader.getLocalName()) {
                  case memberPattern:
                     clusterMember = streamReader.getElementText();
                     if (proof.contains(clusterMember)) LOG.warn(clusterMember + " ist mehrfach verzeichnet. Skip new cluster ");
                     else {
                        proof.add(clusterMember);
                        lookupMap.put(clusterMember, clusterKey);
                     }
                     if (LOG.isInfoEnabled()) {
                        counter++;
                        if (counter % 1000000 == 0) {
                           LOG.info(counter / 1000000 + "M cluster mappings processed.");
                        }
                     }
                     break;
                  case idPattern:
                     clusterKey = streamReader.getElementText();
                     break;
               }
            }
         }
         LOG.info(LocalDateTime.now().toString() + ": " + counter + " cluster mappings successfully processed");
         LOG.info(LocalDateTime.now().toString() + ": " + lookupMap.size() + " preprocessed cluster mappings found");
   }

   @SuppressWarnings("javadoc")
   public static void main(String[] args) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError   {
      XML2SynonymMap me = new XML2SynonymMap();
      me.convert();
   }

   private String getFilePath(String preset, String parameterName) {
      if (preset != null) return preset;
      else {
         String ret = gndModel.getProperty(parameterName);
         if (ret == null) throw new RuntimeException("Parameter: " + parameterName + " is not defined.");
         return gndModel.addDefaultDir(ret);
      }
   }
   

}
