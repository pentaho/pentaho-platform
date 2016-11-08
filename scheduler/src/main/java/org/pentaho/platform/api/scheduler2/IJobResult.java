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

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * This structure is a representation of a particular scheduled job run. Once a job run has completed, an new
 * {@link IJobResult} will be generated and kept by the scheduler for later historical querying.
 * 
 * @author aphillip
 */
public interface IJobResult {

  /**
   * The unique id of the job run.
   * 
   * @return a unique id of the job run
   */
  public String getId();

  /**
   * The job parameters used during this job run.
   * 
   * @return set of parameters used during job run
   */
  public Map<String, Serializable> getJobParams();

  /**
   * The start date/time of the job run
   * 
   * @return start date/time of the job run
   */
  public Date getStartDate();

  /**
   * The end date/time of the job run
   * 
   * @return end date/time of the job run
   */
  public Date getCompletionDate();
}
