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

import de.hebis.it.hds.gnd.Model;

/**
 * Abstract Implementation of common methods of all AutorityRecord&lt;DATASOURCE&gt;Finder.<br>
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-06-02 uh initial version
 */
public abstract class AutorityRecordFinder {
   static final Model config = Model.getModel();

   /**
    * Initialize a new Finder and 'connect to' or 'load from' the data source.
    * 
    * @param configParamName The name of a parameter in the configuration file
    */
   abstract public void init(String configParamName);

   /**
    * get the data for the given id
    * 
    * @param recordId The complete Id mostly prefixed with a ISIL. Eg. '(DE-588)' for the GND
    * @return a authority bean representing the authority record or null if the id is unknown.
    * @throws AuthorityRecordException Indicates a problem while retrieving data from repository
    */
   abstract public AuthorityBean getAuthorityBean(String recordId) throws AuthorityRecordException;

}
