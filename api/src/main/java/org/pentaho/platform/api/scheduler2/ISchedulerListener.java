/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Map;

import org.pentaho.platform.api.action.IAction;

public interface ISchedulerListener {
  public void jobCompleted( IAction actionBean, String actionUser, Map<String, Object> params,
      IBackgroundExecutionStreamProvider streamProvider );
}
