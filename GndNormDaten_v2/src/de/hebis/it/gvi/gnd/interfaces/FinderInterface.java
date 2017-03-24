package de.hebis.it.gvi.gnd.interfaces;

/**
 * Definition der minimal vorhanden Methoden eines GND-Store. Die hier vorgegebenen Methoden sind threadsicher zu implementieren.
 * 
 * @author uwe
 *
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
    * Bei nicht trivialen Implementierungen kann es sein, dass die GND noch nicht oder gerade nicht verfügbar sind.<br/>
    * Die Enummeration Status informiert über den aktuellen Zustand des Store.
    * 
    * @return Der aktuelle Statuscode wird ausgegeben. Der ausgegebene Status ist eine Momentaufname. Es ist also<br/>
    *         nicht garantiert, dass der Status zum letzten oder Nächsten Aufruf von {@link #getGndBean(String)} passt.<br/>
    *         Eine Verbale Beschreibung des Status kann mit {@link #getStatusText()} erfragt werden.
    */
   public FinderStatus getStatus();

   /**
    * Als Ergänzung zu {@link #getStatus()} kann mit Dieser Methode der Status als Text ausgegeben werden.
    * 
    * @return Die Beschreibung des aktuellen Status wird ausgegeben. Der Text kan dabei auf die Besonderheiten der Implementierung eingehen<br/>
    *         Der Status ist dabei eine Momentaufname und musss nicht immer zu, letzen/nächsten Aufruf von {@link #getStatus()} passen.
    */
   public String getStatusText();

}
