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

package org.pentaho.platform.scheduler2.versionchecker;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.versionchecker.PentahoVersionCheckReflectHelper;

public class EmbeddedVersionCheckSystemListener implements IPentahoSystemListener {

  /**
   * This is a direct copy of VersionCheckSystemListener except that the mechanism for talking to quartz goes through
   * the PentahoSystem factory
   */
  private static final Log logger = LogFactory.getLog( EmbeddedVersionCheckSystemListener.class );

  private static final String VERSION_CHECK_JOBNAME = "PentahoSystemVersionCheck"; //$NON-NLS-1$
  private static int MIN_CHECK_INTERVAL = 43200;
  private static int DEFAULT_CHECK_INTERVAL = 86400;

  private int repeatIntervalSeconds = DEFAULT_CHECK_INTERVAL;
  private String requestedReleases = "minor, ga"; //$NON-NLS-1$
  private boolean disableVersionCheck = false;

  public boolean startup( final IPentahoSession session ) {
    if ( isVersionCheckAvailable() ) {
      // register version check job
      try {
        int repeatSeconds = calculateRepeatSeconds();
        int versionRequestFlags = calculateRequestFlags();

        if ( !disableVersionCheck ) {
          scheduleJob( versionRequestFlags, repeatSeconds );
        } else {
          deleteJobIfNecessary();
        }
      } catch ( Exception ignoredMainException ) {
        // ignore errors by versioncheck requirements unless trace level
        if ( logger.isTraceEnabled() ) {
          logger.trace( "Exception in VersionCheck", ignoredMainException ); //$NON-NLS-1$
        }
      }

    } else {
      try {
        deleteJobIfNecessary();
      } catch ( SchedulerException ignoredOnPurpose ) {
        // By version checker requirement, we must not log unless it's trace
        if ( logger.isTraceEnabled() ) {
          logger.trace( "Exception in VersionCheck", ignoredOnPurpose ); //$NON-NLS-1$
        }
      }
    }
    return true;
  }

  public int calculateRepeatSeconds() {
    return Math.max( MIN_CHECK_INTERVAL, repeatIntervalSeconds ); // Force maximum number of times to check in a day
  }

  protected int calculateRequestFlags() {
    boolean requestMajorReleases = requestedReleases.indexOf( "major" ) >= 0; //$NON-NLS-1$
    boolean requestMinorReleases = requestedReleases.indexOf( "minor" ) >= 0; //$NON-NLS-1$
    boolean requestRCReleases = requestedReleases.indexOf( "rc" ) >= 0; //$NON-NLS-1$
    boolean requestGAReleases = requestedReleases.indexOf( "ga" ) >= 0; //$NON-NLS-1$
    boolean requestMilestoneReleases = requestedReleases.indexOf( "milestone" ) >= 0; //$NON-NLS-1$

    int versionRequestFlags =
        ( requestMajorReleases ? 4 : 0 ) + ( requestMinorReleases ? 8 : 0 ) + ( requestRCReleases ? 16 : 0 )
            + ( requestGAReleases ? 32 : 0 ) + ( requestMilestoneReleases ? 64 : 0 );
    return versionRequestFlags;
  }

  protected void scheduleJob( final int versionRequestFlags, final int repeatSeconds ) throws Exception {

    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$

    deleteJobIfNecessary();

    Map<String, Serializable> parms = new HashMap<String, Serializable>();
    parms.put( VersionCheckerAction.VERSION_REQUEST_FLAGS, new Integer( versionRequestFlags ) );
    JobTrigger trigger = new SimpleJobTrigger( new Date(), null, -1, repeatSeconds );
    scheduler.createJob( EmbeddedVersionCheckSystemListener.VERSION_CHECK_JOBNAME, VersionCheckerAction.class, parms,
        trigger );
  }

  protected void deleteJobIfNecessary() throws SchedulerException {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    IJobFilter filter = new IJobFilter() {
      public boolean accept( Job job ) {
        return job.getJobName().contains( EmbeddedVersionCheckSystemListener.VERSION_CHECK_JOBNAME );
      }
    };

    // Like old code - remove the existing job and replace it
    List<Job> matchingJobs = scheduler.getJobs( filter );
    if ( ( matchingJobs != null ) && ( matchingJobs.size() > 0 ) ) {
      for ( Job verCkJob : matchingJobs ) {
        scheduler.removeJob( verCkJob.getJobId() );
      }
    }
  }

  protected boolean isVersionCheckAvailable() {
    return PentahoVersionCheckReflectHelper.isVersionCheckerAvailable();
  }

  public void shutdown() {
  }

  public int getRepeatIntervalSeconds() {
    return repeatIntervalSeconds;
  }

  public void setRepeatIntervalSeconds( int repeatIntervalSeconds ) {
    this.repeatIntervalSeconds = repeatIntervalSeconds;
  }

  public String getRequestedReleases() {
    return requestedReleases;
  }

  public void setRequestedReleases( String requestedReleases ) {
    this.requestedReleases = requestedReleases;
  }

  public boolean isDisableVersionCheck() {
    return disableVersionCheck;
  }

  public void setDisableVersionCheck( boolean disableVersionCheck ) {
    this.disableVersionCheck = disableVersionCheck;
  }

}
