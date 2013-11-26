/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.engine.core;

import org.dom4j.Node;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;

import java.util.List;
import java.util.Map;

public class MockComponent implements IComponent {

  public void done() {
    // TODO Auto-generated method stub

  }

  public int execute() {
    // TODO Auto-generated method stub
    return 0;
  }

  public IActionDefinition getActionDefinition() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getActionName() {
    // TODO Auto-generated method stub
    return null;
  }

  public Node getComponentDefinition() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getInstanceId() {
    // TODO Auto-generated method stub
    return null;
  }

  public List getMessages() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getProcessId() {
    // TODO Auto-generated method stub
    return null;
  }

  public IRuntimeContext getRuntimeContext() {
    // TODO Auto-generated method stub
    return null;
  }

  public IPentahoSession getSession() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean init() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setActionDefinition( IActionDefinition actionDefinition ) {
    // TODO Auto-generated method stub

  }

  public void setActionName( String actionName ) {
    // TODO Auto-generated method stub

  }

  public void setComponentDefinition( Node componentDefinition ) {
    // TODO Auto-generated method stub

  }

  public void setComponentDefinitionMap( Map<String, String> componentDefinitionMap ) {
    // TODO Auto-generated method stub

  }

  public void setInstanceId( String instanceId ) {
    // TODO Auto-generated method stub

  }

  public void setMessages( List messaes ) {
    // TODO Auto-generated method stub

  }

  public void setProcessId( String processId ) {
    // TODO Auto-generated method stub

  }

  public void setRuntimeContext( IRuntimeContext runtimeContext ) {
    // TODO Auto-generated method stub

  }

  public void setSession( IPentahoSession session ) {
    // TODO Auto-generated method stub

  }

  public int validate() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getObjectName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void debug( String message ) {
    // TODO Auto-generated method stub

  }

  public void debug( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void error( String message ) {
    // TODO Auto-generated method stub

  }

  public void error( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void fatal( String message ) {
    // TODO Auto-generated method stub

  }

  public void fatal( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public int getLoggingLevel() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void info( String message ) {
    // TODO Auto-generated method stub

  }

  public void info( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void setLoggingLevel( int loggingLevel ) {
    // TODO Auto-generated method stub

  }

  public void trace( String message ) {
    // TODO Auto-generated method stub

  }

  public void trace( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void warn( String message ) {
    // TODO Auto-generated method stub

  }

  public void warn( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

}
