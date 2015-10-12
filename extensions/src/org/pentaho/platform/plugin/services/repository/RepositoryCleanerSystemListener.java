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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is a 5.4-only class. To use it, update <tt>systemListeners.xml</tt> by adding the following section:
 * <pre>
  &lt;bean id="repositoryCleanerSystemListener"
        class="org.pentaho.platform.plugin.services.repository.RepositoryCleanerSystemListener"&gt;
    &lt;property name="gcEnabled" value="true"/&gt;
    &lt;property name="execute" value="now"/&gt;
  &lt;/bean&gt;
 * </pre>
 * <tt>gcEnabled</tt> is a non-mandatory parameter, <tt>true</tt> by default. Use it to turn off the listener without
 * removing its description from the XML-file
 * <tt>execute</tt> is a parameter, that describes a time pattern of GC procedure. Supported values are:
 * <ul>
 *   <li><tt>now</tt> - for one time execution</li>
 *   <li><tt>weekly</tt> - for every Monday execution</li>
 *   <li><tt>monthly</tt> - for every first day of month execution</li>
 * </ul>
 * Note, that periodic executions will be planned to start at 0:00. If an execution was not started at that time,
 * e.g. the server was shut down, then it will be started as soon as the scheduler is restored.
 * @author Andrey Khayrutdinov
 */
public class RepositoryCleanerSystemListener implements IPentahoSystemListener, IJobFilter {

  private final Log logger = LogFactory.getLog( RepositoryCleanerSystemListener.class );

  enum Frequency {
    NOW( "now" ) {
      @Override public JobTrigger createTrigger() {
        return new SimpleJobTrigger( new Date(),
          new Date( Long.MAX_VALUE ), 0, 1 );
      }
    },

    WEEKLY( "weekly" ) {
      @Override public JobTrigger createTrigger() {
        // execute each first day of week at 0 hours
        return new ComplexJobTrigger( null, null, null, ComplexJobTrigger.SUNDAY, 0 );
      }
    },

    MONTHLY( "monthly" ) {
      @Override public JobTrigger createTrigger() {
        // execute each first day of month at 0 hours
        return new ComplexJobTrigger( null, null, 1, null, 0 );
      }
    };

    private final String value;

    Frequency( String value ) {
      this.value = value;
    }

    public abstract JobTrigger createTrigger();

    public static Frequency fromString( String name ) {
      for ( Frequency frequency : values() ) {
        if ( frequency.value.equalsIgnoreCase( name ) ) {
          return frequency;
        }
      }
      return null;
    }

    String getValue() {
      return value;
    }
  }

  private boolean gcEnabled = true;
  private String execute;

  @Override
  public boolean startup( IPentahoSession session ) {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", session );
    if ( scheduler == null ) {
      logger.error( "Cannot obtain an instance of IScheduler2" );
      return false;
    }

    try {
      List<Job> jobs = scheduler.getJobs( this );
      if ( gcEnabled ) {
        if ( jobs.isEmpty() ) {
          scheduleJob( scheduler );
        } else {
          rescheduleIfNecessary( scheduler, jobs );
        }
      } else {
        if ( !jobs.isEmpty() ) {
          unscheduleJob( scheduler, jobs );
        }
      }
    } catch ( SchedulerException e ) {
      logger.error( "Scheduler error", e );
    }
    return true;
  }

  private JobTrigger findJobTrigger() {
    if ( StringUtil.isEmpty( execute ) ) {
      logger.error( "\"execute\" property is not specified!" );
      return null;
    }

    Frequency frequency = Frequency.fromString( execute );
    if ( frequency == null ) {
      logger.error( "Unknown value for property \"execute\": " + execute );
      return null;
    }

    return frequency.createTrigger();
  }

  private void scheduleJob( IScheduler scheduler ) throws SchedulerException {
    JobTrigger trigger = findJobTrigger();
    if ( trigger != null ) {
      logger.info( "Creating new job with trigger: " + trigger );
      scheduler.createJob( RepositoryGcJob.JOB_NAME, RepositoryGcJob.class, null, trigger );
    }
  }

  private void rescheduleIfNecessary( IScheduler scheduler, List<Job> jobs ) throws SchedulerException {
    JobTrigger trigger = findJobTrigger();
    if ( trigger == null ) {
      return;
    }

    List<Job> matched = new ArrayList<Job>( jobs.size() );
    for ( Job job : jobs ) {
      JobTrigger tr = job.getJobTrigger();
      // unfortunately, JobTrigger does not override equals
      if ( trigger.getClass() != tr.getClass() ) {
        logger.info( "Removing job with id: " + job.getJobId() );
        scheduler.removeJob( job.getJobId() );
      } else {
        matched.add( job );
      }
    }

    if ( matched.isEmpty() ) {
      logger.info( "Need to re-schedule job" );
      scheduleJob( scheduler );
    }
  }

  private void unscheduleJob( IScheduler scheduler, List<Job> jobs ) throws SchedulerException {
    for ( Job job : jobs ) {
      logger.info( "Removing job with id: " + job.getJobId() );
      scheduler.removeJob( job.getJobId() );
    }
  }


  @Override
  public void shutdown() {
    // nothing to do
  }

  @Override
  public boolean accept( Job job ) {
    return RepositoryGcJob.JOB_NAME.equals( job.getJobName() );
  }


  public boolean isGcEnabled() {
    return gcEnabled;
  }

  public void setGcEnabled( boolean gcEnabled ) {
    this.gcEnabled = gcEnabled;
  }

  public String getExecute() {
    return execute;
  }

  public void setExecute( String execute ) {
    this.execute = execute;
  }
}
