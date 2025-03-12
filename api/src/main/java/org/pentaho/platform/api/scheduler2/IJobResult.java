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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * This structure is a representation of a particular scheduled job run. Once a job run has completed, an new
 * {@link IJobResult} will be generated and kept by the scheduler for later historical querying.
 * 
 * @author aphillip
 */
public interface IJobResult {

  /**
   * The unique id of the job run.
   * 
   * @return a unique id of the job run
   */
  String getId();

}
