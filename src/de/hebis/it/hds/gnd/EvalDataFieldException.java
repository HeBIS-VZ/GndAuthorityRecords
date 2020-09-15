/**
 * 
 */
package de.hebis.it.hds.gnd;

/**
 * Simplified own exception to raise detected errors while evaluating the given data.
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 15.09.2020 uh initial 
 */
public class EvalDataFieldException extends Exception {

   private static final long serialVersionUID = 1L;

   /**
    * @param message
    */
   public EvalDataFieldException(String message) {
      super(message);
      // TODO Auto-generated constructor stub
   }

   /**
    * @param cause
    */
   public EvalDataFieldException(Throwable cause) {
      super(cause);
      // TODO Auto-generated constructor stub
   }

   /**
    * @param message
    * @param cause
    */
   public EvalDataFieldException(String message, Throwable cause) {
      super(message, cause);
      // TODO Auto-generated constructor stub
   }
}
