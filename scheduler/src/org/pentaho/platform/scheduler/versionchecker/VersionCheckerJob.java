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

package org.pentaho.platform.scheduler.versionchecker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class is here for legacy reasons, to allow existing system with the old version checker class to exist and be
 * instanced by quartz. The code here is a no-op, the new version checker job will remove the old one before it is added
 * with the correct/new classname.
 */
public class VersionCheckerJob implements Job {

  public static final String VERSION_REQUEST_FLAGS = "versionRequestFlags"; //$NON-NLS-1$

  public Log getLogger() {
    return LogFactory.getLog( VersionCheckerJob.class );
  }

  public void execute( final JobExecutionContext context ) throws JobExecutionException {
  }
}
