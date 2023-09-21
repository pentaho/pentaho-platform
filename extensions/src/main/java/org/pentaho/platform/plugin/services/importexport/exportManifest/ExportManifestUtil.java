/*!
 *
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
 *
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.pentaho.platform.api.scheduler2.IJobScheduleRequest;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.JobScheduleRequest;

import java.util.ArrayList;
import java.util.List;

public class ExportManifestUtil {

  public static ArrayList<JobScheduleRequest> fromSchedulerToBindingRequest( List<IJobScheduleRequest> scheduleList ) {
    ArrayList<JobScheduleRequest> schedules = new ArrayList<>();
    for ( IJobScheduleRequest schedulerRequest : scheduleList ) {
      JobScheduleRequest bindingRequest = new JobScheduleRequest();
      bindingRequest.setJobName( schedulerRequest.getJobName() );
      bindingRequest.setDuration( schedulerRequest.getDuration() );
      //bindingRequest.setJobState( schedulerRequest.getJobState() );
      bindingRequest.setInputFile( schedulerRequest.getInputFile() );
      bindingRequest.setOutputFile( schedulerRequest.getOutputFile() );
      //bindingRequest.setPdiParameters( schedulerRequest.getPdiParameters() );
      bindingRequest.setActionClass( schedulerRequest.getActionClass() );
      bindingRequest.setTimeZone( schedulerRequest.getTimeZone() );
      //bindingRequest.setJobParameters( schedulerRequest.getJobParameters() );
      //bindingRequest.setSimpleJobTrigger( schedulerRequest.getSimpleJobTrigger() );
      //bindingRequest.setCronJobTrigger( schedulerRequest.getCronJobTrigger() );
      schedules.add( bindingRequest );
    }
    return schedules;
  }

  public static ArrayList<IJobScheduleRequest> fromBindingToSchedulerRequest( List<JobScheduleRequest> bindingRequests ) {
    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    assert scheduler != null;
    ArrayList<IJobScheduleRequest> schedules = new ArrayList<>();
    for ( JobScheduleRequest bindingRequest : bindingRequests ) {
      IJobScheduleRequest scheduleRequest = scheduler.createJobScheduleRequest();
      scheduleRequest.setJobName( bindingRequest.getJobName() );
      scheduleRequest.setDuration( bindingRequest.getDuration() );
      //scheduleRequest.setJobState( bindingRequest.getJobState() );
      scheduleRequest.setInputFile( bindingRequest.getInputFile() );
      scheduleRequest.setOutputFile( bindingRequest.getOutputFile() );
      //scheduleRequest.setPdiParameters( bindingRequest.getPdiParameters() );
      scheduleRequest.setActionClass( bindingRequest.getActionClass() );
      scheduleRequest.setTimeZone( bindingRequest.getTimeZone() );
      //scheduleRequest.setJobParameters( bindingRequest.getJobParameters() );
      //scheduleRequest.setSimpleJobTrigger( bindingRequest.getSimpleJobTrigger() );
      //scheduleRequest.setCronJobTrigger( bindingRequest.getCronJobTrigger() );
      schedules.add( scheduleRequest );
    }
    return schedules;
  }
}
