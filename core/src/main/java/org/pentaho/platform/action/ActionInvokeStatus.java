/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
