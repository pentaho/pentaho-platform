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
    // Auto-generated method stub

  }

  public int execute() {
    // Auto-generated method stub
    return 0;
  }

  public IActionDefinition getActionDefinition() {
    // Auto-generated method stub
    return null;
  }

  public String getActionName() {
    // Auto-generated method stub
    return null;
  }

  public Node getComponentDefinition() {
    // Auto-generated method stub
    return null;
  }

  public String getInstanceId() {
    // Auto-generated method stub
    return null;
  }

  public List getMessages() {
    // Auto-generated method stub
    return null;
  }

  public String getProcessId() {
    // Auto-generated method stub
    return null;
  }

  public IRuntimeContext getRuntimeContext() {
    // Auto-generated method stub
    return null;
  }

  public IPentahoSession getSession() {
    // Auto-generated method stub
    return null;
  }

  public boolean init() {
    // Auto-generated method stub
    return false;
  }

  public void setActionDefinition( IActionDefinition actionDefinition ) {
    // Auto-generated method stub

  }

  public void setActionName( String actionName ) {
    // Auto-generated method stub

  }

  public void setComponentDefinition( Node componentDefinition ) {
    // Auto-generated method stub

  }

  public void setComponentDefinitionMap( Map<String, String> componentDefinitionMap ) {
    // Auto-generated method stub

  }

  public void setInstanceId( String instanceId ) {
    // Auto-generated method stub

  }

  public void setMessages( List messaes ) {
    // Auto-generated method stub

  }

  public void setProcessId( String processId ) {
    // Auto-generated method stub

  }

  public void setRuntimeContext( IRuntimeContext runtimeContext ) {
    // Auto-generated method stub

  }

  public void setSession( IPentahoSession session ) {
    // Auto-generated method stub

  }

  public int validate() {
    // Auto-generated method stub
    return 0;
  }

  public String getId() {
    // Auto-generated method stub
    return null;
  }

  public String getObjectName() {
    // Auto-generated method stub
    return null;
  }

  public void debug( String message ) {
    // Auto-generated method stub

  }

  public void debug( String message, Throwable error ) {
    // Auto-generated method stub

  }

  public void error( String message ) {
    // Auto-generated method stub

  }

  public void error( String message, Throwable error ) {
    // Auto-generated method stub

  }

  public void fatal( String message ) {
    // Auto-generated method stub

  }

  public void fatal( String message, Throwable error ) {
    // Auto-generated method stub

  }

  public int getLoggingLevel() {
    // Auto-generated method stub
    return 0;
  }

  public void info( String message ) {
    // Auto-generated method stub

  }

  public void info( String message, Throwable error ) {
    // Auto-generated method stub

  }

  public void setLoggingLevel( int loggingLevel ) {
    // Auto-generated method stub

  }

  public void trace( String message ) {
    // Auto-generated method stub

  }

  public void trace( String message, Throwable error ) {
    // Auto-generated method stub

  }

  public void warn( String message ) {
    // Auto-generated method stub

  }

  public void warn( String message, Throwable error ) {
    // Auto-generated method stub

  }

}
