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


package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;

public class TestOutputHandler implements IContentOutputHandler {

  public static IContentItem contentItem = null;

  public String getActionName() {
    // Auto-generated method stub
    return null;
  }

  public String getContentRef() {
    // Auto-generated method stub
    return null;
  }

  public IContentItem getFileOutputContentItem() {
    return contentItem;
  }

  public String getHandlerId() {
    // Auto-generated method stub
    return null;
  }

  public String getInstanceId() {
    // Auto-generated method stub
    return null;
  }

  public String getMimeType() {
    // Auto-generated method stub
    return null;
  }

  public IPentahoSession getSession() {
    // Auto-generated method stub
    return null;
  }

  public String getSolutionName() {
    // Auto-generated method stub
    return null;
  }

  public String getSolutionPath() {
    // Auto-generated method stub
    return null;
  }

  public void setActionName( String actionName ) {
    // Auto-generated method stub

  }

  public void setContentRef( String contentRef ) {
    // Auto-generated method stub

  }

  public void setHandlerId( String handlerId ) {
    // Auto-generated method stub

  }

  public void setInstanceId( String instanceId ) {
    // Auto-generated method stub

  }

  public void setMimeType( String mimeType ) {
    // Auto-generated method stub

  }

  public void setSession( IPentahoSession session ) {
    // Auto-generated method stub

  }

  public void setSolutionName( String solutionName ) {
    // Auto-generated method stub

  }

  public void setSolutionPath( String solutionPath ) {
    // Auto-generated method stub

  }

  public void setName( String name ) {
    // Auto-generated method stub

  }

}
