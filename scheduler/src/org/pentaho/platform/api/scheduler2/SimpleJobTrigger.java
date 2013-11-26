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

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple way of specifying a schedule on which a job will fire as opposed to {@link ComplexJobTrigger}. The
 * {@link SimpleJobTrigger} can meet your needs if you are looking for a way to have a job start, execute a set number
 * of times on a regular interval and then end either after a specified number of runs or at an end date.
 * 
 * @author aphillips
 */
@XmlRootElement
public class SimpleJobTrigger extends JobTrigger implements Serializable {
  private static final long serialVersionUID = 7838270781497116177L;
  public static final int REPEAT_INDEFINITELY = -1;
  private int repeatCount = 0;
  private long repeatInterval = 0;

  public SimpleJobTrigger( Date startTime, Date endTime, int repeatCount, long repeatIntervalSeconds ) {
    super( startTime, endTime );
    this.repeatCount = repeatCount;
    this.repeatInterval = repeatIntervalSeconds;
  }

  public SimpleJobTrigger() {
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public void setRepeatCount( int repeatCount ) {
    this.repeatCount = repeatCount;
  }

  public long getRepeatInterval() {
    return repeatInterval;
  }

  public void setRepeatInterval( long repeatIntervalSeconds ) {
    this.repeatInterval = repeatIntervalSeconds;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append( "repeatCount=" ); //$NON-NLS-1$
    b.append( repeatCount );
    b.append( ", " ); //$NON-NLS-1$
    b.append( "repeatInterval=" ); //$NON-NLS-1$
    b.append( repeatInterval );
    b.append( ", " ); //$NON-NLS-1$
    b.append( "startTime=" ); //$NON-NLS-1$
    b.append( super.getStartTime() );
    b.append( ", " ); //$NON-NLS-1$
    b.append( "endTime=" ); //$NON-NLS-1$
    b.append( super.getEndTime() );
    return b.toString();
  }

}
