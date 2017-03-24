package de.hebis.it.gvi.gnd;

import java.util.ArrayList;
import java.util.Collection;

import de.hebis.it.gvi.gnd.interfaces.GndBeanInterface;

/**
 * Einfache Bean zur Representation eines Normdatensatzes.<br/>
 * Aus Performancegründen wurde darauf verzichtet die Bean explizit inmutable zu machen.<br/>
 * Es sollte in den aufrufenden Klassen darauf verzichtet werden die ausgegebenen Objekte zu verändern.
 * 
 * @author uwe
 *
 */
public class GndBean implements GndBeanInterface {
   private String       myId;
   private String       myPreferred;
   private Collection<Object> mySynonyms;
   private Collection<Object> myRelations;

   /**
    * Im Konstruktor werden alle Werte voreingestellt.<br/>
    * Eine spätere Änderung ist nicht vorgesehen/erwünscht aber nicht explizit verhindert.
    * 
    * @param id
    * @param preferred
    * @param synonymes
    * @param relations
    */
   public GndBean(String id, String preferred, Collection<Object> synonymes, Collection<Object> relations) {
      if (id == null) throw new NullPointerException("Die Id ist ein Pflichtparameter");
      myId = id;
      myPreferred = (preferred == null) ? "" : preferred;
      mySynonyms = (synonymes == null) ? new ArrayList<>() : synonymes;
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
