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
