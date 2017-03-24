package de.hebis.it.gndexpander.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@SuppressWarnings("javadoc")
public class TestData {
   public static URI getURI4UpdateData() {
      return url2uri(TestData.class.getResource("oaiUpdate.xml"));
   }

   public static URI getURI4Redirect() {
      return url2uri(TestData.class.getResource("umlenk.mrc.xml"));
   }

   public static URI getURI4Organisation() {
      return url2uri(TestData.class.getResource("Tb.mrc.xml"));
   }

   public static URI getURI4Conference() {
      return url2uri(TestData.class.getResource("Tf.mrc.xml"));
   }

   public static URI getURI4Geografic() {
      return url2uri(TestData.class.getResource("Tg.mrc.xml"));
   }

   public static URI getURI4Person() {
      return url2uri(TestData.class.getResource("Tn.mrc.xml"));
   }

   public static URI getURI4IndividulizedPerson() {
      return url2uri(TestData.class.getResource("Tp.mrc.xml"));
   }

   public static URI getURI4Term() {
      return url2uri(TestData.class.getResource("Ts.mrc.xml"));
   }

   public static URI getURI4Work() {
      return url2uri(TestData.class.getResource("Tu.mrc.xml"));
   }

   // ToDO
   // public static URI getLocal() {
   // return url2uri(TestData.class.getResource("Tr.mrc.xml"));
   // }
   // public static URI getLibrary() {
   // return url2uri(TestData.class.getResource("Tw.mrc.xml"));
   // }
   
   private static URI url2uri(URL resource) {
      try {
         return resource.toURI();
      } catch (URISyntaxException e) {
         // Should never happen
         throw new RuntimeException("URI conversion error, while geting test data.");
      }
   }
}
