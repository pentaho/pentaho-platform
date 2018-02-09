/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.action;

import org.pentaho.platform.api.action.IActionInvokeStatus;

public class ActionInvokeStatus implements IActionInvokeStatus {
  private boolean requiresUpdate;
  private Throwable throwable;
  private Object streamProvider;
  private boolean executionStatus;

  public void setRequiresUpdate( final boolean requiresUpdate ) {
    this.requiresUpdate = requiresUpdate;
  }

  public boolean requiresUpdate() {
    return this.requiresUpdate;
  }

  public void setThrowable( final Throwable throwable ) {
    this.throwable = throwable;
  }

  public Throwable getThrowable() {
    return this.throwable;
  }

  public void setStreamProvider( final Object streamProvider ) {
    this.streamProvider = streamProvider;
  }

  public Object getStreamProvider() {
    return this.streamProvider;
  }

  public boolean isExecutionSuccessful() {
    return executionStatus;
  }

  public void setExecutionStatus( boolean status ) {
    executionStatus = status;
  }
}
