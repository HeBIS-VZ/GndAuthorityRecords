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
package de.hebis.it.hds.gnd.in.subfields;

import java.util.Arrays;

/**
 * Static methods as helper for the Tests
 *
 * @author Uwe Reh (uh), HeBIS-IT
 * @version 30.03.2017 uh initial
 */
public class TestHelper {
   
   /**
    * Builds new marc {@link DataField} from the parameters.
    * 
    * @param recordId Id for the record
    * @param testData NULL for a new {@link DataField} or one to reuse.
    * @param dataFieldId Id of the data field
    * @param subFieldId Id of the first subfield
    * @param subFieldValues One or more value(s) for the (repeatable) subfield
    * @return A new Dateifeld with one (repeatable) subfield;
    */
   public static DataField dataFieldFactory(String recordId, DataField testData, String dataFieldId, String subFieldId, String... subFieldValues) {
      testData = new DataField(recordId, testData);
      testData.put("tag", testData.newList(dataFieldId));
      testData.put("ind1", testData.newList(" "));
      testData.put("ind2", testData.newList(" "));
      testData.put(subFieldId, Arrays.asList(subFieldValues));
      return testData;
   }

   /**
    * Adds the provided subfield
    * 
    * @param field The data field to extend
    * @param subFieldId Id of the first subfield
    * @param subFieldValues One or more value(s) for the (repeatable) subfield
    * @return The provided data field with the added (repeatable) subfield;
    */
   public static DataField addSubField(DataField field, String subFieldId, String... subFieldValues) {
      field.put(subFieldId, Arrays.asList(subFieldValues));
      return field;
   }
}
