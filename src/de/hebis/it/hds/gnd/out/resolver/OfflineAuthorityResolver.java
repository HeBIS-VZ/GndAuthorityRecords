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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hebis.it.hds.gnd.Model;
import de.hebis.it.hds.gnd.out.AuthorityBean;
import net.openhft.chronicle.map.ChronicleMapBuilder;

/**
 * Get the authority informations from the repository.<br>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-05-12 uh initial version
 */
public class OfflineAuthorityResolver extends AuthorityResolver {
   private static final Logger LOG              = LogManager.getLogger(OfflineAuthorityResolver.class);
   private Model               model            = Model.getModel();
   private static Map<String,String>   synonymMap         = null;
   private String              partSeparator    = model.getProperty("GroupSeparator", "\u001D");
   private String              synonymSeparator = model.getProperty("EntrySeperator", "\u001F");

   /**
    * Load the default synonym property file
    * 
    */
   public OfflineAuthorityResolver() {
      String filePath = model.getProperty("SynonymMap");
      if (filePath == null) throw new RuntimeException("Parameter 'SynonymMap' is not defined.");
      filePath = model.addDefaultDir(filePath);
      init(filePath);
   }

   /**
    * Load the given synonym property file.
    * 
    * @param filePath Path to the property file
    */
   public OfflineAuthorityResolver(String filePath) {
      init(filePath);
   }

   @Override
   public void init(String filePath) {
      if (synonymMap != null) return;
      ChronicleMapBuilder<String, String> lookupBuilder = ChronicleMapBuilder.of(String.class, String.class);
      lookupBuilder.averageKey("(DE-588)0123456789abc"); // example to calculate the needed space.
      lookupBuilder.averageValueSize(200); // to calculate the needed space.
      lookupBuilder.entries(2000000); // 70Mio entries expected
      try {
         synonymMap = lookupBuilder.createOrRecoverPersistedTo(new File(filePath));
      } catch (IOException e) {
         throw new RuntimeException("Can't open synonym map: " + filePath, e);
      }
   }

   /**
    * get the data for the given id
    * 
    * @param recordId The complete Id mostly prefixed with a ISIL. Eg. '(DE-588)' for the GND
    * @return a authority bean representing the authority record or null if the id is unknown.
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   @Override
   public AuthorityBean getAuthorityBean(String recordId) throws AuthorityRecordException {
      if (recordId == null) throw new NullPointerException("The id is mandatory");
      String synonymString = synonymMap.get(recordId);
      if (synonymString == null) {
         if (LOG.isDebugEnabled()) LOG.debug("Failed to resolve id:" + recordId);
         return null;
      }
      if (synonymString.isEmpty()) throw new AuthorityRecordException("Data error (empty) in synonym file at id:" + recordId);
      String[] parts = synonymString.split(partSeparator);
      AuthorityBean ret = new AuthorityBean();
      ret.id = recordId;
      ret.preferred = parts[0];
      if (parts.length > 1) ret.synonyms = Arrays.asList(parts[1].split(synonymSeparator));
      return ret;
   }

   private void listAll() {
      AuthorityBean tst = null;
      for (String key : synonymMap.keySet()) {
         try {
            tst = getAuthorityBean(key);
         } catch (AuthorityRecordException e) {
            continue;
         }
         LOG.info(tst.toString());
      }
   }
   
   /**
    * Minimal Test and usage example
    * 
    * @param args cmd line parameters
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   public static void main(String[] args) throws AuthorityRecordException {
      OfflineAuthorityResolver me = new OfflineAuthorityResolver();
      me.listAll();
   }

}
