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

package org.pentaho.platform.web.http;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Utility class for scheduler job operations.
 */
public class SchedulerJobUtil {

  private static final Log logger = LogFactory.getLog( SchedulerJobUtil.class );

  private SchedulerJobUtil() {
    // Private constructor to prevent instantiation
  }

  /**
   * Check if the given job ID corresponds to a system job that uses actionClass instead of inputFile.
   * System jobs don't have repository files but are valid scheduled jobs.
   * 
   * @param jobId the job ID to check
   * @return true if this is a system job using actionClass, false otherwise (including when unable to determine)
   */
  public static boolean isSystemJob( String jobId ) {
    try {
      IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null );
      if ( scheduler != null ) {
        IJob job = scheduler.getJob( jobId );
        if ( job != null ) {
          Map<String, Object> jobParams = job.getJobParams();
          // System jobs use actionClass; file-based jobs use inputFile
          return jobParams != null
            && jobParams.containsKey( IScheduler.RESERVEDMAPKEY_ACTIONCLASS )
            && !jobParams.containsKey( IScheduler.RESERVEDMAPKEY_STREAMPROVIDER_INPUTFILE );
        }
      }
    } catch ( Exception e ) {
      // Unable to determine - log the error and treat as not a system job by returning false
      logger.debug( "Exception while checking if job is a system job: " + jobId, e );
    }
    return false;
  }
}
