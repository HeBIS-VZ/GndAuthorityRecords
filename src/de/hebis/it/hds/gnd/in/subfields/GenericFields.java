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
 * Common methods for other subfield classes
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 26.04.2017 uh initial
 */
public class GenericFields {
   private final static Logger LOG = LogManager.getLogger(GenericFields.class);

   /**
    * Main term<br>
    * Subfield '$a' into field (preferred)<br>
    * 
    * @param dataField The content of the data field
    */

   public static void heading(DataField dataField) {
      dataField.storeValues("a", "preferred", false, null);
   }

   /**
    * Complex See Reference<br>
    * Subfield '$0' into (schema:relatedIds)<br>
    * Subfield '$a' into (schema:related)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void complexSeeReference(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("0", "seeAlso", true, "https?://d-nb.info.*"); // dismiss redundant URI
      dataField.storeValues("a", "synonyms", true, null);
   }

   /**
    * Alternatives<br>
    * Subfield '$a' into (schema:synonyms)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void tracing(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("a", "synonyms", true, null);
   }

   /**
    * Related terms<br>
    * Subfield '$0' into (schema:relatedIds)<br>
    * Subfield '$a' into (schema:related)<br>
    * 
    * @param dataField The content of the data field
    */
   public static void related(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      dataField.storeValues("0", "relatedIds", true, "https?://d-nb.info.*"); // dismiss redundant URI
      dataField.storeValues("a", "related", true, null);
   }

   /**
    * Alternative names in other systems<br>
    * Subfield '$a' is taken as alias. (schema:synonyms)<br>
    * 
    * @param dataField The content of the data field
    * @param filterpattern Optional regex to remove noise
    */
   public static void linkingEntry(DataField dataField, String filterPattern) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      String altName = dataField.getFirstValue("a");
      if (altName == null) return; // w/o a  name we need no id
      if (filterPattern != null) altName = altName.replaceAll("%DE.*", "");
      dataField.storeMultiValued("synonyms", altName);
      dataField.storeValues("0", "sameAs", true, "http.+"); // no URLs

   }
}
