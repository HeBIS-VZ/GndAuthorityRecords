package de.hebis.it.gvi.gnd.interfaces;

import java.util.Collection;

/**
 * Initialer Container für denormalisierte Normdaten.<br/>
 * Es werden nicht alle Attribute von der Normdaten abgedeckt. Der Container kann bei Berarf erweitert werden.
 * @author Uwe 
 * 08.03.2017
 *
 */
public interface GndBeanInterface {
   /**
    * Gibt die ID des Normdatensatzes aus. 
    * @return Derzeit nur die GND-ID in der Form (DE-588)<id>
    */
   public String getId();
   /**
    * Gibt die bevorzugte Schreibweise des Normdatensatzes aus.
    * @return Bevorzugte Scheibweise
    */
   public String getPreferred();
   /**
    * Gibt Synonyme zur bevorzugten Schreibweise des Normdatensatzes aus.
    * @return Liste der Synonyme
    */
   public Collection<Object> getSynonyms();
   /**
    * Gibt relationierte Begriffe des Normdatensatzes aus.<br> 
    * Hier ohne Bezeichner weil die für die Suche eher unwichtig sind.<br/>
    * Mit den Einträgen könnte ein zusätzliches Suchfeld gefüllt werden.
    * @return Liste der relationierten Begriffe
    */
   public Collection<Object> getRelations();
}
