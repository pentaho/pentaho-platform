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

import java.util.List;

public interface ISchedulerResource {

  List<IJob> getJobsList();

  Object pause();

  void removeJob( IJobRequest jobRequest );

  Object createJob( IJobScheduleRequest scheduleRequest );

  Object start();

  void pauseJob( IJobRequest jobRequest );
}
