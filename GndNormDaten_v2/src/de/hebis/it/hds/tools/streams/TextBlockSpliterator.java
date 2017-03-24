package de.hebis.it.hds.tools.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hilfsklasse zum Mappen der Datenblöcke eines Textes (Stream<String>) in einen
 * paralellisierbaren Stream der Blöcke Stream<List<String>>.
 * TODO - ACHTUNG. Diese Klasse geht von der Vereinfachun aus, dass die Kennzeichen eines Blockendes
 * NIE in der selben Zeile stehen wie die Zeichenkette, die einen neuen Block anzeigt.  
 * 
 * @author Uwe 23.10.2014
 *
 */
public class TextBlockSpliterator implements Spliterator<List<String>> {
   static final Logger                LOG      = LogManager.getLogger(TextBlockSpliterator.class);
   private final Spliterator<String> source;
   private final Predicate<String>   start, end;
   private final Consumer<String>    getBlock;
   private List<String>              block;

   /**
    * Neuen ChunkSpliterator instanzieren
    * 
    * @param lineSpliterator zu verarbeitender Stream<String>
    * @param chunkStart Pattern zum Erkennen der ersten Zeile eines Blocks
    * @param chunkEnd Pattern zum Erkennen der letzten Zeile eines Blocks
    */
   TextBlockSpliterator(Spliterator<String> lineSpliterator, Predicate<String> chunkStart, Predicate<String> chunkEnd) {
      source = lineSpliterator;
      start = chunkStart;
      end = chunkEnd;
      getBlock = s -> {
         if (block == null) {
            if (start.test(s)) {
               if (LOG.isTraceEnabled()) LOG.trace("start: " + s);
               block = new ArrayList<>();
               block.add(s);
            }
            else {
               if (LOG.isTraceEnabled()) LOG.trace("noise:" + s);
            }
         }
         else {
            if (LOG.isTraceEnabled()) LOG.trace("newln: " + s);
            block.add(s);
         }
      };
   }

   @Override
   public boolean tryAdvance(Consumer<? super List<String>> action) {
      // Warten bis die Endbedingung eines Blocks erfüllt ist.
      while (block == null || block.isEmpty() || !end.test(block.get(block.size() - 1)))
         if (!source.tryAdvance(getBlock)) return false;
      // Zusammegesammelte Zeilen zur Verarbeitung weiterleiten und Spliterator zurücksetzen.
      action.accept(block);
      block = null;
      return true;
   }

   @Override
   public Spliterator<List<String>> trySplit() {
      return null;
   }

   @Override
   public long estimateSize() {
      return Long.MAX_VALUE;
   }

   @Override
   public int characteristics() {
      return ORDERED | NONNULL;
//      return  CONCURRENT | IMMUTABLE | NONNULL;
   }

   /**
    * Factory für einen ChunkSpliterator
    * 
    * @param lines Der zu consumierende Stream<String>
    * @param chunkStart Pattern zum Erkennen der ersten Zeile eines Blocks
    * @param chunkEnd Pattern zum Erkennen der letzten Zeile eines Blocks
    * @param parallel Anzeige ob die Ausgabe paralellisert weiterverarbeitet werden darf
    * @return Listen mit den Zeilen der erkannten Blöcke 
    */
   public static Stream<List<String>> toBlocks(Stream<String> lines, Predicate<String> chunkStart, Predicate<String> chunkEnd, boolean parallel) {
      return StreamSupport.stream(new TextBlockSpliterator(lines.spliterator(), chunkStart, chunkEnd), parallel);
   }
}
