/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.recur;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;

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
