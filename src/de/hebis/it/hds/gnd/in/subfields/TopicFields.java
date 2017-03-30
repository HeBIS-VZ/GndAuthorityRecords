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
 * Methods for 'topic' subfields
 * <dl>
 * <dt>Referenced definitions:
 * <dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class TopicFields {
   private final static Logger LOG = LogManager.getLogger(TopicFields.class);

   /**
    * Stores topic term from &lt;datafield tag="150"&gt;.<br>
    * Subfield '$a' into field (preferred)<br>
    * 
    * @param dataField The content of the data field
    */

   public static void headingTopicalTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.recordId + ": in method");
      dataField.storeValues("a", "preferred", false, null);
   }

   /**
    * Stores related terms from &lt;datafield tag="550"&gt;.<br>
    * Subfield '$0' into (schema:relatedIds)<br>
    * Subfield '$a' into (schema:related)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void tracingTopicalTerm(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.recordId + ": in method");
      dataField.storeValues("0", "relatedIds", true, "http://d-nb.info.*"); // dismiss redundant URI
      dataField.storeValues("a", "related", true, null);
   }

}
