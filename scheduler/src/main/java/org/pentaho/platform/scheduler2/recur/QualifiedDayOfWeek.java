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

@XmlRootElement
public class QualifiedDayOfWeek implements ITimeRecurrence {
  public enum DayOfWeek {
    SUN, MON, TUE, WED, THU, FRI, SAT
  };

  public enum DayOfWeekQualifier {
    FIRST, SECOND, THIRD, FOURTH, FIFTH, LAST
  };

  DayOfWeekQualifier qualifier;
  DayOfWeek dayOfWeek;

  public QualifiedDayOfWeek() {
  }

  public QualifiedDayOfWeek( DayOfWeekQualifier qualifier, DayOfWeek dayOfWeek ) {
    this.qualifier = qualifier;
    this.dayOfWeek = dayOfWeek;
  }

  public QualifiedDayOfWeek( Integer qualifier, Integer dayOfWeek ) {
    this.qualifier = DayOfWeekQualifier.values()[( qualifier.intValue() - 1 ) % 5];
    this.dayOfWeek = DayOfWeek.values()[( dayOfWeek.intValue() - 1 ) % 7];
  }

  public DayOfWeekQualifier getQualifier() {
    return qualifier;
  }

  public void setQualifier( DayOfWeekQualifier qualifier ) {
    this.qualifier = qualifier;
  }

  public DayOfWeek getDayOfWeek() {
    return dayOfWeek;
  }

  public void setDayOfWeek( DayOfWeek dayOfWeek ) {
    this.dayOfWeek = dayOfWeek;
  }

  public String toString() {
    String aString = ""; //$NON-NLS-1$
    if ( ( getQualifier() != null ) && ( getDayOfWeek() != null ) ) {
      if ( getQualifier() == DayOfWeekQualifier.LAST ) {
        aString = Integer.toString( dayOfWeek.ordinal() + 1 ) + "L"; //$NON-NLS-1$
      } else {
        aString = Integer.toString( dayOfWeek.ordinal() + 1 ) + "#" + ( qualifier.ordinal() + 1 ); //$NON-NLS-1$
      }
    }
    return aString;
  }
}
