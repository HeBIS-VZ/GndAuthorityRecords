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

/**
 * Full template to read the authority records. <br>
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 11.05.2017 uh initial
 */
public class AuthorityCompleteBean extends AuthorityBean {

   /** This authority record was changed as N'th modification in the index. (automatic) */
   @Field("_version_")
   public String       version;

   /** The date when the record was last changed. (default= NOW) */
   @Field("index_date")
   public String       indexDate;

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
    * </ul>
    */
   @Field
   public String       authorityType;

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

}
