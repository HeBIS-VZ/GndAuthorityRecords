package de.hebis.it.hds.gnd.out;

import de.hebis.it.hds.gnd.out.resolver.OfflineAuthorityResolver;
import de.hebis.it.hds.gnd.Model;
import de.hebis.it.hds.gnd.out.resolver.AuthorityResolver;
import de.hebis.it.hds.gnd.out.resolver.OnlineAuthorityResolver;

public class AuthorityResolverFactory {
   private static final Model         model      = Model.getModel();


   public enum Modus {
      online,
      offline
   }
   
   private AuthorityResolverFactory() {
     // NeverEver
   }
   
   public static AuthorityResolver getResolver() {
      return ("online".equals(model.getProperty("GndMode", "offline"))) ? new OnlineAuthorityResolver() : new OfflineAuthorityResolver();  
   }
   
   public static AuthorityResolver getResolver(Modus mode) {
      return (mode == Modus.online) ? new OnlineAuthorityResolver() : new OfflineAuthorityResolver();  
   }
}
