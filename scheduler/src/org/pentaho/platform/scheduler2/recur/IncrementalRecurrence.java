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
 * Used to specify incremental dates or times. For example an incremental recurrence with 1 as a starting value and 3 as
 * an increment translates to 1, 4, 7, etc. The time units may represent years, or days of month or hours, etc. The
 * method to which this class is passed will determine the meaning of the integers within the list.
 * 
 * @author arodriguez
 */
@XmlRootElement
public class IncrementalRecurrence implements ITimeRecurrence {
  Integer startingValue;
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
    this.startingValue = startingValue;
    this.increment = increment;
  }

  /**
   * Returns the starting value
   * 
   * @return the starting value
   */
  public Integer getStartingValue() {
    return startingValue;
  }

  /**
   * Sets the starting value
   * 
   * @param startingValue
   *          the starting value
   */
  public void setStartingValue( Integer startingValue ) {
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
