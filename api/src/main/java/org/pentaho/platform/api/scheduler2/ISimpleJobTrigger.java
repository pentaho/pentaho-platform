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


package org.pentaho.platform.api.scheduler2;

/**
 * A simple way of specifying a schedule on which a job will fire as opposed to {@link IComplexJobTrigger}. The
 * {@link ISimpleJobTrigger} can meet your needs if you are looking for a way to have a job start, execute a set number
 * of times on a regular interval and then end either after a specified number of runs or at an end date.
 *
 * @author aphillips
 */

public interface ISimpleJobTrigger extends IJobTrigger {
  int getRepeatCount();

  void setRepeatCount( int repeatCount );

  long getRepeatInterval();

  void setRepeatInterval( long repeatIntervalSeconds );
}
