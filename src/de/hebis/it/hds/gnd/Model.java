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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

   /**
    * Private constructor, to avoid multiple instances. <br>
    * A reference can obtained from the factory {@link #getModel()}
    */
   private Model() {
      super();
      readConfigFile();
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
    */
   private void readConfigFile() {
      LOG.trace("Konfiguration einlesen");
      InputStream configStream = Model.class.getClassLoader().getResourceAsStream(configFileName);
      if (configStream != null) {
         try {
            this.load(new InputStreamReader(configStream));
         } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in loading: \"" + configFileName + "\".", e);
         }
         LOG.debug("Config file sucsessfully loaded");
         if (LOG.isTraceEnabled()) {
            for (Object key : this.keySet()) {
               LOG.trace("Konfig: " + key + " = " + this.getProperty((String) key));
            }
         }
      } else {
         throw new RuntimeException("Config file \"" + configFileName + "\" couldn't be found in class path.");
      }
   }
}
