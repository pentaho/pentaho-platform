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

package org.pentaho.platform.engine.services.actionsequence;

import org.apache.commons.collections.map.ListOrderedMap;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ISequenceDefinition;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionDefinition implements ISolutionActionDefinition {

  private int errorCode;

  private int loggingLevel;

  // private boolean audit;
  private List preExecuteAuditList;

  private List postExecuteAuditList;

  // private ISequenceDefinition sequenceData;

  private String description;

  private String author;

  private String help;

  private String iconUrl;

  private Node componentNode;

  private Node actionRootNode;

  private IComponent component;

  private String componentName;

  private Map actionInputDefinitions;

  private Map actionInputMapping;

  private Map actionOutputDefinitions;

  private Map actionOutputMapping;

  private Map actionResourceMapping;

  private boolean hasActionResources = false;

  public ActionDefinition( final Node actionRootNode, final ILogger logger ) {

    this.actionRootNode = actionRootNode;

    errorCode = ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_OK;
    // this.sequenceData = sequenceData;

    // get the input parameter definitions
    actionInputDefinitions = new ListOrderedMap();
    actionInputMapping = new ListOrderedMap();
    errorCode =
        SequenceDefinition.parseParameters( actionRootNode, logger,
            "action-inputs/*", actionInputDefinitions, actionInputMapping, true ); //$NON-NLS-1$

    // get the ouput definitions
    actionOutputDefinitions = new ListOrderedMap();
    actionOutputMapping = new ListOrderedMap();
    errorCode =
        SequenceDefinition.parseParameters( actionRootNode, logger,
            "action-outputs/*", actionOutputDefinitions, actionOutputMapping, false ); //$NON-NLS-1$

    // get the resource definitions
    actionResourceMapping = new ListOrderedMap();
    if ( actionRootNode.selectNodes( "action-resources/*" ).size() > 0 ) { //$NON-NLS-1$
      hasActionResources = true;
      errorCode =
          SequenceDefinition.parseActionResourceDefinitions( actionRootNode, logger,
              "action-resources/*", actionResourceMapping ); //$NON-NLS-1$
    }

    componentName = XmlDom4JHelper.getNodeText( "component-name", actionRootNode ); //$NON-NLS-1$
    String loggingLevelString = XmlDom4JHelper.getNodeText( "logging-level", actionRootNode ); //$NON-NLS-1$
    loggingLevel = Logger.getLogLevel( loggingLevelString );

    // get the component payload
    componentNode = actionRootNode.selectSingleNode( "component-definition" ); //$NON-NLS-1$
    if ( componentNode == null ) {
      componentNode = ( (Element) actionRootNode ).addElement( "component-definition" ); //$NON-NLS-1$
    }

    // TODO populate preExecuteAuditList and postExecuteAuditList
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getMappedInputName( final String name ) {
    return ( (String) actionInputMapping.get( name ) );
  }

  public Map getActionInputDefinitions() {
    return actionInputDefinitions;
  }

  public String getMappedOutputName( final String name ) {
    return ( (String) actionOutputMapping.get( name ) );
  }

  public Map getActionOutputDefinitions() {
    return actionOutputDefinitions;
  }

  public String getMappedResourceName( final String name ) {
    return ( (String) actionResourceMapping.get( name ) );
  }

  public List getActionResourceDefinitionNames() {
    return ( new ArrayList( actionResourceMapping.keySet() ) );
  }

  public boolean hasActionResources() {
    return ( hasActionResources );
  }

  public void setLoggingLevel( final int loggingLevel ) {
    this.loggingLevel = loggingLevel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IActionDefinition#getComponentName()
   */
  public String getComponentName() {
    // TODO Auto-generated method stub
    return componentName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IActionDefinition#getComponentSection()
   */
  public Node getComponentSection() {
    // TODO Auto-generated method stub
    return componentNode;
  }

  public Node getNode() {
    return actionRootNode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IActionDefinition#getSyncPreference()
   */
  public boolean getSyncPreference() {
    // TODO Auto-generated method stub
    return false;
  }

  public int getLoggingLevel() {
    return loggingLevel;
  }

  public List getPostExecuteAuditList() {
    return preExecuteAuditList;
  }

  public List getPreExecuteAuditList() {
    return postExecuteAuditList;
  }

  public IComponent getComponent() {
    return component;
  }

  public void setComponent( final IComponent component ) {
    this.component = component;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getAuthor() {
    return author;
  }

  public String getDescription() {
    return description;
  }

  public String getHelp() {
    return help;
  }

}
