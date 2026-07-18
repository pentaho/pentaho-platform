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
import java.util.Map;

import org.pentaho.platform.api.action.IAction;

public interface ISchedulerListener {
  public void jobCompleted( IAction actionBean, String actionUser, Map<String, Object> params,
      IBackgroundExecutionStreamProvider streamProvider );
}
