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
 * Used to specify a sequence of dates or times from first value through and including the last value. The sequence may
 * represent years, or days of month or hours, etc. The method to which this class is passed will determine the meaning
 * of the integers within the list.
 * 
 * @author arodriguez
 */
@XmlRootElement
public class SequentialRecurrence implements ITimeRecurrence {
  Integer firstValue;
  Integer lastValue;

  /**
   * Creates a new sequence with a null first and last value
   */
  public SequentialRecurrence() {
  }

  /**
   * Creates a new sequence.
   * 
   * @param firstValue
   *          the first value in the sequence
   * @param lastValue
   *          the last value in the sequence
   */
  public SequentialRecurrence( Integer firstValue, Integer lastValue ) {
    this.firstValue = firstValue;
    this.lastValue = lastValue;
  }

  /**
   * Returns the first value in the sequence.
   * 
   * @return the first value in the sequence
   */
  public Integer getFirstValue() {
    return firstValue;
  }

  /**
   * Sets the first value in the sequence.
   * 
   * @param firstValue
   *          the first value in the sequence
   */
  public void setFirstValue( Integer firstValue ) {
    this.firstValue = firstValue;
  }

  /**
   * Returns the last value in the sequence.
   * 
   * @return the last value in the sequence
   */
  public Integer getLastValue() {
    return lastValue;
  }

  /**
   * Sets the last value in the sequence.
   * 
   * @param lastValue
   *          the last value in the sequence
   */
  public void setLastValue( Integer lastValue ) {
    this.lastValue = lastValue;
  }

}
