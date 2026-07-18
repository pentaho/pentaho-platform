/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface IJob {
  IJobTrigger getJobTrigger();
  Map<String, Object> getJobParams();
  String getJobId();
  String getJobName();
  JobState getState();
  String getUserName();
  Date getNextRun();
  Date getLastRun();
}
