/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
