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

/**
 * Specifies a filter to be used when processing lists of jobs such as in {@link IScheduler#getJobs(IJobFilter)}
 * 
 * @author aphillips
 */
public interface IJobFilter {
  /**
   * Returns <code>true</code> if the job should be accepted as part of the filtered results.
   * 
   * @param job
   *          the job to decide to accept or reject
   * @return <code>true</code> if the job should be accepted as part of the filtered results
   */
  boolean accept( IJob job );
}
