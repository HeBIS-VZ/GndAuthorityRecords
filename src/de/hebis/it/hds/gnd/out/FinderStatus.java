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

/**
 * Berschreibung des Statuses der Verbindung zwischen dem {@link de.hebis.it.hds.gnd.out.Finder} und des dahinterligenden Stores.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-02-28 uh First try *
 */
public enum FinderStatus {
   /** Die Datenverbindung wird Initialisiert. */
   STARTING,
   /** Die Datenverbindung ist etabliert. */
   READY,
   /** Es liegt ein temporäres Problem vor */
   WAITING,
   /** Es besteht ein Grundlegendes Problem. Es können kein Normdaten fgefunden werden. */
   DOWN;
}
