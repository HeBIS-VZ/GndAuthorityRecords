/*
 * Copyright 2016, 2017 by HeBIS (www.hebis.de).
 * 
 * This file is part of HeBIS HdsToolkit.
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
package de.hebis.it.hds.gnd.in.subfields;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods for persons
 * <dl>
 * <dt>Referenced definitions:
 * <dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 04.04.2017 uh initial
 */
public class PersonFields {
   private final static Logger LOG = LogManager.getLogger(PersonFields.class);
   /**
    * Alternative names in other systems &lt;datafield tag="700"&gt;.<br>
    * Subfield '$a' is taken as alias. (schema:synonyms)<br>
    * Optional trailing informations "ABC%DE3..." will be removed -> "ABC"
    * @param dataField 
    */
   public static void linkingEntryPersonalName(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      String altName = dataField.getFirstValue("a");
      dataField.storeMultiValued("synonyms", altName.replaceAll("%DE.*", ""));
      String altId = dataField.getFirstValue("0");
      dataField.storeMultiValued("sameAs", altId);
   }

}
