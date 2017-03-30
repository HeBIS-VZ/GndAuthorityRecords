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
package de.hebis.it.gvi.hds.gnd.out;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import de.hebis.it.hds.gnd.out.GndBean;

/**
 * Bereitstellung von Beispieldaten für die Tests
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-03-15 uh First try
 *
 */
public class ExampleSolrDocs {
   // Gekürzte Norndatensätze als GndBeans
   private static final GndBean                dummy     = new GndBean("dummy", "Testdata", collect("Beispiel", "Unfug"), null);
   private static final GndBean                goethe    = new GndBean("(DE-588)118540238", "Goethe, Johann Wolfgang", collect("Goethe, J. W. v.", "Göte, Iogann V.", "Ǧūta, Yūhān Wulfǧānǧ fūn"),
         collect("Schiller, Friedrich", "Goethe, Cornelia", "Stein, Charlotte"));
   private static final GndBean                tucholski = new GndBean("(DE-588)11862444X", "Tucholsky, Kurt",
         collect("Tucholʹskij, Kurt", "Tûkôlsqî, Qûrṭ", "Old Shatterhand", "Tiger, Theobald", "Panter, Peter"), null);
   private static final GndBean                orwell    = new GndBean("(DE-588)118590359", "Orwell, George", collect("Blair, Eric Arthur", "Ārvel, Jārg", "Orvell, Džordž"),
         collect("Blair, Richard"));
   /**
    * Die verkürzte Beispieldaten zum Einpielen in einen Testindex.<br/>
    * goethe:(DE-588)118540238, tucholski:(DE-588)11862444X und orwell:(DE-588)118590359
    */
   public static final List<SolrInputDocument> examples  = convertBeans2Docs();

   /**
    * Kleine Helfer für die statische Initialisierung
    */
   private static Collection<Object> collect(Object... einträge) {
      return Arrays.asList(einträge);
   }

   private static List<SolrInputDocument> convertBeans2Docs() {
      List<SolrInputDocument> ret = new ArrayList<>();
      ret.add(convert(dummy));
      ret.add(convert(goethe));
      ret.add(convert(tucholski));
      ret.add(convert(orwell));
      return ret;
   }

   private static SolrInputDocument convert(GndBean data) {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField("id", data.getId());
      doc.addField("fullrecord", "Dummy: No MarcData");
      doc.addField("preferred", data.getPreferred());
      for (Object syn : data.getSynonyms()) {
         doc.addField("synonyms", syn);
      }
      for (Object rel : data.getRelations()) {
         doc.addField("related", rel);
      }
      return doc;
   }
}
