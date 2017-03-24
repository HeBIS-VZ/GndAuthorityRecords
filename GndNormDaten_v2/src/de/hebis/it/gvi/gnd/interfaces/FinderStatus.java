package de.hebis.it.gvi.gnd.interfaces;

/**
 * Berschreibung des Statuses der Verbindung zwischen dem {@link de.hebis.it.gvi.gnd.Finder} und des dahinterligenden Stores.
 * @author uwe
 *
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
