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

package org.pentaho.platform.scheduler2.ws;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.scheduler2.ws.JaxBSafeMap.JaxBSafeEntry;

/**
 * Handles the sending of {@link Job} objects over JAXWS webservices by utilizing {@link JaxbSafeJob} as a transport
 * type.
 * 
 * @author aphillips
 */
public class JobAdapter extends XmlAdapter<JobAdapter.JaxbSafeJob, Job> {

  private static final Log logger = LogFactory.getLog( JobAdapter.class );

  public JaxbSafeJob marshal( Job job ) throws Exception {
    if ( job == null ) {
      return null;
    }

    JaxbSafeJob jaxbSafeJob = new JaxbSafeJob();
    try {
      if ( !( job.getJobTrigger() instanceof JobTrigger ) ) {
        throw new IllegalArgumentException();

      }
      jaxbSafeJob.jobTrigger = job.getJobTrigger();
      jaxbSafeJob.jobParams = new JaxBSafeMap( toParamValueMap( job.getJobParams() ) );
      jaxbSafeJob.lastRun = job.getLastRun();
      jaxbSafeJob.nextRun = job.getNextRun();
      jaxbSafeJob.schedulableClass = job.getSchedulableClass();
      jaxbSafeJob.jobId = job.getJobId();
      jaxbSafeJob.userName = job.getUserName();
      jaxbSafeJob.jobName = job.getJobName();
      jaxbSafeJob.state = job.getState();
    } catch ( Throwable t ) {
      // no message bundle since this is a development error case
      logger.error( "Error marshalling job", t ); //$NON-NLS-1$
      return null;
    }
    return jaxbSafeJob;
  }

  @SuppressWarnings( "unchecked" )
  private Map<String, ParamValue> toParamValueMap( Map<String, Serializable> unsafeMap ) {
    Map<String, ParamValue> paramValueMap = new HashMap<String, ParamValue>();
    for ( Map.Entry<String, Serializable> entry : unsafeMap.entrySet() ) {
      if ( entry.getValue() instanceof Map ) {
        // convert the inner map
        MapParamValue map = new MapParamValue();
        Map innerMap = (Map) entry.getValue();
        Set<Map.Entry> entrySet = innerMap.entrySet();
        for ( Map.Entry innerEntry : entrySet ) {
          map.put( innerEntry.getKey().toString(), ( innerEntry.getValue() == null ) ? null : innerEntry.getValue()
              .toString() );
        }
        // add the converted map the the top-level map
        paramValueMap.put( entry.getKey(), map );
      } else if ( entry.getValue() instanceof List ) {
        ListParamValue list = new ListParamValue();
        List innerList = (List) entry.getValue();
        list.addAll( innerList );
        paramValueMap.put( entry.getKey(), list );
      } else {
        paramValueMap.put( entry.getKey(), new StringParamValue( ( entry.getValue() == null ) ? null : entry.getValue()
            .toString() ) );
      }
    }
    return paramValueMap;
  }

  private Map<String, Serializable> toProperMap( JaxBSafeMap safeMap ) {
    Map<String, Serializable> unsafeMap = new HashMap<String, Serializable>();
    for ( JaxBSafeEntry safeEntry : safeMap.entry ) {
      if ( safeEntry.getStringValue() != null ) {
        unsafeMap.put( safeEntry.key, ( safeEntry.getStringValue() == null ) ? null : safeEntry.getStringValue()
            .toString() );
        continue;
      }
      if ( safeEntry.getListValue() != null ) {
        unsafeMap.put( safeEntry.key, safeEntry.getListValue() );
        continue;
      }
      if ( safeEntry.getMapValue() != null ) {
        unsafeMap.put( safeEntry.key, safeEntry.getMapValue() );
        continue;
      }
    }
    return unsafeMap;
  }

  public Job unmarshal( JaxbSafeJob jaxbSafeJob ) throws Exception {
    if ( jaxbSafeJob == null ) {
      return null;
    }

    Job job = new Job();
    try {
      job.setJobTrigger( jaxbSafeJob.jobTrigger );
      job.setJobParams( toProperMap( jaxbSafeJob.jobParams ) );
      job.setLastRun( jaxbSafeJob.lastRun );
      job.setNextRun( jaxbSafeJob.nextRun );
      job.setSchedulableClass( jaxbSafeJob.schedulableClass );
      job.setJobId( jaxbSafeJob.jobId );
      job.setUserName( jaxbSafeJob.userName );
      job.setJobName( jaxbSafeJob.jobName );
      job.setState( jaxbSafeJob.state );
    } catch ( Throwable t ) {
      // no message bundle since this is a development error case
      logger.error( "Error unmarshalling job", t ); //$NON-NLS-1$
      return null;
    }
    return job;

  }

  @XmlRootElement
  public static class JaxbSafeJob {
    public JobTrigger jobTrigger;

    public JaxBSafeMap jobParams;

    public Date lastRun;

    public Date nextRun;

    public String schedulableClass;

    public String jobId;

    public String userName;

    public String jobName;

    public JobState state;
  }

}
