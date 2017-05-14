/*
 * Copyright 2016, 2017 by HeBIS (www.hebis.de).
 * 
 * This file is part of HeBIS HdsToolkit.
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
package de.hebis.it.hds.gnd.out;

import java.util.List;

/**
 * Simplified template to read the authority records.
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 11.05.2017 uh initial
 */
import org.apache.solr.client.solrj.beans.Field;

public class AuthorityBean {

   /** The unique Id of the authority record. (mandatory) */
   @Field("id")
   public String       id;

   /** The one preferred textual representation. (mandatory) */
   @Field("preferred")
   public String       preferred;

   /** Alternative textual representation. (optional) */
   @Field("synonyms")
   public List<String> synonyms;

   /** DDC notation with trust level. (optional) <br>
    * The entries are formated as <digit>:<ddc notation> The Digit [1..4] indicates the trust level. <br>
    * '4' indicates a perfect match if the ddc notation. <br>
    * '1' indicates only a weak match.  */
   @Field("ddc")
   public List<String> ddc;

   /** The unchanged authority record. (mandatory) */
   @Field("fullrecord")
   public String       fullRecord;
   
   /** 
    * Human readable overview of the retrieved data 
    * 
    */
   @Override
   public String toString() {
      StringBuilder ret = new StringBuilder("AuthorityBean: ");
      ret.append(id);
      ret.append("=\"");
      ret.append(preferred);
      ret.append("=\"");
      if ((synonyms != null) && !synonyms.isEmpty()) {
         ret.append("; synonyms=");
         ret.append(synonyms.toString());
      }
      if ((ddc != null) && !ddc.isEmpty()) {
         ret.append("; ddc=");
         ret.append(ddc.toString());
      }
      return ret.toString();
   }
}
