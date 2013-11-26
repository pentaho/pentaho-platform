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

package org.pentaho.platform.api.scheduler;

import java.util.Date;

public interface IJobSchedule {
  public String getName();

  public void setName( String name );

  public String getFullname();

  public void setFullname( String fullname );

  public String getTriggerName();

  public void setTriggerName( String triggerName );

  public String getTriggerGroup();

  public void setTriggerGroup( String triggerGroup );

  public int getTriggerState();

  public void setTriggerState( int triggerState );

  public Date getNextFireTime();

  public void setNextFireTime( Date nextFireTime );

  public Date getPreviousFireTime();

  public void setPreviousFireTime( Date previousFireTime );

  public String getJobName();

  public void setJobName( String jobName );

  public String getJobGroup();

  public void setJobGroup( String jobGroup );

  public String getJobDescription();

  public void setJobDescription( String jobDescription );
}
