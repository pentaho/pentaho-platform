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

import java.util.List;
import java.util.Map;

/**
 * The ActionDefinition represents the definition and metadata for a single action execution, which is equivalent
 * to one execution of any given Component.
 * <p>
 * The ActionDefinition is derived from the solution's action sequence document. One ActionDefinition is handed to
 * the appropriate Component, and provides all the necessary inputs, outputs and resources for that Component to
 * execute.
 */
public interface ISolutionActionDefinition {

  /**
   * Returns a Map of the input parameters that are defined to this ActionDefinition.
   * 
   * @return Map of input parameters. Parameters take the name-value form.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getActionInputDefinitions();

  /**
   * 
   * Returns the name of the parameter that the passed in name is mapped to in the Action Sequence Document
   * 
   * @param name
   *          String name of the parameter to get a mapping for
   * @return String name of the parameter that 'name' is mapped to
   */
  public String getMappedInputName( String name );

  /**
   * Returns a <tt>Map</tt> of the output parameters that are defined for this ActionDefinition.
   * 
   * @return <tt>Map</tt> of output parameters. Parameters take the name-value form.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getActionOutputDefinitions();

  /**
   * 
   * Returns the name of the parameter that the passed in name is mapped to in the Action Sequence Document
   * 
   * @param name
   *          String name of the parameter to get a mapping for
   * @return String name of the parameter that 'name' is mapped to
   */
  public String getMappedOutputName( String name );

  /**
   * Returns a <tt>List</tt> of the resource parameter names that are defined for this ActionDefinition.
   * 
   * @return <tt>List</tt> of resource parameter names.
   */
  @SuppressWarnings( "rawtypes" )
  public List getActionResourceDefinitionNames();

  public boolean hasActionResources();

  /**
   * 
   * Returns the name of the parameter that the passed in name is mapped to in the Action Sequence Document
   * 
   * @param name
   *          String name of the parameter to get a mapping for
   * @return String name of the parameter that 'name' is mapped to
   */
  public String getMappedResourceName( String name );

  /**
   * Get the logging level for this ActionDefinition. The logging level may be set independently or may be
   * inherited from a parent object's logging level.
   * 
   * @return this ActionDefinition's logging level
   * @see org.pentaho.platform.api.engine.ILogger
   */
  public int getLoggingLevel();

  /**
   * Returns the list of input and output parameters that will be audited before component execution. This list is
   * handed off to the auditing subsystem as metadata.
   * 
   * @return <tt>List</tt> of parameters defined for pre-execution auditing
   */
  @SuppressWarnings( "rawtypes" )
  public List getPreExecuteAuditList();

  /**
   * Returns the list of input and output parameters that will be audited after component execution. This list is
   * handed off to the auditing subsystem as metadata.
   * 
   * @return <tt>List</tt> of parameters defined for post-execution auditing
   */
  @SuppressWarnings( "rawtypes" )
  public List getPostExecuteAuditList();

  /**
   * Returns boolean value regarding whether this action is set to execute synchronous or asynchronously.
   * 
   * @return true, if set to asynchronous, false if set to synchronous
   */
  public boolean getSyncPreference();

  /**
   * Returns the Java class name of the Component that this ActionDefinition is created for.
   * 
   * @return the Java class name of Component for this ActionDefinition
   */
  public String getComponentName();

  /**
   * Returns the Component definition portion of this ActionDefinition. The Component section typically describes
   * that data and metadata that is relevant only to that particular component.
   * 
   * @return the Component definition section of the ActionDefinition
   */
  public Node getComponentSection();

  public Node getNode();

  /**
   * Returns the Component object that this ActionDefinition belongs to.
   * 
   * @return the definition's Component object
   */
  public IComponent getComponent();

  /**
   * Returns the author of the ActionDefinition, if defined, or null otherwise.
   * 
   * @return this definition's author, or null if not defined.
   */
  public String getAuthor();

  /**
   * Returns the description of the ActionDefinition, if defined, or null otherwise.
   * 
   * @return this definition's description, or null if not defined.
   */
  public String getDescription();

  /**
   * Returns the URL to the Help page for this definition.
   * 
   * @return the definition's Help URL
   */
  public String getHelp();

  /**
   * Returns the URL to the icon for this definition.
   * 
   * @return the definition's icon URL
   */
  public String getIconUrl();

  /**
   * Sets the Component object that this definition will belong to. The component must be valid for this
   * ActionDefinition, otherwise execution validation will fail.
   * 
   * @param component
   *          the Component that is valid for this definiton.
   */
  public void setComponent( IComponent component );

}
