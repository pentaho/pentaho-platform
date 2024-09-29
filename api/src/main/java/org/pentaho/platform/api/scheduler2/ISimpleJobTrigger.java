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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.scheduler2;

/**
 * A simple way of specifying a schedule on which a job will fire as opposed to {@link IComplexJobTrigger}. The
 * {@link ISimpleJobTrigger} can meet your needs if you are looking for a way to have a job start, execute a set number
 * of times on a regular interval and then end either after a specified number of runs or at an end date.
 *
 * @author aphillips
 */

public interface ISimpleJobTrigger extends IJobTrigger {
  int getRepeatCount();

  void setRepeatCount( int repeatCount );

  long getRepeatInterval();

  void setRepeatInterval( long repeatIntervalSeconds );
}
