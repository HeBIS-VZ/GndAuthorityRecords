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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hebis.it.hds.gnd.Model;

/**
 * Get the authority informations from the repository.<br>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-05-12 uh initial version
 */
public class AutorityRecordFileFinder  extends AutorityRecordFinder {
   private static final Logger  LOG                  = LogManager.getLogger(AutorityRecordFileFinder.class);
   private Model model = Model.getModel();
   private static Properties    synonyms               = null;
   private String seperator = config.getProperty("EntrySeperator", "!_#_!");

   /**
    * Load the default synonym property file
    * 
    */
   public AutorityRecordFileFinder() {
      this(null);
   }

   /**
    * Load the given synonym property file.
    * 
    * @param filePath Path to the property file
    */
   public AutorityRecordFileFinder(String filePath) {
      init(filePath);
   }

   @Override
   public void init(String filePath) {
      synonyms = model.loadPropertyFile(filePath);
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
      String synonymString = synonyms.getProperty(recordId);
      if (synonymString == null) {
         if (LOG.isDebugEnabled()) LOG.debug("Failed to resolve id:" + recordId);
         return null;
      }
      if (synonymString.isEmpty()) throw new AuthorityRecordException("Data error in synonym file at id:" + recordId);
      String[] entries = synonymString.split(seperator);
      AuthorityBean ret = new AuthorityBean();
      ret.id = recordId;
      ret.preferred = entries[0];
      if (entries.length > 1) {
         ArrayList<String> synonyms = new ArrayList<>(Arrays.asList(entries));  
         synonyms.remove(0);
         ret.synonyms = synonyms;
      }
      return ret;
   }


   /**
    * Minimal Test and usage example
    * 
    * @param args cmd line parameters
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   public static void main(String[] args) throws AuthorityRecordException {
      AutorityRecordFileFinder me = new AutorityRecordFileFinder("test/data/GndSynonyms.prop");
      AuthorityBean data = me.getAuthorityBean(args[0]);
      System.out.println(data.toString());
   }

}
