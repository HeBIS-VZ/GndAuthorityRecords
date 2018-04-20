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

import java.util.Date;
import java.util.List;

/**
 * Simplified template to read the authority records.
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 11.05.2017 uh initial
 */
import org.apache.solr.client.solrj.beans.Field;

/**
 * Container for the retrieved authority records.
 * 
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 2017-05-11 uh first try
 * @version 2018-04-05 uh extended to complete set
 */
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

   /**
    * DDC notation with trust level. (optional) <br>
    * The entries are formated as &lt;digit&gt;:&lt;ddc notation&gt; The Digit [1..4] indicates the trust level. <br>
    * '4' indicates a perfect match if the ddc notation. <br>
    * '1' indicates only a weak match.
    */
   @Field("ddc")
   public List<String> ddc;

   /** This authority record was changed as N'th modification in the index. (automatic) */
   @Field("_version_")
   public long       version;

   /** The date when the record was last changed. (default= NOW) */
   @Field("index_date")
   public Date       indexDate;

   /** The format of the authority record in {@link AuthorityBean#fullRecord}. (mandatory) */
   @Field("recordtype")
   public String       recordType;

   /** The ISIL of the publisher of the authority record. (default = "DE-101") */
   @Field
   public String       source;

   /** Related authority records in textual representation. (optional) */
   @Field
   public List<String> related;

   /** Related authority records in textual representation. (optional) */
   @Field
   public List<String> relatedIds;

   /** Ids/URIs of other authority records describing the same content. (optional) */
   @Field
   public List<String> sameAs;

   /** Ids/URIs of other authority records describing the similar content. (optional) */
   @Field
   public List<String> seeAlso;

   /**
    * The type of the Authority record. coded in the scheme of the German National Library (optional)
    * <ul>
    * <li>p = individualized persons</li>
    * <li>n = not individualized persons</li>
    * <li>s = topics</li>
    * <li>b = organizations</li>
    * <li>f = meetings</li>
    * <li>p = geographic</li>
    * <li>u = titles</li>
    * <li>r = local defined</li>
    * <li>w = libraries</li>
    * <li>- = default for is not used</li>
    * </ul>
    */
   @Field
   public String       authorityType = "-";

   /**
    * The quality level of the authority record coded as digit. (optional) <br>
    * 1 means best, below 4 the quality may not guaranteed.
    */
   @Field
   public String       qualityLevel;

   /** Coordinates given in the authority record as points in WGS84. (optional) */
   @Field
   public List<String> coordinates;

   /**
    * This flag marks authority records with unresolved references to others. (default = false) <br>
    * Records with this flag set, need to be handled in a second pass.
    * <dl>
    * <dt>Motivation</dt>
    * <dd>A assumed name (pseudonym) contains a reference to the real name, but not to other assumed names. <br>
    * To obtain all synonyms, it's necessary to copy the synonyms of the 'real name' record.
    * <dd>
    * </dl>
    */
   @Field
   public boolean      look4me;

      
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
