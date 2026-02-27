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
