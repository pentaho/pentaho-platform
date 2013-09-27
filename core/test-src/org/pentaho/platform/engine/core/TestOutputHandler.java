/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;

public class TestOutputHandler implements IContentOutputHandler {

  public static IContentItem contentItem = null;

  public String getActionName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentRef() {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentItem getFileOutputContentItem() {
    return contentItem;
  }

  public String getHandlerId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getInstanceId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getMimeType() {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoSession getSession() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSolutionName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSolutionPath() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setActionName( String actionName ) {
    // TODO Auto-generated method stub

  }

  public void setContentRef( String contentRef ) {
    // TODO Auto-generated method stub

  }

  public void setHandlerId( String handlerId ) {
    // TODO Auto-generated method stub

  }

  public void setInstanceId( String instanceId ) {
    // TODO Auto-generated method stub

  }

  public void setMimeType( String mimeType ) {
    // TODO Auto-generated method stub

  }

  public void setSession( IPentahoSession session ) {
    // TODO Auto-generated method stub

  }

  public void setSolutionName( String solutionName ) {
    // TODO Auto-generated method stub

  }

  public void setSolutionPath( String solutionPath ) {
    // TODO Auto-generated method stub

  }

  public void setName( String name ) {
    // TODO Auto-generated method stub

  }

}
