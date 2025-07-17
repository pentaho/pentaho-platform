/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.scheduler2.recur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Used to specify a list of recurrences representing dates or times. The list may represent years, or days of month or
 * hours, etc. The method to which this class is passed will determine the meaning of the integers within the list.
 * 
 * @author arodriguez
 */
@XmlRootElement
public class RecurrenceList implements ITimeRecurrence {
  private List<Integer> values = new ArrayList<Integer>();

  public RecurrenceList( Integer... values ) {
    this.values = Arrays.asList( values );
  }

  public RecurrenceList() {
  }

  public List<Integer> getValues() {
    return values;
  }
  @JsonProperty( "values" )
  public List<String> valuesAsStrings() {
    return values.stream()
      .map( String::valueOf )
      .collect( Collectors.toList() );
  }

  public void setValues( List<Integer> values ) {
    this.values = values;
  }

}
