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


package org.pentaho.platform.api.engine;

import org.dom4j.Node;
import org.pentaho.actionsequence.dom.IActionDefinition;

import java.util.List;
import java.util.Map;

/**
 * A Component is the smallest module in the platform architecture and represents a unit of work, or an action to
 * be performed. Different Component implementations provide new channels of functionality as well as multiple
 * implementations of similar features (.ie, the BIRT Reporting Component and the Jasper Reports Reporting
 * Component).
 */
public interface IComponent extends IAuditable, ILogger {

  /**
   * Initialize the Component. This method is typically called on construction.
   * 
   * @return returns true if the Component initialized successfully, otherwise returns false
   */
  public boolean init();

  /**
   * Validate that the Component has all the necessary inputs, outputs and resources it needs to execute
   * successfully. Also may validate a schema here.
   * 
   * @return one of IRuntimeContext validation conditions
   * @see org.pentaho.platform.api.engine.IRuntimeContext
   */
  public int validate();

  /**
   * Perform the Component execution; logic for what this Component does goes here.
   * 
   * @return one of IRuntimeContext execution conditions
   * @see org.pentaho.platform.api.engine.IRuntimeContext
   */
  public int execute();

  /**
   * Allows the component to perform any cleanup after the execution of the action.
   * 
   */
  public void done();

  public void setInstanceId( String instanceId );

  public String getInstanceId();

  public void setActionName( String actionName );

  public String getActionName();

  public void setProcessId( String processId );

  public String getProcessId();

  public void setComponentDefinition( Node componentDefinition );

  public void setComponentDefinitionMap( Map<String, String> componentDefinitionMap );

  public Node getComponentDefinition();

  public void setRuntimeContext( IRuntimeContext runtimeContext );

  public IRuntimeContext getRuntimeContext();

  public void setSession( IPentahoSession session );

  public IPentahoSession getSession();

  @SuppressWarnings( "rawtypes" )
  public void setMessages( List messaes );

  @SuppressWarnings( "rawtypes" )
  public List getMessages();

  public void setActionDefinition( IActionDefinition actionDefinition );

  public IActionDefinition getActionDefinition();
}
