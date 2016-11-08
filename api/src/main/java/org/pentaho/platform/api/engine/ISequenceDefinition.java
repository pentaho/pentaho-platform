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

import java.util.Map;

/**
 * The SequenceDefinition represents the definition of several consecutive action definitions that comprise an
 * ActionSequence object, which is the runtime equivalent of an action sequence document.
 * <p>
 * A SequenceDefinition can contain one to many ActionDefinitions that, when executed by their constituent
 * Components, flow automatically from one ActionDefinition to the next.
 * 
 */
public interface ISequenceDefinition {

  /**
   * return code indicating a valid SequenceDefinition
   */
  public static final int ACTION_SEQUENCE_DEFINITION_OK = 0;

  /**
   * return code indicating a problem with the action sequences's XML
   */
  public static final int ACTION_SEQUENCE_DEFINITION_INVALID_XML = 1;

  /**
   * return code indicating an action document that does validate against the action document schema
   */
  public static final int ACTION_SEQUENCE_DEFINITION_INVALID_ACTION_DOC = 2;

  /**
   * result-type value indicating the action should be not be displayed to users during navigation
   */
  public static final String RESULT_TYPE_NONE = "none"; //$NON-NLS-1$

  /**
   * result-type value indicating the action should be be displayed as a report to users during navigation
   */
  public static final String RESULT_TYPE_REPORT = "report"; //$NON-NLS-1$

  /**
   * result-type value indicating the action should be be displayed as a business rule to users during navigation
   */
  public static final String RESULT_TYPE_RULE = "rule"; //$NON-NLS-1$

  /**
   * result-type value indicating the action should be be displayed as a process to users during navigation
   */
  public static final String RESULT_TYPE_PROCESS = "process"; //$NON-NLS-1$

  /**
   * Returns a Map of the input parameters that are defined to this SequenceDefinition. These inputs are part of
   * the contract between this sequence definition and the platform subsystems, ie., the runtime context.
   * 
   * @return Map of input parameters. Parameters take the name-value form.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getInputDefinitions();

  /**
   * Returns a Map of the input parameters that are defined to this SequenceDefinition for a specific parameter
   * provider. For example the input named "REGION" may come from a request parameter named "regn" Calling this
   * method passing in "request" for the parameterproviderName will return a map that includes an entry with the
   * key "regn" mapped to the "REGION" IActionParameter.
   * 
   * @param parameterProviderName
   *          The name of the parameter provider e.g. "request", "session", "global"...
   * 
   * @return Map of input parameters. Parameters take the name-value form.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getInputDefinitionsForParameterProvider( String parameterProviderName );

  /**
   * Returns a Map of the output parameters that are defined to this SequenceDefinition. These outputs are part of
   * the contract between this sequence definition and the platform subsystems, ie., the runtime context.
   * 
   * @return Map of output parameters. Parameters take the name-value form.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getOutputDefinitions();

  /**
   * Returns the sequence's resource definitions as a Map. Resources are elements that exist outside of the action
   * sequence document, such as images, icons, additional definition documents, etc.
   * 
   * @return Map of resource parameters. Parameters take the name-value form.
   */
  @SuppressWarnings( "rawtypes" )
  public Map getResourceDefinitions();

  /**
   * Returns the document name of the action sequence document that this SequenceDefinition came from.
   * 
   * @return the action sequence document name
   */
  public String getSequenceName();

  /**
   * Returns the type of the overall result of executing the action sequence document that this SequenceDefinition
   * came from. For example if the sequence results in the generation of a report the result type for the sequence
   * should be RESULT_TYPE_REPORT. This property is used to select icons to show next to the sequence name when
   * users navigate the available actions. Tif this returns RESULT_TYPE_NONE, empty string or null, the action
   * sequence will not be visible to users as they navigate
   * 
   * @return the action sequence result type
   */
  public String getResultType();

  /**
   * Returns the author of the SequenceDefinition, if defined, or null otherwise.
   * 
   * @return this definition's author, or null if not defined.
   */
  public String getAuthor();

  /**
   * Returns the description of this SequenceDefinition, if defined, or null otherwise.
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
   * Returns the title of this SequenceDefinition, if defined, or null otherwise.
   * 
   * @return this definition's title, or null if not defined.
   */
  public String getTitle();

  /**
   * Returns the solution name, which is the name at the root level of the solution path.
   * 
   * @return the name of the root level of this definition's solution
   */
  public String getSolutionName();

  /**
   * Returns the path relative to the solution name that will lead to this definition
   * 
   * @return the solution path to this definition
   */
  public String getSolutionPath();

  /**
   * Get the logging level for this SequenceDefinition. The logging level may be set independently or may be
   * inherited from a parent object's logging level.
   * 
   * @return this SequenceDefinition's logging level
   * 
   * @see org.pentaho.platform.api.engine.ILogger
   */
  public int getLoggingLevel();

  /**
   * Returns the path to the icon for this SequenceDefinition. The path can be relative or absolute In the
   * pre-configured install these paths are URLs of the form /style/icons/iconname.png This path is used by the
   * navigation XSL to generate the navigation user interface. If this property is not set a generic icon is used.
   * 
   * @return the url to the icon
   */
  public String getIcon();

}
