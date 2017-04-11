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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Einfache Bean zur Representation eines Normdatensatzes.<br>
 * Aus Performancegründen wurde darauf verzichtet die Bean explizit inmutable zu machen.<br>
 * Es sollte in den aufrufenden Klassen darauf verzichtet werden die ausgegebenen Objekte zu verändern.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-02-28 uh First try *
 */
public class GndBean implements GndBeanInterface {
   private String             myId;
   private String             myPreferred;
   private Collection<Object> mySynonyms;
   private Collection<Object> myRelations;

   /**
    * Im Konstruktor werden alle Werte voreingestellt.<br>
    * Eine spätere Änderung ist nicht vorgesehen/erwünscht aber nicht explizit verhindert.
    * 
    * @param id The GND-ID
    * @param preferred The prefered notation
    * @param synonyms Alternative notations
    * @param relations Related objects
    */
   public GndBean(String id, String preferred, Collection<Object> synonyms, Collection<Object> relations) {
      if (id == null) throw new NullPointerException("Die Id ist ein Pflichtparameter");
      myId = id;
      myPreferred = (preferred == null) ? "" : preferred;
      mySynonyms = (synonyms == null) ? new ArrayList<>() : synonyms;
      myRelations = (relations == null) ? new ArrayList<>() : relations;
   }

   @Override
   public String getId() {
      return myId;
   }

   @Override
   public String getPreferred() {
      return myPreferred;
   }

   @Override
   public Collection<Object> getSynonyms() {
      return mySynonyms;
   }

   @Override
   public Collection<Object> getRelations() {
      return myRelations;
   }

}
