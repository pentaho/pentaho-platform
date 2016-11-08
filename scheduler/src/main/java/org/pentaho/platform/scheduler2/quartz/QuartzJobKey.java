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

package org.pentaho.platform.scheduler2.quartz;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.scheduler2.messsages.Messages;

/**
 * This class is the key by which we identify a quartz job. It provides the means to create a new key or derive a key
 * from an existing job. This should be the only place in the Quartz scheduler that knows exactly how a jobId is
 * constructed.
 * 
 * @author aphillips
 */
public class QuartzJobKey {
  private String userName;
  private String jobName;
  private long timeInMillis;

  /**
   * Use this constructor when you wish to create a new unique job key.
   * 
   * @param jobName
   *          the user-provided job name
   * @param username
   *          the user who is executing this job
   * @throws SchedulerException
   */
  public QuartzJobKey( String jobName, String username ) throws SchedulerException {
    if ( StringUtils.isEmpty( jobName ) ) {
      throw new SchedulerException( Messages.getInstance().getErrorString( "QuartzJobKey.ERROR_0000" ) ); //$NON-NLS-1$
    }
    if ( StringUtils.isEmpty( username ) ) {
      throw new SchedulerException( Messages.getInstance().getErrorString( "QuartzJobKey.ERROR_0001" ) ); //$NON-NLS-1$
    }
    userName = username;
    this.jobName = jobName;
    timeInMillis = System.currentTimeMillis();
  }

  private QuartzJobKey() {
  }

  /**
   * Parses an existing jobId into a {@link QuartzJobKey}
   * 
   * @param jobId
   *          an existing jobId
   * @return a quartz job key
   * @throws SchedulerException
   */
  public static QuartzJobKey parse( String jobId ) throws SchedulerException {
    String delimiter = jobId.contains( "\t" ) || jobId.isEmpty() ? "\t" : ":";
    String[] elements = jobId.split( delimiter ); //$NON-NLS-1$
    if ( elements == null || elements.length < 3 ) {
      throw new SchedulerException( MessageFormat.format( Messages.getInstance().getErrorString(
          "QuartzJobKey.ERROR_0002" ), jobId ) ); //$NON-NLS-1$
    }
    QuartzJobKey key = new QuartzJobKey();
    key.userName = elements[0];
    key.jobName = elements[1];
    try {
      key.timeInMillis = Long.parseLong( elements[2] );
    } catch ( NumberFormatException ex ) {
      throw new SchedulerException( MessageFormat.format( Messages.getInstance().getErrorString(
          "QuartzJobKey.ERROR_0002" ), jobId ) ); //$NON-NLS-1$
    }
    return key;
  }

  public String getUserName() {
    return userName;
  }

  public String getJobName() {
    return jobName;
  }

  @Override
  public String toString() {
    return userName + "\t" + jobName + "\t" + timeInMillis; //$NON-NLS-1$ //$NON-NLS-2$
  }
}
