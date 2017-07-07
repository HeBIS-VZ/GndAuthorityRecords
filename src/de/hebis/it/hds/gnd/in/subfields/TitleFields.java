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
 * Methods for 'title' subfields
 * <dl>
 * <dt>Referenced definitions:</dt>
 * <dd>Basics: <a href="https://www.loc.gov/marc/authority/">LOC: MARC 21 Format for Authority Data</a></dd>
 * <dd>Extentions: "Normdaten (GND)" at <a href="http://www.dnb.de/DE/Standardisierung/Formate/MARC21/marc21_node.html">DNB: MARC 21</a></dd>
 * </dl>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 28.04.2017 uh initial
 */
public class TitleFields {
   private final static Logger LOG = LogManager.getLogger(TitleFields.class);

   /**
    * Title term &lt;datafield tag="130"&gt;.<br>
    * see: {@link GenericFields#heading(DataField)}
    * 
    * @param dataField The content of the data field
    */

   public static void headingTitle(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      GenericFields.heading(dataField);
   }

   /**
    * Alternative title &lt;datafield tag="430"&gt;.<br>
    * see: {@link GenericFields#tracing(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void tracingTitle(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      GenericFields.tracing(dataField);
   }

   /**
    * Related title &lt;datafield tag="530"&gt;.<br>
    * see: {@link GenericFields#related(DataField)}
    * 
    * @param dataField The content of the data field
    */
   public static void relatedTitle(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      GenericFields.related(dataField);
   }

   /**
    * Alternative titles in other systems &lt;datafield tag="730"&gt;.<br>
    * see: {@link GenericFields#linkingEntry(DataField, String)}
    * 
    * @param dataField The content of the data field
    */
   public static void linkingEntryTitle(DataField dataField) {
      if (LOG.isTraceEnabled()) LOG.trace(dataField.getRecordId() + ": in method");
      GenericFields.linkingEntry(dataField, null);
   }
}
