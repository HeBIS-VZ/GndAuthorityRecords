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

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Indicates a problem while retrieving data from repository
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-05-11 uh initial *
 */
public class AuthorityRecordException extends IOException {

   private static final long serialVersionUID = 1L;
   private static Logger     LOG              = LogManager.getLogger(AuthorityRecordException.class);
   private static final String myMsg = "Error in connection to repository: ";

   /**
    * Indicates a problem while retrieving data from repository
    * 
    * @param msg The own errormessage
    * @param e The source of the raised error
    */
   public AuthorityRecordException(String msg, Exception e) {
      super(msg, e);
      LOG.warn(myMsg + msg, e);
   }

   /**
    * Indicates a problem while retrieving data from repository
    * 
    * @param msg The own errormessage
    */
   public AuthorityRecordException(String msg) {
      super(msg);
      LOG.warn(myMsg + msg);
   }
}
