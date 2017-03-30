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

import java.util.Collection;

/**
 * Initialer Container für denormalisierte Normdaten.<br/>
 * Es werden nicht alle Attribute von der Normdaten abgedeckt. Der Container kann bei Berarf erweitert werden.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-02-28 uh First try
 *
 */
public interface GndBeanInterface {
   /**
    * Gibt die ID des Normdatensatzes aus.
    * 
    * @return Derzeit nur die GND-ID in der Form (DE-588)<id>
    */
   public String getId();

   /**
    * Gibt die bevorzugte Schreibweise des Normdatensatzes aus.
    * 
    * @return Bevorzugte Scheibweise
    */
   public String getPreferred();

   /**
    * Gibt Synonyme zur bevorzugten Schreibweise des Normdatensatzes aus.
    * 
    * @return Liste der Synonyme
    */
   public Collection<Object> getSynonyms();

   /**
    * Gibt relationierte Begriffe des Normdatensatzes aus.<br>
    * Hier ohne Bezeichner weil die für die Suche eher unwichtig sind.<br/>
    * Mit den Einträgen könnte ein zusätzliches Suchfeld gefüllt werden.
    * 
    * @return Liste der relationierten Begriffe
    */
   public Collection<Object> getRelations();
}
