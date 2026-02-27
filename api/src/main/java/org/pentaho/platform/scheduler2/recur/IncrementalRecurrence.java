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

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Used to specify incremental dates or times. For example an incremental recurrence with 1 as a starting value and 3 as
 * an increment translates to 1, 4, 7, etc. The time units may represent years, or days of month or hours, etc. The
 * method to which this class is passed will determine the meaning of the integers within the list.
 * 
 * @author arodriguez
 */
@XmlRootElement
public class IncrementalRecurrence implements ITimeRecurrence {
  String startingValue;
  Integer increment;

  /**
   * Creates a new incremental recurrence with a null starting value and increment
   */
  public IncrementalRecurrence() {

  }
  /**
   * Creates a new incremental recurrence.
   *
   * @param startingValue
   *          the starting value
   * @param increment
   *          the increment
   */
  public IncrementalRecurrence( Integer startingValue, Integer increment ) {
    this.startingValue = startingValue.toString();
    this.increment = increment;
  }

  /**
   * Creates a new incremental recurrence.
   * 
   * @param startingValue
   *          the starting value
   * @param increment
   *          the increment
   */
  public IncrementalRecurrence( String startingValue, Integer increment ) {
    // Starting value can either be a number of *
    try {
      Integer.parseInt(startingValue);
    } catch ( NumberFormatException nfe) {
      if(startingValue == null  || startingValue.isEmpty() || !startingValue.equals("*")) {
        throw new IllegalArgumentException("StartingValue can only be a integer or * ");
      }
    }
    this.startingValue = startingValue;
    this.increment = increment;
  }

  /**
   * Returns the starting value
   * 
   * @return the starting value
   */
  public String getStartingValue() {
    return startingValue;
  }

  /**
   * Sets the starting value
   * 
   * @param startingValue
   *          the starting value
   */
  public void setStartingValue( String startingValue ) {
    this.startingValue = startingValue;
  }

  /**
   * Returns the increment
   * 
   * @return the increment
   */
  public Integer getIncrement() {
    return increment;
  }

  /**
   * Sets the increment
   * 
   * @param increment
   *          the increment
   */
  public void setIncrement( Integer increment ) {
    this.increment = increment;
  }
}
