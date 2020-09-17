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
package de.hebis.it.hds.gnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Model bean as singleton
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-05-12 uh initial version
 */
public class Model extends Properties {
   private static final long   serialVersionUID = 1L;
   private final static Logger LOG              = LogManager.getLogger(Model.class);
   private static Model        singleton        = null;
   private static final String configFileName   = "AutorityRecord.properties";
   private static final String KEY_ConfigDir    = "gnd.configdir";
   private static final String KEY_ConfigFile   = "gnd.configfile";
   private String              myConfigDir      = "unset";
   private String              myConfigFile     = "unset";

   /**
    * Private constructor, to avoid multiple instances. <br>
    * A reference can obtained from the factory {@link #getModel()}
    */
   private Model() {
      super();
      try {
         readConfigFile();
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Sh*t executable JARs.", e);
      }
      LOG.debug("The model is created and initialized.");
   }

   /**
    * Factory to build exact on instance (Singelton)
    * 
    * @return The reference to the singelton.
    */
   public static synchronized Model getModel() {
      if (singleton == null) {
         LOG.debug("Build new instance.");
         singleton = new Model();
      }
      LOG.trace("Return the reference.");
      return singleton;
   }

   /**
    * Read the configuration file.
    * 
    * @throws FileNotFoundException
    */
   private void readConfigFile() throws FileNotFoundException {
      InputStream configStream = null;
      myConfigFile = System.getProperty(KEY_ConfigFile);
      myConfigDir = System.getProperty(KEY_ConfigDir);
      if (myConfigFile != null) {
         if (LOG.isDebugEnabled()) LOG.debug("Try to load " + myConfigFile + ". (defined by '-Dgnd.configfile=...' in comandline");
         configStream = new FileInputStream(myConfigFile);
      } else {
         LOG.debug("No '-Dgnd.configfile=...' defined. Look for '-Dgnd.configdir=...'");
         if (myConfigDir != null) {
            myConfigFile = myConfigDir.trim() + File.separator + configFileName;
            if (LOG.isDebugEnabled()) LOG.debug("Try to load " + myConfigFile + ". (defined by '-Dgnd.configdir=...' and constant: " + configFileName);
            configStream = new FileInputStream(myConfigFile);
         } else {
            LOG.warn("Neither '-Dgnd.configfile' nor '-Dgnd.configdir' is defined trying to load config as recource.");
            configStream = ClassLoader.getSystemClassLoader().getResourceAsStream(configFileName);
         }
      }
      if (configStream == null) {
         throw new FileNotFoundException("Can't find config file \"" + myConfigFile + "\".");
      }
      if (LOG.isTraceEnabled()) LOG.trace("Found: \"" + myConfigFile + "\".");
      try {
         this.load(new InputStreamReader(configStream));
      } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException("Error while loading: \"" + myConfigFile + "\".", e);
      }
      LOG.debug("Config file sucsessfully loaded");
      if (LOG.isTraceEnabled()) {
         for (Object key : this.keySet()) {
            LOG.trace("Config param: " + key + " = " + this.getProperty((String) key));
         }
      }
   }

   public Properties loadPropertyFile(String file) {
      String fileToLoad = mapResource(file);
      Properties ret = new Properties();
      if ((fileToLoad == null) || fileToLoad.trim().isEmpty()) fileToLoad = getProperty("PropertyFilePath");
      if ((fileToLoad == null) || fileToLoad.trim().isEmpty()) throw new RuntimeException("Name/path of synonym file is missing.");
      if ((myConfigFile != null) && !fileToLoad.contains(File.separator)) fileToLoad = myConfigDir.trim() + File.separator + fileToLoad;
      try {
         if (LOG.isTraceEnabled()) LOG.trace("try to load property file: \"" + fileToLoad + "\".");
         ret.load(new InputStreamReader(new FileInputStream(fileToLoad)));
      } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException("Error in loading: \"" + fileToLoad + "\".", e);
      }
      if (LOG.isDebugEnabled()) LOG.debug("Property file: \"" + fileToLoad + "\" sucsessfully loaded");
      return ret;
   }
   
   /**
    * Convert a recource zu its file path.<br>
    * If a file path was given, return just this. 
    * @param file
    * @return
    */
   private String mapResource(String file) {
      URL otto = System.class.getResource(file);
      if (otto != null) {
         return otto.getPath();
      }
      return file;
      
      
   }

   public static void main(String[] unused) {
      Model me = Model.getModel();
      System.out.println(me.toString());
   }
}
