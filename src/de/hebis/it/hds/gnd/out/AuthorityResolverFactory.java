package de.hebis.it.hds.gnd.out;

import de.hebis.it.hds.gnd.out.resolver.OfflineAuthorityResolver;
import de.hebis.it.hds.gnd.out.resolver.AuthorityResolver;
import de.hebis.it.hds.gnd.out.resolver.OnlineAuthorityResolver;

public class AuthorityResolverFactory {

   public enum Modus {
      online,
      offline
   }
   
   private AuthorityResolverFactory() {
     // NeverEver
   }
   
   public static AuthorityResolver getResolver(Modus mode) {
      return (mode == Modus.online) ? new OnlineAuthorityResolver() : new OfflineAuthorityResolver();  
   }
}
