package de.hebis.it.hds.tools.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WaitingNamedExecutorService implements ThreadFactory {
   private static final Logger        LOG                  = LogManager.getLogger(WaitingNamedExecutorService.class);
   private static final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
   private String                     myName               = "unset";
   private BlockingQueue<Runnable>    queue                = null;
   private ExecutorService            executor             = null;

   /**
    * Wartender Wrapper zu {@link java.util.concurrent.ExecutorService}. Bei voller Warteschlange wird mit der Ausführung gewartet. Es wird nur eine Subset der Methoden unterstützt
    * 
    * @param executorName Eine Kennung für diese Instantanz
    * @param queuelength Länge der vorgelagerten Warteschlange
    * @param paralelthreads Größe der Executors
    */
   public WaitingNamedExecutorService(String executorName, int queuelength, int paralelthreads) {
      if (executorName != null) myName = executorName;
      if (queuelength < 1) queuelength = 1;
      if (paralelthreads < 1) paralelthreads = 1;
      queue = new ArrayBlockingQueue<>(queuelength);
      executor = new ThreadPoolExecutor(paralelthreads, paralelthreads, 10, TimeUnit.HOURS, queue, this);
   }

   /**
    * Neues Runnable aufnehmen.
    * 
    * @param task
    */
   public synchronized void execute(Runnable task) {
      if (waitOnQueue()) {
         if (LOG.isTraceEnabled()) LOG.trace(myName + ": Callable \"" + task.toString() + "\" wird in die Warteschlange eingetragen.");
         try {
            executor.execute(task);
         } catch (Exception e) {
            LOG.warn(myName + "(execute): Fehler \"" + e.toString() + "\" Versuche Wiederholung.");
            execute(task);
         }
         return;
      }
      LOG.warn("Callable:" + task + "konnte nicht im ThreadPool aufgenommen werden.");
   }

   /**
    * Neues Callable aufnehmen.
    * 
    * @param task
    * @return Der Rückgabewert des Callable oder NULL wenn das Callable nicht aufgenommen werden konnte.
    */
   public synchronized <T> Future<T> submit(Callable<T> task) {
      if (waitOnQueue()) {
         if (LOG.isTraceEnabled()) LOG.trace(myName + ": Callable \"" + task.toString() + "\" wird in die Warteschlange eingetragen.");
         try {
            return executor.submit(task);
         } catch (Exception e) {
            LOG.warn(myName + "(submit): Fehler \"" + e.toString() + "\" Versuche Wiederholung.");
            return submit(task);
         }
      }
      LOG.warn("Callable:" + task + "konnte nicht im ThreadPool aufgenommen werden.");
      return null;
   }

   /**
    * Den ExecutorService schließen. im Gegensatzu zu {@link java.util.concurrent.ExecutorService#shutdown} wird hier gewartet bis alle Tasks abgearbeitet sind.
    * 
    */
   public synchronized void shutdown() {
      if (LOG.isDebugEnabled()) LOG.debug(myName + " wird geschlossen. Auf die Abarbeitung der Runnables wid gewartet.");
      executor.shutdown();
      // Zuerst Warten bis die Warteschlange leer ist.
      while (queue.size() > 0) {
         try {
            if (LOG.isTraceEnabled()) LOG.trace(myName + ": Beende die Auftragsliste: Die Warteschlange enthält noch " + queue.size() + " Einträge. Eine Sekunde wird gewartet");
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            executor.shutdownNow();
            LOG.warn(myName + "(shutdown): Sofortiger Abbruch wegen Interupt");
            return;
         }
      }
      int count = 0;
      while (!executor.isTerminated()) {
         try {
            if (LOG.isInfoEnabled()) LOG.info(myName + ": Warte seit " + (count++ * 10) + " Sekunden auf die Beendigung der letzten Threads");
            executor.awaitTermination(10, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
            executor.shutdownNow();
            LOG.warn(myName + "(shutdown): Sofortiger Abbruch wegen Interupt");
            return;
         }
      }
   }

   /**
    * Sicherstellen, dass die Warteschlange der ExecutorService nicht voll ist.
    * 
    * @return TRUE wenn der Wartevorgang nicht durch einen Interrupt unterbrochen wurde.
    */
   private boolean waitOnQueue() {
      if (executor.isShutdown() || executor.isTerminated()) return false;
      if (queue.remainingCapacity() > 0) return true;
      while (queue.remainingCapacity() < 1) {
         try {
            if (LOG.isTraceEnabled()) LOG.trace(myName + ": Die Warteschlange ist zu lang (" + queue.size() + ").  Es wird zwei Sekunden gewartet.");
            Thread.sleep(2000);
         } catch (InterruptedException e) {
            LOG.warn(myName + ": Das Warten auf eine Platz in der Warteschlange wegen einem Interrupt abgebrochen.");
            return false;
         }
      }
      return true;
   }

   /**
    * Für das neue Runnable 'task' wird ein vom Standard abweichender Name generiert. Anstelle des vom {@link Executors.defaultThreadFactory()} Namensschema ('pool-#-thread-#') wird der Namen aus dem
    * Namen der Instanz und der Zählung gebildet. ('executorname-#') bis zur Zählung der Threads ersetzt. (-> "poolname-#") Die Methode implementiert das Interface {@link #ThreadFactory}.
    *
    * @param task
    */
   @Override
   public Thread newThread(Runnable task) {
      Thread newThread = defaultThreadFactory.newThread(task);
      newThread.setName(newThread.getName().replaceFirst("pool-\\d+-thread", myName));
      return newThread;
   }

}
