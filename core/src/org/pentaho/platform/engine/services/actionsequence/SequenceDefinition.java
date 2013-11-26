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
import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IConditionalExecution;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ISequenceDefinition;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SequenceDefinition implements ISequenceDefinition {

  private static final boolean debug = PentahoSystem.debug;

  private int errorCode;

  private String xactionPath;

  private String version;

  private String title;

  private boolean isWebService;

  private String cacheLevel;

  private int loggingLevel;

  private String description;

  private String author;

  private String help;

  private String resultType;

  private String iconPath;

  private Map outputDefinitions;

  private Map inputDefinitions;

  private Map resourceDefinitions;

  IApplicationContext applicationContext;

  ISolutionActionDefinition[] actionDefinitions;

  public static IActionSequence ActionSequenceFactory( final Document document, final String solutionPath,
      final ILogger logger, final IApplicationContext applicationContext, final int loggingLevel ) {

    // Check for a sequence document
    Node sequenceDefinitionNode = document.selectSingleNode( "//action-sequence" ); //$NON-NLS-1$
    if ( sequenceDefinitionNode == null ) {
      logger.error( Messages.getInstance().getErrorString(
          "SequenceDefinition.ERROR_0002_NO_ACTION_SEQUENCE_NODE", "", solutionPath, "" ) ); //$NON-NLS-1$
      return null;
    }

    ISequenceDefinition seqDef =
        new SequenceDefinition( sequenceDefinitionNode, solutionPath, logger, applicationContext );

    Node actionNode = sequenceDefinitionNode.selectSingleNode( "actions" ); //$NON-NLS-1$

    return ( SequenceDefinition.getNextLoopGroup( seqDef, actionNode, solutionPath, logger, loggingLevel ) );
  }

  private static IActionSequence getNextLoopGroup( final ISequenceDefinition seqDef, final Node actionsNode,
      final String solutionPath, final ILogger logger, final int loggingLevel ) {

    String loopParameterName = XmlDom4JHelper.getNodeText( "@loop-on", actionsNode ); //$NON-NLS-1$
    boolean loopUsingPeek = "true".equalsIgnoreCase( XmlDom4JHelper.getNodeText( "@peek-only", actionsNode ) ); //$NON-NLS-1$ //$NON-NLS-2$

    Node actionDefinitionNode;
    ActionDefinition actionDefinition;

    List actionDefinitionList = new ArrayList();

    List nodeList = actionsNode.selectNodes( "*" ); //$NON-NLS-1$
    Iterator actionDefinitionNodes = nodeList.iterator();
    while ( actionDefinitionNodes.hasNext() ) {
      actionDefinitionNode = (Node) actionDefinitionNodes.next();
      if ( actionDefinitionNode.getName().equals( "actions" ) ) { //$NON-NLS-1$
        actionDefinitionList.add( SequenceDefinition.getNextLoopGroup( seqDef, actionDefinitionNode, solutionPath,
            logger, loggingLevel ) );
      } else if ( actionDefinitionNode.getName().equals( "action-definition" ) ) { //$NON-NLS-1$
        actionDefinition = new ActionDefinition( actionDefinitionNode, logger );
        actionDefinition.setLoggingLevel( loggingLevel );
        actionDefinitionList.add( actionDefinition );
      }
    }
    // action sequences with 0 actions are valid, see: JIRA PLATFORM-837

    IConditionalExecution conditionalExecution =
        SequenceDefinition.parseConditionalExecution( actionsNode, logger, "condition" ); //$NON-NLS-1$

    ActionSequence sequence = new ActionSequence( loopParameterName, seqDef, actionDefinitionList, loopUsingPeek );

    sequence.setConditionalExecution( conditionalExecution );
    return sequence;
  }

  private SequenceDefinition( final Node sequenceRootNode, final String solutionPath, final ILogger logger,
      final IApplicationContext applicationContext ) {

    // initialize this object from the contents of the xml

    this.xactionPath = solutionPath;
    this.applicationContext = applicationContext;

    // get the descriptive entries
    version = XmlDom4JHelper.getNodeText( "version", sequenceRootNode ); //$NON-NLS-1$
    title = XmlDom4JHelper.getNodeText( "title", sequenceRootNode ); //$NON-NLS-1$

    isWebService = "true".equals( XmlDom4JHelper.getNodeText( "web-service", sequenceRootNode ) ); //$NON-NLS-1$ //$NON-NLS-2$
    loggingLevel = Logger.getLogLevel( XmlDom4JHelper.getNodeText( "logging-level", sequenceRootNode ) ); //$NON-NLS-1$

    description = XmlDom4JHelper.getNodeText( "documentation/description", sequenceRootNode ); //$NON-NLS-1$
    help = XmlDom4JHelper.getNodeText( "documentation/help", sequenceRootNode ); //$NON-NLS-1$
    author = XmlDom4JHelper.getNodeText( "documentation/author", sequenceRootNode ); //$NON-NLS-1$
    resultType = XmlDom4JHelper.getNodeText( "documentation/result-type", sequenceRootNode ); //$NON-NLS-1$
    iconPath = XmlDom4JHelper.getNodeText( "documentation/icon", sequenceRootNode ); //$NON-NLS-1$

    // get the input parameter definitions
    inputDefinitions = new ListOrderedMap();
    errorCode = SequenceDefinition.parseParameters( sequenceRootNode, logger, "inputs/*", inputDefinitions, null, true ); //$NON-NLS-1$

    // get the ouput definitions
    outputDefinitions = new ListOrderedMap();
    errorCode =
        SequenceDefinition.parseParameters( sequenceRootNode, logger, "outputs/*", outputDefinitions, null, false ); //$NON-NLS-1$
    if ( errorCode != ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_OK ) {
      logger.info( Messages.getInstance().getString( "SequenceDefinition.INFO_OUTPUT_PARAMETERS_NOT_DEFINED" ) ); //$NON-NLS-1$      
    }
    // get the resource definitions
    errorCode = parseResourceDefinitions( sequenceRootNode, logger );
    if ( errorCode != ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_OK ) {
      logger.info( Messages.getInstance().getString( "SequenceDefinition.INFO_RESOURCES_PARAMETERS_NOT_DEFINED" ) ); //$NON-NLS-1$     
    }
  }

  public String getVersion() {
    return version;
  }

  public boolean isWebService() {
    return isWebService;
  }

  public String getCacheLevel() {
    return cacheLevel;
  }

  public int getErrorCode() {
    return errorCode;
  }

  static IConditionalExecution parseConditionalExecution( final Node actionRootNode, final ILogger logger,
      final String nodePath ) {
    try {
      Node condition = actionRootNode.selectSingleNode( nodePath );
      if ( condition == null ) {
        return null;
      }
      String script = condition.getText();
      IConditionalExecution ce = PentahoSystem.get( IConditionalExecution.class, null );
      ce.setScript( script );
      return ce;
    } catch ( Exception ex ) {
      logger.error( Messages.getInstance().getErrorString( "SequenceDefinition.ERROR_0005_PARSING_PARAMETERS" ), ex ); //$NON-NLS-1$
    }
    return null;
  }

  static int parseParameters( final Node actionRootNode, final ILogger logger, final String nodePath,
      final Map parameterMap, final Map mapTo, final boolean inputVar ) {
    try {
      List parameters = actionRootNode.selectNodes( nodePath );

      // TODO create objects to represent the types
      // TODO need source variable list
      Iterator parametersIterator = parameters.iterator();
      Node parameterNode;
      String parameterName;
      String parameterType;
      ActionParameter parameter;
      List variableNodes;
      List variables;
      Node variableNode;
      Iterator variablesIterator;
      String variableSource;
      String variableName;
      int variableIdx;
      Object defaultValue = null;

      while ( parametersIterator.hasNext() ) {
        parameterNode = (Node) parametersIterator.next();
        parameterName = parameterNode.getName();
        parameterType = XmlDom4JHelper.getNodeText( "@type", parameterNode ); //$NON-NLS-1$

        if ( mapTo != null ) {
          mapTo.put( parameterName, XmlDom4JHelper.getNodeText( "@mapping", parameterNode, parameterName ) ); //$NON-NLS-1$
        }

        defaultValue = SequenceDefinition.getDefaultValue( parameterNode );
        // get the list of sources for this parameter
        variableNodes = parameterNode.selectNodes( ( inputVar ) ? "sources/*" : "destinations/*" ); //$NON-NLS-1$ //$NON-NLS-2$
        variablesIterator = variableNodes.iterator();
        variableIdx = 1;
        variables = new ArrayList();
        while ( variablesIterator.hasNext() ) {
          variableNode = (Node) variablesIterator.next();
          // try to resolve the parameter value for this
          try {
            variableSource = variableNode.getName();
            variableName = variableNode.getText();
            ActionParameterSource variable = new ActionParameterSource( variableSource, variableName );
            if ( SequenceDefinition.debug ) {
              logger.debug( Messages.getInstance().getString(
                  "SequenceDefinition.DEBUG_ADDING_SOURCE_FOR_PARAMETER", variableSource, parameterName ) ); //$NON-NLS-1$
            }

            variables.add( variable );
          } catch ( Exception e ) {
            logger
                .error(
                    Messages
                        .getInstance()
                        .getErrorString(
                            "SequenceDefinition.ERROR_0004_VARIABLE_SOURCE_NOT_VALID", Integer.toString( variableIdx ), parameterName ), e ); //$NON-NLS-1$
          }
          variableIdx++;
        }
        if ( defaultValue != null ) {
          if ( SequenceDefinition.debug ) {
            logger.debug( Messages.getInstance().getString(
                "SequenceDefinition.DEBUG_USING_DEFAULT_VALUE", defaultValue.toString(), parameterName ) ); //$NON-NLS-1$
          }
        }
        boolean isOutputParameter =
            Boolean.parseBoolean( XmlDom4JHelper.getNodeText( "@is-output-parameter", parameterNode, "true" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        parameter = new ActionParameter( parameterName, parameterType, null, variables, defaultValue );
        parameter.setOutputParameter( isOutputParameter );
        parameterMap.put( parameterName, parameter );
      }
      return ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_OK;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString( "SequenceDefinition.ERROR_0005_PARSING_PARAMETERS" ), e ); //$NON-NLS-1$
    }

    return ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_INVALID_ACTION_DOC;
  }

  private int parseResourceDefinitions( final Node actionRootNode, final ILogger logger ) {

    resourceDefinitions = new ListOrderedMap();

    try {
      List resources = actionRootNode.selectNodes( "resources/*" ); //$NON-NLS-1$

      // TODO create objects to represent the types
      // TODO need source variable list
      Iterator resourcesIterator = resources.iterator();

      Node resourceNode;
      String resourceName;
      String resourceTypeName;
      String resourceMimeType;
      int resourceType;
      ActionSequenceResource resource;
      Node typeNode, mimeNode;
      while ( resourcesIterator.hasNext() ) {
        resourceNode = (Node) resourcesIterator.next();
        typeNode = resourceNode.selectSingleNode( "./*" ); //$NON-NLS-1$
        if ( typeNode != null ) {
          resourceName = resourceNode.getName();
          resourceTypeName = typeNode.getName();
          resourceType = ActionSequenceResource.getResourceType( resourceTypeName );
          String resourceLocation = XmlDom4JHelper.getNodeText( "location", typeNode ); //$NON-NLS-1$
          if ( ( resourceType == IActionSequenceResource.SOLUTION_FILE_RESOURCE )
              || ( resourceType == IActionSequenceResource.FILE_RESOURCE ) ) {
            if ( resourceLocation == null ) {
              logger.error( Messages.getInstance().getErrorString(
                  "SequenceDefinition.ERROR_0008_RESOURCE_NO_LOCATION", resourceName ) ); //$NON-NLS-1$
              continue;
            }
          } else if ( resourceType == IActionSequenceResource.STRING ) {
            resourceLocation = XmlDom4JHelper.getNodeText( "string", resourceNode ); //$NON-NLS-1$
          } else if ( resourceType == IActionSequenceResource.XML ) {
            //resourceLocation = XmlHelper.getNodeText("xml", resourceNode); //$NON-NLS-1$
            Node xmlNode = typeNode.selectSingleNode( "./location/*" ); //$NON-NLS-1$
            // Danger, we have now lost the character encoding of the XML in this node
            // see BISERVER-895
            resourceLocation = ( xmlNode == null ) ? "" : xmlNode.asXML(); //$NON-NLS-1$
          }
          mimeNode = typeNode.selectSingleNode( "mime-type" ); //$NON-NLS-1$
          if ( mimeNode != null ) {
            resourceMimeType = mimeNode.getText();
            if ( ( resourceType == IActionSequenceResource.SOLUTION_FILE_RESOURCE )
                || ( resourceType == IActionSequenceResource.FILE_RESOURCE ) ) {
              resourceLocation = FilenameUtils.separatorsToUnix( resourceLocation );
              if ( !resourceLocation.startsWith( "/" ) ) { //$NON-NLS-1$
                String parentDir = FilenameUtils.getFullPathNoEndSeparator( xactionPath );
                if ( parentDir.length() == 0 ) {
                  parentDir = RepositoryFile.SEPARATOR;
                }
                resourceLocation = FilenameUtils.separatorsToUnix( FilenameUtils.concat( parentDir,
                  resourceLocation ) );
              }
            }
            resource = new ActionSequenceResource( resourceName, resourceType, resourceMimeType, resourceLocation );
            resourceDefinitions.put( resourceName, resource );
          } else {
            logger.error( Messages.getInstance().getErrorString(
                "SequenceDefinition.ERROR_0007_RESOURCE_NO_MIME_TYPE", resourceName ) ); //$NON-NLS-1$
          }
        }
        // input = new ActionParameter( resourceName, resourceType, null
        // );
        // resourceDefinitions.put( inputName, input );
      }
      return ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_OK;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString( "SequenceDefinition.ERROR_0006_PARSING_RESOURCE" ), e ); //$NON-NLS-1$               
    }
    return ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_INVALID_ACTION_DOC;

  }

  /**
   * sbarkdull: method appears to never be used anywhere
   * 
   * @param actionRootNode
   * @param logger
   * @param nodePath
   * @param mapTo
   * @return
   */
  static int parseActionResourceDefinitions( final Node actionRootNode, final ILogger logger, final String nodePath,
      final Map mapTo ) {

    try {
      List resources = actionRootNode.selectNodes( nodePath );

      // TODO create objects to represent the types
      // TODO need source variable list
      Iterator resourcesIterator = resources.iterator();

      Node resourceNode;
      String resourceName;
      while ( resourcesIterator.hasNext() ) {
        resourceNode = (Node) resourcesIterator.next();
        resourceName = resourceNode.getName();
        if ( mapTo != null ) {
          mapTo.put( resourceName, XmlDom4JHelper.getNodeText( "@mapping", resourceNode, resourceName ) ); //$NON-NLS-1$
        }
      }
      return ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_OK;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString( "SequenceDefinition.ERROR_0006_PARSING_RESOURCE" ), e ); //$NON-NLS-1$               
    }
    return ISequenceDefinition.ACTION_SEQUENCE_DEFINITION_INVALID_ACTION_DOC;

  }

  private static Object getDefaultValue( final Node parameterNode ) {
    Node rootNode = parameterNode.selectSingleNode( "default-value" ); //$NON-NLS-1$
    if ( rootNode == null ) {
      return ( null );
    }

    String dataType = XmlDom4JHelper.getNodeText( "@type", rootNode ); //$NON-NLS-1$
    if ( dataType == null ) {
      dataType = XmlDom4JHelper.getNodeText( "@type", parameterNode ); //$NON-NLS-1$
    }

    if ( "string-list".equals( dataType ) ) { //$NON-NLS-1$
      List nodes = rootNode.selectNodes( "list-item" ); //$NON-NLS-1$
      if ( nodes == null ) {
        return ( null );
      }

      ArrayList rtnList = new ArrayList();
      for ( Iterator it = nodes.iterator(); it.hasNext(); ) {
        rtnList.add( ( (Node) it.next() ).getText() );
      }
      return ( rtnList );
    } else if ( "property-map-list".equals( dataType ) ) { //$NON-NLS-1$
      List nodes = rootNode.selectNodes( "property-map" ); //$NON-NLS-1$
      if ( nodes == null ) {
        return ( null );
      }

      ArrayList rtnList = new ArrayList();
      for ( Iterator it = nodes.iterator(); it.hasNext(); ) {
        Node mapNode = (Node) it.next();
        rtnList.add( SequenceDefinition.getMapFromNode( mapNode ) );
      }
      return ( rtnList );
    } else if ( "property-map".equals( dataType ) ) { //$NON-NLS-1$
      return ( SequenceDefinition.getMapFromNode( rootNode.selectSingleNode( "property-map" ) ) ); //$NON-NLS-1$
    } else if ( "long".equals( dataType ) ) { //$NON-NLS-1$
      try {
        return ( new Long( rootNode.getText() ) );
      } catch ( Exception e ) {
        //ignore
      }
      return ( null );
    } else if ( "result-set".equals( dataType ) ) { //$NON-NLS-1$

      return ( MemoryResultSet.createFromActionSequenceInputsNode( parameterNode ) );
    } else { // Assume String
      return ( rootNode.getText() );
    }

  }

  private static Map getMapFromNode( final Node mapNode ) {
    Map rtnMap = new ListOrderedMap();

    if ( mapNode != null ) {
      List nodes = mapNode.selectNodes( "entry" ); //$NON-NLS-1$
      if ( nodes != null ) {
        for ( Iterator it = nodes.iterator(); it.hasNext(); ) {
          Node entryNode = (Node) it.next();
          rtnMap.put( XmlDom4JHelper.getNodeText( "@key", entryNode ), entryNode.getText() ); //$NON-NLS-1$
        }
      }
    }
    return ( rtnMap );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IActionDefinition#getParamDefs()
   */
  public Map getInputDefinitions() {
    return inputDefinitions;
  }

  public Map getInputDefinitionsForParameterProvider( final String parameterProviderName ) {
    Map rtnMap = new ListOrderedMap();

    Map paramList = getInputDefinitions();
    for ( Iterator it = paramList.values().iterator(); it.hasNext(); ) {
      IActionParameter actionParameter = (IActionParameter) it.next();
      List vars = actionParameter.getVariables();
      for ( int i = 0; i < vars.size(); i++ ) {
        ActionParameterSource source = (ActionParameterSource) ( vars.get( i ) );
        if ( source.getSourceName().equals( parameterProviderName ) ) {
          rtnMap.put( source.getValue(), actionParameter );
        }
      }
    }
    return ( rtnMap );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IActionDefinition#getOutputDefs()
   */
  public Map getOutputDefinitions() {
    return outputDefinitions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IActionDefinition#getResourceDefs()
   */
  public Map getResourceDefinitions() {
    return resourceDefinitions;
  }

  public String getSequenceName() {
    return FilenameUtils.getName( xactionPath );
  }

  public String getAuthor() {
    return author;
  }

  public String getDescription() {
    return description;
  }

  public String getResultType() {
    return resultType;
  }

  public String getHelp() {
    return help;
  }

  public String getTitle() {
    return title;
  }

  public String getSolutionName() {
    return "";
  }

  public String getSolutionPath() {
    return xactionPath;
  }

  public int getLoggingLevel() {
    return ( loggingLevel );
  }

  public String getIcon() {
    return iconPath;
  }

}
