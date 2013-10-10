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

/**
 * Specifies a filter to be used when processing lists of jobs such as in {@link IScheduler#getJobs(IJobFilter)}
 * 
 * @author aphillips
 */
public interface IJobFilter {
  /**
   * Returns <code>true</code> if the job should be accepted as part of the filtered results.
   * 
   * @param job
   *          the job to decide to accept or reject
   * @return <code>true</code> if the job should be accepted as part of the filtered results
   */
  public boolean accept( Job job );
}
