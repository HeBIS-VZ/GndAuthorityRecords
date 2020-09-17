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
package data;

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

   public static URI getURI4Geographic() {
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

   
   private static URI url2uri(URL resource) {
      try {
         return resource.toURI();
      } catch (URISyntaxException e) {
         // Should never happen
         throw new RuntimeException("URI conversion error, while geting test data.");
      }
   }
   
   public static void main(String[] args) {
      System.out.println(TestData.getURI4Work());
   }
}
