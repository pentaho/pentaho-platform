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


package org.pentaho.platform.api.scheduler2.wrappers;


import org.pentaho.platform.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfWeek;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;

import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DayOfWeekWrapper extends ITimeWrapper {
  @XmlElementRefs( { @XmlElementRef( type = SequentialRecurrence.class ),
      @XmlElementRef( type = IncrementalRecurrence.class ), @XmlElementRef( type = RecurrenceList.class ),
      @XmlElementRef( type = QualifiedDayOfWeek.class ) } )
  @Override
  public List<ITimeRecurrence> getRecurrences() {
    return recurrences;
  }


}
