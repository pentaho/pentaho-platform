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

package org.pentaho.platform.web.http.api.resources.services;

import java.util.List;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.pentaho.platform.web.http.api.resources.JobRequest;
import org.pentaho.platform.web.http.api.resources.proxies.BlockStatusProxy;

public class SchedulerService {

  protected static IScheduler scheduler;

  protected static IAuthorizationPolicy policy;
  
  protected static IBlockoutManager blockoutManager =
      PentahoSystem.get( IBlockoutManager.class, "IBlockoutManager", null ); //$NON-NLS-1$;


  public Job triggerNow( JobRequest jobRequest ) throws SchedulerException {
    Job job = getScheduler().getJob( jobRequest.getJobId() );
    if ( getPolicy().isAllowed( SchedulerAction.NAME ) ) {
      getScheduler().triggerNow( jobRequest.getJobId() );
    } else {
      if ( getSession().getName().equals( job.getUserName() ) ) {
        getScheduler().triggerNow( jobRequest.getJobId() );
      }
    }
    // udpate job state
    job = getScheduler().getJob( jobRequest.getJobId() );

    return job;
  }

  public List<Job> getBlockOutJobs() {
    return blockoutManager.getBlockOutJobs();
  }

  public boolean hasBlockouts() {
    List<Job> jobs = blockoutManager.getBlockOutJobs();
    return jobs != null && jobs.size() > 0;
  }

  public boolean willFire( IJobTrigger trigger ) {
    return blockoutManager.willFire( trigger );
  }
  
  public boolean shouldFireNow() {
    return blockoutManager.shouldFireNow();    
  }
  
  public BlockStatusProxy getBlockStatus( IJobTrigger trigger ) {
    Boolean totallyBlocked = false;
    Boolean partiallyBlocked = blockoutManager.isPartiallyBlocked( trigger );
    if ( partiallyBlocked ) {
      totallyBlocked = !blockoutManager.willFire( trigger );
    }
    return new BlockStatusProxy( totallyBlocked, partiallyBlocked );
  }
  
  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  public static IScheduler getScheduler() {
    if ( scheduler == null ) {
      scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null );
    }

    return scheduler;
  }

  public static IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }

    return policy;
  }
  
}
