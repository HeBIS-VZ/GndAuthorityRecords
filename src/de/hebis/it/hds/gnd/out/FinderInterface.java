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
 * Definition der minimal vorhanden Methoden eines GND-Store. Die hier vorgegebenen Methoden sind threadsicher zu implementieren.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-02-28 uh First try *
 */
public interface FinderInterface {
   /**
    * Holt aus dem Key/Value Store die Daten zur angefragten GND-ID
    * 
    * @param gndId Die GND-ID mit oder ohne vorangestelltes ISIL '(DE-599)'
    * @return Liefert die zur gndId passende Normdatenrepräsentation oder Null wenn die Id unbekannt ist.
    */
   public GndBeanInterface getGndBean(String gndId);

   /**
    * Bei nicht trivialen Implementierungen kann es sein, dass die GND noch nicht oder gerade nicht verfügbar sind.<br>
    * Die Enummeration Status informiert über den aktuellen Zustand des Store.
    * 
    * @return Der aktuelle Statuscode wird ausgegeben. Der ausgegebene Status ist eine Momentaufname. Es ist also<br>
    *         nicht garantiert, dass der Status zum letzten oder Nächsten Aufruf von {@link #getGndBean(String)} passt.<br>
    *         Eine Verbale Beschreibung des Status kann mit {@link #getStatusText()} erfragt werden.
    */
   public FinderStatus getStatus();

   /**
    * Als Ergänzung zu {@link #getStatus()} kann mit Dieser Methode der Status als Text ausgegeben werden.
    * 
    * @return Die Beschreibung des aktuellen Status wird ausgegeben. Der Text kan dabei auf die Besonderheiten der Implementierung eingehen<br>
    *         Der Status ist dabei eine Momentaufname und muss nicht immer zu, letzen/nächsten Aufruf von {@link #getStatus()} passen.
    */
   public String getStatusText();

}
