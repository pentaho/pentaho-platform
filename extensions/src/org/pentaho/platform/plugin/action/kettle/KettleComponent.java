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

package org.pentaho.platform.plugin.action.kettle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandlerCache;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * KettleComponent shows a list of available transformations in the root of the choosen repository.
 *
 * @author Matt
 */

/*
 * Legitimate outputs: EXECUTION_STATUS_OUTPUT - (execution-status) [JOB | TRANS] Returns the resultant execution status
 * 
 * EXECUTION_LOG_OUTPUT - (execution-log) [JOB | TRANS] Returns the resultant log
 * 
 * TRANSFORM_SUCCESS_OUTPUT - (transformation-written) [Requires MONITORSTEP to be defined] [TRANS] Returns a
 * "result-set" for all successful rows written (Unless error handling is not defined for the specified step, in which
 * case ALL rows are returned here)
 * 
 * TRANSFORM_ERROR_OUTPUT - (transformation-errors) [Requires MONITORSTEP to be defined] [TRANS] Returns a "result-set"
 * for all rows written that have caused an error
 * 
 * TRANSFORM_SUCCESS_COUNT_OUTPUT - (transformation-written-count) [Requires MONITORSTEP to be defined] [TRANS] Returns
 * a count of all rows returned in TRANSFORM_SUCCESS_OUTPUT
 * 
 * TRANSFORM_ERROR_COUNT_OUTPUT - (transformation-errors-count) [Requires MONITORSTEP to be defined] [TRANS] Returns a
 * count of all rows returned in TRANSFORM_ERROR_OUTPUT
 * 
 * Legitimate inputs: MONITORSTEP Takes the name of the step from which success and error rows can be detected
 * 
 * KETTLELOGLEVEL Sets the logging level to be used in the EXECUTION_LOG_OUTPUT Valid settings: basic detail error debug
 * minimal rowlevel
 */
public class KettleComponent extends ComponentBase implements RowListener {

  private static final long serialVersionUID = 8217343898202366129L;

  private static final String DIRECTORY = "directory"; //$NON-NLS-1$

  private static final String TRANSFORMATION = "transformation"; //$NON-NLS-1$

  private static final String JOB = "job"; //$NON-NLS-1$

  private static final String TRANSFORMFILE = "transformation-file"; //$NON-NLS-1$

  private static final String JOBFILE = "job-file"; //$NON-NLS-1$

  // IMPORTSTEP here for backwards compatibility; Superceded by MONITORSTEP
  private static final String IMPORTSTEP = "importstep"; //$NON-NLS-1$

  private static final String MONITORSTEP = "monitor-step"; //$NON-NLS-1$

  private static final String KETTLELOGLEVEL = "kettle-logging-level"; //$NON-NLS-1$

  private static final String EXECUTION_STATUS_OUTPUT = "kettle-execution-status"; //$NON-NLS-1$

  private static final String EXECUTION_LOG_OUTPUT = "kettle-execution-log"; //$NON-NLS-1$

  private static final String TRANSFORM_SUCCESS_OUTPUT = "transformation-output-rows"; //$NON-NLS-1$

  private static final String TRANSFORM_ERROR_OUTPUT = "transformation-output-error-rows"; //$NON-NLS-1$

  private static final String TRANSFORM_SUCCESS_COUNT_OUTPUT = "transformation-output-rows-count"; //$NON-NLS-1$

  private static final String TRANSFORM_ERROR_COUNT_OUTPUT = "transformation-output-error-rows-count"; //$NON-NLS-1$

  public static final String PARAMETER_MAP_CMD_ARG = "set-argument"; //$NON-NLS-1$

  public static final String PARAMETER_MAP_VARIABLE = "set-variable"; //$NON-NLS-1$

  public static final String PARAMETER_MAP_PARAMETER = "set-parameter"; //$NON-NLS-1$

  private static final ArrayList<String> outputParams = new ArrayList<String>( Arrays.asList( EXECUTION_STATUS_OUTPUT,
      EXECUTION_LOG_OUTPUT, TRANSFORM_SUCCESS_OUTPUT, TRANSFORM_ERROR_OUTPUT, TRANSFORM_SUCCESS_COUNT_OUTPUT,
      TRANSFORM_ERROR_COUNT_OUTPUT ) );

  /**
   * The repositories.xml file location, if empty take the default $HOME/.kettle/repositories.xml
   */
  private String repositoriesXMLFile;

  /**
   * The name of the repository to use
   */
  private String repositoryName;

  /**
   * The username to login with
   */
  private String username;

  private MemoryResultSet results;

  private MemoryResultSet errorResults;

  private String executionStatus;

  private String executionLog;

  /**
   * The password to login with
   */
  private String password;

  /**
   * The log channel ID of the executing transformation or job
   */
  private String logChannelId;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( KettleComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // set pentaho.solutionpath so that it can be used in file paths
    boolean useRepository =
        PentahoSystem.getSystemSetting( "kettle/settings.xml", "repository.type", "files" ).equals( "rdbms" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    if ( useRepository ) {
      repositoriesXMLFile = PentahoSystem.getSystemSetting( "kettle/settings.xml", "repositories.xml.file", null ); //$NON-NLS-1$ //$NON-NLS-2$
      repositoryName = PentahoSystem.getSystemSetting( "kettle/settings.xml", "repository.name", null ); //$NON-NLS-1$ //$NON-NLS-2$
      username = PentahoSystem.getSystemSetting( "kettle/settings.xml", "repository.userid", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      password = PentahoSystem.getSystemSetting( "kettle/settings.xml", "repository.password", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // Check the Kettle settings...
      if ( "".equals( repositoryName ) || username.equals( "" ) ) { //$NON-NLS-1$ //$NON-NLS-2$
        // looks like the Kettle stuff is not configured yet...
        // see if we can provide feedback to the user...

        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0001_SERVER_SETTINGS_NOT_SET" ) ); //$NON-NLS-1$
        return false;
      }

      boolean ok = ( ( repositoryName != null ) && ( repositoryName.length() > 0 ) );
      ok = ok || ( ( username != null ) && ( username.length() > 0 ) );

      return ok;
    }
    return true;
  }

  @Override
  public boolean init() {
    LogChannel kettleComponentChannel = new LogChannel( "Kettle platform component" );
    logChannelId = kettleComponentChannel.getLogChannelId();
    return true;

  }

  private boolean checkMapping( Node name, Node mapping ) {
    if ( name == null ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0031_NAME_ELEMENT_MISSING_FROM_MAPPING" ) ); //$NON-NLS-1$
      return false;
    }
    if ( mapping == null ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0032_MAPPING_ELEMENT_MISSING_FROM_MAPPING" ) ); //$NON-NLS-1$
      return false;
    }

    // Make sure the mapping field is available as an input
    if ( !isDefinedInput( mapping.getText() ) ) {
      error( Messages.getInstance().getErrorString(
          "Kettle.ERROR_0033_MAPPING_NOT_FOUND_IN_ACTION_INPUTS", mapping.getText() ) ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @SuppressWarnings ( "unchecked" )
  @Override
  public boolean validateAction() {

    // If there are any mappings, validate their xml and values
    if ( getComponentDefinition().selectNodes(
        PARAMETER_MAP_CMD_ARG + " | " + PARAMETER_MAP_VARIABLE + " | " + PARAMETER_MAP_PARAMETER ).size() > 0 ) { //$NON-NLS-1$ //$NON-NLS-2$
      Map<String, String> argumentMap = null;

      Node name = null, mapping = null;

      // Extract all mapping elements from component-definition and verify
      // they have a 'name' and 'mapping' child element
      for ( Node n : (List<Node>) getComponentDefinition().selectNodes( PARAMETER_MAP_CMD_ARG ) ) {
        name = n.selectSingleNode( "name" ); //$NON-NLS-1$
        mapping = n.selectSingleNode( "mapping" ); //$NON-NLS-1$
        if ( checkMapping( name, mapping ) ) {
          if ( argumentMap == null ) {
            argumentMap = new HashMap<String, String>();
          }
          argumentMap.put( name.getText(), applyInputsToFormat( getInputStringValue( mapping.getText() ) ) );
        } else {
          return false;
        }
      }

      for ( Node n : (List<Node>) getComponentDefinition().selectNodes( PARAMETER_MAP_VARIABLE ) ) {
        name = n.selectSingleNode( "name" ); //$NON-NLS-1$
        mapping = n.selectSingleNode( "mapping" ); //$NON-NLS-1$
        if ( !checkMapping( name, mapping ) ) {
          return false;
        }
      }

      for ( Node n : (List<Node>) getComponentDefinition().selectNodes( PARAMETER_MAP_PARAMETER ) ) {
        name = n.selectSingleNode( "name" ); //$NON-NLS-1$
        mapping = n.selectSingleNode( "mapping" ); //$NON-NLS-1$
        if ( !checkMapping( name, mapping ) ) {
          return false;
        }
      }

      // Make sure all of the arguments are present, correctly labeled and
      // that there are not more then 10 (currently supported by Kettle)
      if ( argumentMap != null ) {
        String val = null;
        for ( int i = 1; i <= argumentMap.size(); i++ ) {
          val = argumentMap.get( Integer.toString( i ) );
          if ( val == null ) {
            error( Messages.getInstance().getErrorString( "Kettle.ERROR_0030_INVALID_ARGUMENT_MAPPING" ) ); //$NON-NLS-1$
            return false;
          }
        }
      }
    }

    if ( isDefinedResource( KettleComponent.TRANSFORMFILE ) || isDefinedResource( KettleComponent.JOBFILE ) ) {
      return true;
    }

    boolean useRepository =
        PentahoSystem.getSystemSetting( "kettle/settings.xml", "repository.type", "files" ).equals( "rdbms" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if ( !useRepository ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0019_REPOSITORY_TYPE_FILES" ) ); //$NON-NLS-1$
      return false;
    }

    if ( isDefinedInput( KettleComponent.DIRECTORY )
        && ( isDefinedInput( KettleComponent.TRANSFORMATION ) || isDefinedInput( KettleComponent.JOB ) ) ) {
      return true;
    }

    if ( !isDefinedInput( KettleComponent.DIRECTORY ) ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0002_DIR_OR_FILE__NOT_DEFINED", getActionName() ) ); //$NON-NLS-1$
      return false;
    } else {
      if ( !isDefinedInput( KettleComponent.TRANSFORMATION ) ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0003_TRANS_NOT_DEFINED", getActionName() ) ); //$NON-NLS-1$
        return false;
      }
    }

    return false;

  }

  /**
   * Execute the specified transformation in the chosen repository.
   */
  @SuppressWarnings ( "unchecked" )
  @Override
  public boolean executeAction() {

    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "Kettle.DEBUG_START" ) ); //$NON-NLS-1$
    }

    TransMeta transMeta = null;
    JobMeta jobMeta = null;

    // Build lists of parameters, variables and command line arguments

    Map<String, String> argumentMap = new HashMap<String, String>();
    Map<String, String> variableMap = new HashMap<String, String>();
    Map<String, String> parameterMap = new HashMap<String, String>();

    for ( Node n : (List<Node>) getComponentDefinition().selectNodes( PARAMETER_MAP_CMD_ARG ) ) {
      argumentMap
          .put(
              n.selectSingleNode( "name" ).getText(),
              applyInputsToFormat( getInputStringValue( n.selectSingleNode( "mapping" ).getText() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    for ( Node n : (List<Node>) getComponentDefinition().selectNodes( PARAMETER_MAP_VARIABLE ) ) {
      variableMap
          .put(
              n.selectSingleNode( "name" ).getText(),
              applyInputsToFormat( getInputStringValue( n.selectSingleNode( "mapping" ).getText() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    for ( Node n : (List<Node>) getComponentDefinition().selectNodes( PARAMETER_MAP_PARAMETER ) ) {
      parameterMap
          .put(
              n.selectSingleNode( "name" ).getText(),
              applyInputsToFormat( getInputStringValue( n.selectSingleNode( "mapping" ).getText() ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    String[] arguments = null;

    // If no mappings are provided, assume all inputs are command line
    // arguments (This supports the legacy method)
    if ( argumentMap.size() <= 0 && variableMap.size() <= 0 && parameterMap.size() <= 0 ) {
      // this use is now considered obsolete, as we prefer the
      // action-sequence inputs since they
      // now maintain order
      boolean running = true;
      int index = 1;
      ArrayList<String> argumentList = new ArrayList<String>();
      while ( running ) {
        if ( isDefinedInput( "parameter" + index ) ) { //$NON-NLS-1$
          String value = null;
          String inputName = getInputStringValue( "parameter" + index ); //$NON-NLS-1$
          // see if we have an input with this name
          if ( isDefinedInput( inputName ) ) {
            value = getInputStringValue( inputName );
          }
          argumentList.add( value );
        } else {
          running = false;
        }
        index++;
      }

      // this is the preferred way to provide inputs to the
      // KetteComponent, the order of inputs is now preserved
      Iterator<?> inputNamesIter = getInputNames().iterator();
      while ( inputNamesIter.hasNext() ) {
        String name = (String) inputNamesIter.next();
        argumentList.add( getInputStringValue( name ) );
      }

      arguments = (String[]) argumentList.toArray( new String[argumentList.size()] );
    } else {
      // Extract arguments from argumentMap (Throw an error if the
      // sequential ordering is broken)
      arguments = new String[argumentMap.size()];
      for ( int i = 0; i < argumentMap.size(); i++ ) {
        arguments[i] = argumentMap.get( Integer.toString( i + 1 ) ); // Mapping
        // is
        // 1
        // based
        // to
        // match
        // Kettle
        // UI
        if ( arguments[i] == null ) {
          error( Messages.getInstance().getErrorString( "Kettle.ERROR_0030_INVALID_ARGUMENT_MAPPING" ) ); //$NON-NLS-1$
        }
      }
    }

    // initialize environment variables
    try {
      KettleSystemListener.environmentInit( getSession() );
    } catch ( KettleException ke ) {
      error( ke.getMessage(), ke );
    }

    String solutionPath = "solution:";

    Repository repository = connectToRepository();
    boolean result = false;

    try {
      if ( isDefinedInput( KettleComponent.DIRECTORY ) ) {
        String directoryName = getInputStringValue( KettleComponent.DIRECTORY );

        if ( repository == null ) {
          return false;
        }

        if ( isDefinedInput( KettleComponent.TRANSFORMATION ) ) {
          String transformationName = getInputStringValue( KettleComponent.TRANSFORMATION );
          transMeta = loadTransformFromRepository( directoryName, transformationName, repository );
          if ( transMeta != null ) {
            try {
              for ( String key : parameterMap.keySet() ) {
                transMeta.setParameterValue( key, parameterMap.get( key ) );
              }
              for ( String key : variableMap.keySet() ) {
                transMeta.setVariable( key, variableMap.get( key ) );
              }

            } catch ( UnknownParamException e ) {
              error( e.getMessage() );
            }
            transMeta.setArguments( arguments );
          } else {
            return false;
          }
        } else if ( isDefinedInput( KettleComponent.JOB ) ) {
          String jobName = getInputStringValue( KettleComponent.JOB );
          jobMeta = loadJobFromRepository( directoryName, jobName, repository );
          if ( jobMeta != null ) {
            try {
              for ( String key : parameterMap.keySet() ) {
                jobMeta.setParameterValue( key, parameterMap.get( key ) );
              }
              for ( String key : variableMap.keySet() ) {
                jobMeta.setVariable( key, variableMap.get( key ) );
              }

            } catch ( UnknownParamException e ) {
              error( e.getMessage() );
            }
            jobMeta.setArguments( arguments );
          } else {
            return false;
          }
        }
      } else if ( isDefinedResource( KettleComponent.TRANSFORMFILE ) ) {
        IActionSequenceResource transformResource = getResource( KettleComponent.TRANSFORMFILE );
        String fileAddress = getActualFileName( transformResource );

        try {
          if ( fileAddress != null ) { // We have an actual loadable
            // filesystem and file
            transMeta = new TransMeta( fileAddress, repository, true );
            transMeta.setFilename( fileAddress );
          } else if ( repository != null && repository.isConnected() ) {

            fileAddress = transformResource.getAddress();
            // load transformation resource from kettle/settings.xml configured repository
            transMeta = loadTransformFromRepository( FilenameUtils.getPathNoEndSeparator( fileAddress ), FilenameUtils.getBaseName( fileAddress ), repository );
          } else {
            String jobXmlStr = getResourceAsString( getResource( KettleComponent.TRANSFORMFILE ) );
            jobXmlStr = jobXmlStr.replaceAll( "\\$\\{pentaho.solutionpath\\}", solutionPath ); //$NON-NLS-1$
            jobXmlStr = jobXmlStr.replaceAll( "\\%\\%pentaho.solutionpath\\%\\%", solutionPath ); //$NON-NLS-1$
            org.w3c.dom.Document doc = XmlW3CHelper.getDomFromString( jobXmlStr );
            // create a tranformation from the document
            transMeta = new TransMeta( doc.getFirstChild(), repository );
          }
        } catch ( Exception e ) {
          error( Messages.getInstance().getErrorString(
              "Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.TRANSFORMFILE, fileAddress ), e ); //$NON-NLS-1$
          return false;
        }

        /*
         * Unreachable code below... if (transMeta == null) {
         * error(Messages.getInstance().getErrorString("Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.TRANSFORMFILE,
         * fileAddress)); //$NON-NLS-1$ debug(getKettleLog(true)); return false; }
         */

        // Don't forget to set the parameters here as well...
        try {
          for ( String key : parameterMap.keySet() ) {
            transMeta.setParameterValue( key, parameterMap.get( key ) );
          }
          for ( String key : variableMap.keySet() ) {
            transMeta.setVariable( key, variableMap.get( key ) );
          }

        } catch ( UnknownParamException e ) {
          error( e.getMessage() );
        }
        transMeta.setArguments( arguments );
        /*
         * We do not need to concatenate the solutionPath info as the fileAddress has the complete location of the file
         * from start to end. This is to resolve BISERVER-502.
         */
        transMeta.setFilename( fileAddress );

      } else if ( isDefinedResource( KettleComponent.JOBFILE ) ) {
        String fileAddress = ""; //$NON-NLS-1$
        try {
          fileAddress = getResource( KettleComponent.JOBFILE ).getAddress();

          if ( repository != null && repository.isConnected() ) {

            solutionPath = StringUtils.EMPTY;

            // load job resource from kettle/settings.xml configured repository
            jobMeta = loadJobFromRepository( FilenameUtils.getPathNoEndSeparator( fileAddress ), FilenameUtils.getBaseName( fileAddress ), repository );

          } else {

            String jobXmlStr = getResourceAsString( getResource( KettleComponent.JOBFILE ) );
            // String jobXmlStr =
            // XmlW3CHelper.getContentFromSolutionResource(fileAddress);
            jobXmlStr = jobXmlStr.replaceAll( "\\$\\{pentaho.solutionpath\\}", solutionPath ); //$NON-NLS-1$
            jobXmlStr = jobXmlStr.replaceAll( "\\%\\%pentaho.solutionpath\\%\\%", solutionPath ); //$NON-NLS-1$
            org.w3c.dom.Document doc = XmlW3CHelper.getDomFromString( jobXmlStr );
            if ( doc == null ) {
              error( Messages.getInstance().getErrorString(
                  "Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.JOBFILE, fileAddress ) ); //$NON-NLS-1$
              debug( getKettleLog( true ) );
              return false;
            }
            // create a job from the document
            try {
              repository = connectToRepository();
              // if we get a valid repository its great, if not try it
              // without

              jobMeta = new JobMeta( solutionPath + fileAddress, repository );
            } catch ( Exception e ) {
              error( Messages.getInstance().getString( "Kettle.ERROR_0023_NO_META" ), e ); //$NON-NLS-1$
            } finally {
              if ( repository != null ) {
                if ( ComponentBase.debug ) {
                  debug( Messages.getInstance().getString( "Kettle.DEBUG_DISCONNECTING" ) ); //$NON-NLS-1$
                }
                repository.disconnect();
              }
            }
          }
        } catch ( Exception e ) {
          error( Messages.getInstance().getErrorString(
              "Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.JOBFILE, fileAddress ), e ); //$NON-NLS-1$
          return false;
        }
        if ( jobMeta == null ) {
          error( Messages.getInstance().getErrorString(
              "Kettle.ERROR_0015_BAD_RESOURCE", KettleComponent.JOBFILE, fileAddress ) ); //$NON-NLS-1$
          debug( getKettleLog( true ) );
          return false;
        } else {
          try {
            for ( String key : parameterMap.keySet() ) {
              jobMeta.setParameterValue( key, parameterMap.get( key ) );
            }
            for ( String key : variableMap.keySet() ) {
              jobMeta.setVariable( key, variableMap.get( key ) );
            }

          } catch ( UnknownParamException e ) {
            error( e.getMessage() );
          }
          jobMeta.setArguments( arguments );
          jobMeta.setFilename( solutionPath + fileAddress );
        }

      }

      // OK, we have the information, let's load and execute the
      // transformation or job

      if ( transMeta != null ) {
        result = executeTransformation( transMeta );
      }
      if ( jobMeta != null ) {
        result = executeJob( jobMeta, repository );
      }

    } finally {

      if ( repository != null ) {
        if ( ComponentBase.debug ) {
          debug( Messages.getInstance().getString( "Kettle.DEBUG_DISCONNECTING" ) ); //$NON-NLS-1$
        }
        try {
          repository.disconnect();
        } catch ( Exception ignored ) {
          //ignore
        }
      }

      if ( transMeta != null ) {
        try {
          cleanLogChannel( transMeta );
          transMeta.clear();
        } catch ( Exception ignored ) {
          //ignore
        }
        transMeta = null;
      }
      if ( jobMeta != null ) {
        try {
          cleanLogChannel( jobMeta );
          jobMeta.clear();
        } catch ( Exception ignored ) {
          //ignored
        }
        // Can't do anything about an exception here.
        jobMeta = null;
      }
    }

    if ( isDefinedOutput( EXECUTION_LOG_OUTPUT ) ) {
      setOutputValue( EXECUTION_LOG_OUTPUT, executionLog );
    }

    if ( isDefinedOutput( EXECUTION_STATUS_OUTPUT ) ) {
      setOutputValue( EXECUTION_STATUS_OUTPUT, executionStatus );
    }

    XMLHandlerCache.getInstance().clear();
    return result;

  }

  private void cleanLogChannel( LoggingObjectInterface loi ) {
    try {
      cleanLogChannelFromMap( loi );
      KettleLogStore.getAppender().removeChannelFromBuffer( loi.getLogChannelId() );
    } catch ( Exception ignored ) {
      //ignored
    }
    // Nothing I can do here...
  }

  private void cleanLogChannelFromMap( LoggingObjectInterface loi ) {
    String logChannelId = loi.getLogChannelId();
    Map<String, LoggingObjectInterface> logChannelMap = LoggingRegistry.getInstance().getMap();
    if ( ( logChannelMap != null ) ) {
      List<String> logKids = LoggingRegistry.getInstance().getLogChannelChildren( logChannelId );
      if ( logKids != null ) {
        for ( int i = 0; i < logKids.size(); i++ ) {
          logChannelMap.remove( logKids.get( i ) );
        }
      }
      logChannelMap.remove( logChannelId );
    }
  }

  private String getActualFileName( final IActionSequenceResource resource ) {
    String fileAddress = null;

    // Is it a hardcoded path?
    if ( ( resource.getSourceType() == IActionSequenceResource.FILE_RESOURCE ) ) {
      fileAddress = resource.getAddress();
    } else if ( resource.getSourceType() == IActionSequenceResource.SOLUTION_FILE_RESOURCE ) {
      fileAddress = resource.getAddress();
    }

    // Can it be loaded? this may not be true if using the DB Based repos
    if ( fileAddress != null ) {
      File file = new File( fileAddress );
      if ( !file.exists() || !file.isFile() ) {
        fileAddress = null;
      }
    }
    return ( fileAddress );
  }

  protected boolean customizeTrans( Trans trans ) {
    // override this to customize the transformation before it runs
    // by default there is no transformation
    return true;
  }

  private boolean executeTransformation( final TransMeta transMeta ) {
    boolean success = true;
    Trans trans = null;

    try {
      if ( transMeta != null ) {
        try {
          trans = new Trans( transMeta );
        } catch ( Exception e ) {
          throw new KettleComponentException( Messages.getInstance().getErrorString(
              "Kettle.ERROR_0010_BAD_TRANSFORMATION_METADATA" ), e ); //$NON-NLS-1$
        }
      }

      if ( trans == null ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0010_BAD_TRANSFORMATION_METADATA" ) ); //$NON-NLS-1$
      }

      // Remember where to get our execution logging from
      //
      logChannelId = trans.getLogChannelId();

      // OK, we have the transformation, now run it!
      if ( !customizeTrans( trans ) ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0028_CUSTOMIZATION_FUNCITON_FAILED" ) ); //$NON-NLS-1$
      }

      debug( Messages.getInstance().getString( "Kettle.DEBUG_PREPARING_TRANSFORMATION" ) ); //$NON-NLS-1$

      try {
        LogLevel lvl = getLogLevel();
        trans.setLogLevel( lvl );
        trans.prepareExecution( transMeta.getArguments() );
      } catch ( Exception e ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0011_TRANSFORMATION_PREPARATION_FAILED" ), e ); //$NON-NLS-1$
      }

      String stepName = null;
      String outputName = null;

      try {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_FINDING_STEP_IMPORTER" ) ); //$NON-NLS-1$

        stepName = getMonitorStepName();
        outputName = getTransformSuccessOutputName();

        if ( outputName != null ) {
          registerAsStepListener( stepName, trans );
        }
      } catch ( Exception e ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0012_ROW_LISTENER_CREATE_FAILED" ), e ); //$NON-NLS-1$
      }

      try {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_STARTING_TRANSFORMATION" ) ); //$NON-NLS-1$
        trans.startThreads();
      } catch ( Exception e ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0013_TRANSFORMATION_START_FAILED" ), e ); //$NON-NLS-1$
      }

      try {
        // It's running in a separate thread to allow monitoring,
        // etc.
        debug( Messages.getInstance().getString( "Kettle.DEBUG_TRANSFORMATION_RUNNING" ) ); //$NON-NLS-1$

        trans.waitUntilFinished();
        cleanLogChannel( trans );
        trans.cleanup();
      } catch ( Exception e ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0014_ERROR_DURING_EXECUTE" ), e ); //$NON-NLS-1$
      }

      // Dump the Kettle log...
      debug( getKettleLog( false ) );

      // Build written row output
      if ( results != null ) {
        if ( outputName != null ) {
          setOutputValue( outputName, results );
        }
        if ( isDefinedOutput( TRANSFORM_SUCCESS_COUNT_OUTPUT ) ) {
          setOutputValue( TRANSFORM_SUCCESS_COUNT_OUTPUT, results.getRowCount() );
        }
      }

      // Build error row output
      if ( errorResults != null ) {
        if ( isDefinedOutput( TRANSFORM_ERROR_OUTPUT ) ) {
          setOutputValue( TRANSFORM_ERROR_OUTPUT, errorResults );
        }
        if ( isDefinedOutput( TRANSFORM_ERROR_COUNT_OUTPUT ) ) {
          setOutputValue( TRANSFORM_ERROR_COUNT_OUTPUT, errorResults.getRowCount() );
        }
      }
    } catch ( KettleComponentException e ) {
      success = false;
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0008_ERROR_RUNNING", e.toString() ), e ); //$NON-NLS-1$
    }

    prepareKettleOutput( trans );

    return success;
  }

  private boolean registerAsStepListener( String stepName, Trans trans ) throws KettleComponentException {
    boolean success = false;

    try {
      if ( trans != null ) {
        List<StepMetaDataCombi> stepList = trans.getSteps();
        // find the specified step
        for ( StepMetaDataCombi step : stepList ) {
          if ( step.stepname.equals( stepName ) ) {
            if ( ComponentBase.debug ) {
              debug( Messages.getInstance().getString( "Kettle.DEBUG_FOUND_STEP_IMPORTER" ) ); //$NON-NLS-1$
            }
            // this is the step we are looking for
            if ( ComponentBase.debug ) {
              debug( Messages.getInstance().getString( "Kettle.DEBUG_GETTING_STEP_METADATA" ) ); //$NON-NLS-1$
            }
            RowMetaInterface row = trans.getTransMeta().getStepFields( stepName );

            // create the metadata that the Pentaho result sets need
            String[] fieldNames = row.getFieldNames();
            String[][] columns = new String[1][fieldNames.length];
            for ( int column = 0; column < fieldNames.length; column++ ) {
              columns[0][column] = fieldNames[column];
            }
            if ( ComponentBase.debug ) {
              debug( Messages.getInstance().getString( "Kettle.DEBUG_CREATING_RESULTSET_METADATA" ) ); //$NON-NLS-1$
            }

            MemoryMetaData metaData = new MemoryMetaData( columns, null );
            results = new MemoryResultSet( metaData );
            errorResults = new MemoryResultSet( metaData );

            // add ourself as a row listener
            step.step.addRowListener( this );
            success = true;
            break;
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleComponentException( Messages.getInstance().getString(
          "Kettle.ERROR_0027_ERROR_INIT_STEP", stepName ), e ); //$NON-NLS-1$
    }

    return success;
  }

  private String getMonitorStepName() {
    String result = null;

    // Supporting "importstep" for backwards compatibility
    if ( isDefinedInput( KettleComponent.IMPORTSTEP ) ) {
      result = getInputStringValue( KettleComponent.IMPORTSTEP );
    } else if ( isDefinedInput( KettleComponent.MONITORSTEP ) ) {
      result = getInputStringValue( KettleComponent.MONITORSTEP );
    }
    return result;
  }

  private LogLevel getLogLevel() {
    if ( isDefinedInput( KettleComponent.KETTLELOGLEVEL ) ) {
      String logLevelStr = getInputStringValue( KettleComponent.KETTLELOGLEVEL );
      try {
        return LogLevel.valueOf( logLevelStr );
      } catch ( Exception ex ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0024_BAD_LOGGING_LEVEL", logLevelStr ) ); //$NON-NLS-1$
        return LogLevel.BASIC;
      }
    } else {
      // If not defined in the component, translate
      // xaction logging level to PDI Logging Level
      switch ( loggingLevel ) {
        case ILogger.DEBUG:
          return LogLevel.DEBUG;
        case ILogger.ERROR:
          return LogLevel.ERROR;
        case ILogger.FATAL:
          return LogLevel.ERROR;
        case ILogger.INFO:
          return LogLevel.MINIMAL;
        case ILogger.WARN:
          return LogLevel.BASIC;
        case ILogger.TRACE:
          return LogLevel.ROWLEVEL;
        default:
          return LogLevel.BASIC;
      }

    }
  }

  @SuppressWarnings ( "unchecked" )
  private String getTransformSuccessOutputName() {
    String result = null;

    // Supporting "importstep" for backwards compatibility
    if ( isDefinedInput( KettleComponent.IMPORTSTEP ) ) {
      if ( getOutputNames().size() == 1 ) {
        result = (String) getOutputNames().iterator().next();
      } else {
        // Need to find the name that does not match one of the
        // predefined output parameters
        result = getUndefinedOutputParameter( getOutputNames().iterator() );
        if ( result == null ) {
          // Use the new TRANSFORM_SUCCESS_OUTPUT to send the output
          // to, if it is present
          if ( isDefinedOutput( TRANSFORM_SUCCESS_OUTPUT ) ) {
            result = TRANSFORM_SUCCESS_OUTPUT;
          }
        }
      }
    } else if ( isDefinedOutput( TRANSFORM_SUCCESS_OUTPUT ) ) {
      result = TRANSFORM_SUCCESS_OUTPUT;
    }

    return result;
  }

  private void prepareKettleOutput( Trans trans ) {
    extractKettleStatus( trans );
    extractKettleLog();
  }

  private void prepareKettleOutput( Job job ) {
    extractKettleStatus( job );
    extractKettleLog();
  }

  private void extractKettleStatus( Trans trans ) {
    if ( trans != null ) {
      executionStatus = trans.getStatus();
    } else {
      executionStatus = Messages.getInstance().getErrorString( "Kettle.ERROR_0025_TRANSFORMATION_NOT_LOADED" ); //$NON-NLS-1$
    }
  }

  private void extractKettleStatus( Job job ) {
    if ( job != null ) {
      executionStatus = job.getStatus();
    } else {
      executionStatus = Messages.getInstance().getErrorString( "Kettle.ERROR_0026_JOB_NOT_LOADED" ); //$NON-NLS-1$
    }
  }

  private String getKettleLog( boolean includeGeneral ) {
    StringBuffer logText = KettleLogStore.getAppender().getBuffer( logChannelId, includeGeneral );
    return logText.toString();
  }

  private void extractKettleLog() {
    executionLog = getKettleLog( false );
  }

  private String getUndefinedOutputParameter( Iterator<String> outputNames ) {
    String tempName = null;
    while ( outputNames.hasNext() ) {
      tempName = (String) outputNames.next();
      if ( !outputParams.contains( tempName ) ) {
        // Found user defined named
        return ( tempName );
      }
    }
    return null;
  }

  private boolean executeJob( final JobMeta jobMeta, final Repository repository ) {
    boolean success = true;
    Job job = null;

    try {
      if ( jobMeta != null ) {
        try {
          job = new Job( repository, jobMeta );
        } catch ( Exception e ) {
          throw new KettleComponentException( Messages.getInstance().getErrorString(
              "Kettle.ERROR_0021_BAD_JOB_METADATA" ), e ); //$NON-NLS-1$
        }

      }
      if ( job == null ) {
        debug( getKettleLog( true ) );
        throw new KettleComponentException( Messages.getInstance()
            .getErrorString( "Kettle.ERROR_0021_BAD_JOB_METADATA" ) ); //$NON-NLS-1$
      }

      // Remember where to get our execution logging from
      //
      logChannelId = job.getLogChannelId();

      try {
        if ( ComponentBase.debug ) {
          debug( Messages.getInstance().getString( "Kettle.DEBUG_STARTING_JOB" ) ); //$NON-NLS-1$
        }
        LogLevel lvl = getLogLevel();
        job.setLogLevel( lvl );
        job.start();
      } catch ( Exception e ) {
        throw new KettleComponentException( Messages.getInstance()
            .getErrorString( "Kettle.ERROR_0022_JOB_START_FAILED" ), e ); //$NON-NLS-1$
      }

      try {
        // It's running in a separate tread to allow monitoring,
        // etc.
        if ( ComponentBase.debug ) {
          debug( Messages.getInstance().getString( "Kettle.DEBUG_JOB_RUNNING" ) ); //$NON-NLS-1$
        }
        job.waitUntilFinished();
        if ( job.getResult().getNrErrors() > 0 ) {
          debug( getKettleLog( true ) );
          throw new KettleComponentException( Messages.getInstance().getErrorString(
              "Kettle.ERROR_0014_ERROR_DURING_EXECUTE" ) ); //$NON-NLS-1$
        }
      } catch ( Exception e ) {
        throw new KettleComponentException( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0014_ERROR_DURING_EXECUTE" ), e ); //$NON-NLS-1$
      } finally {
        if ( job != null ) {
          cleanLogChannel( job );
        }
      }

      // Dump the Kettle log...
      debug( getKettleLog( false ) );
    } catch ( KettleComponentException e ) {
      success = false;
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0008_ERROR_RUNNING", e.toString() ), e ); //$NON-NLS-1$
    }

    prepareKettleOutput( job );

    return success;

  }

  private TransMeta loadTransformFromRepository( final String directoryName, final String transformationName,
                                                 final Repository repository ) {
    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "Kettle.DEBUG_DIRECTORY", directoryName ) ); //$NON-NLS-1$
    }
    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "Kettle.DEBUG_TRANSFORMATION", transformationName ) ); //$NON-NLS-1$
    }
    TransMeta transMeta = null;
    try {

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_FINDING_DIRECTORY" ) ); //$NON-NLS-1$
      }

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_GETTING_TRANSFORMATION_METADATA" ) ); //$NON-NLS-1$
      }

      try {
        // Load the transformation from the repository
        RepositoryDirectoryInterface repositoryDirectory =
            repository.loadRepositoryDirectoryTree().findDirectory( directoryName );
        transMeta = repository.loadTransformation( transformationName, repositoryDirectory, null, true, null );
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0009_TRANSFROMATION_METADATA_NOT_FOUND", directoryName + "/" + transformationName ), e ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }
      if ( transMeta == null ) {
        error( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0009_TRANSFROMATION_METADATA_NOT_FOUND", directoryName + "/" + transformationName ) ); //$NON-NLS-1$ //$NON-NLS-2$
        debug( getKettleLog( true ) );
        return null;
      } else {
        return transMeta;
      }

    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0008_ERROR_RUNNING", e.toString() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  private JobMeta loadJobFromRepository( final String directoryName, final String jobName,
                                         final Repository repository ) {
    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "Kettle.DEBUG_DIRECTORY", directoryName ) ); //$NON-NLS-1$
    }
    if ( ComponentBase.debug ) {
      debug( Messages.getInstance().getString( "Kettle.DEBUG_JOB", jobName ) ); //$NON-NLS-1$
    }
    JobMeta jobMeta = null;
    try {

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_FINDING_DIRECTORY" ) ); //$NON-NLS-1$
      }

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_GETTING_JOB_METADATA" ) ); //$NON-NLS-1$
      }

      try {
        // Load the job from the repository
        RepositoryDirectoryInterface repositoryDirectory =
            repository.loadRepositoryDirectoryTree().findDirectory( directoryName );
        jobMeta = repository.loadJob( jobName, repositoryDirectory, null, null );
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0020_JOB_METADATA_NOT_FOUND", directoryName + "/" + jobName ), e ); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
      }
      if ( jobMeta == null ) {
        error( Messages.getInstance().getErrorString(
            "Kettle.ERROR_0020_JOB_METADATA_NOT_FOUND", directoryName + "/" + jobName ) ); //$NON-NLS-1$ //$NON-NLS-2$
        debug( getKettleLog( true ) );
        return null;
      } else {
        return jobMeta;
      }

    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0008_ERROR_RUNNING", e.toString() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  private Repository connectToRepository() {
    boolean useRepository =
        PentahoSystem.getSystemSetting( "kettle/settings.xml", "repository.type", "files" ).equals( "rdbms" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    if ( !useRepository ) {
      return null;
    }

    try {
      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_META_REPOSITORY" ) ); //$NON-NLS-1$
      }

      RepositoriesMeta repositoriesMeta = null;
      try {
        repositoriesMeta = new RepositoriesMeta();
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0007_BAD_META_REPOSITORY" ), e ); //$NON-NLS-1$
        return null;
      }
      /*
       * Unreachable code below if (repositoriesMeta == null) {
       * error(Messages.getInstance().getErrorString("Kettle.ERROR_0007_BAD_META_REPOSITORY")); //$NON-NLS-1$
       * debug(getKettleLog(true)); return null; }
       */

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_POPULATING_META" ) ); //$NON-NLS-1$
      }
      try {
        // TODO: add support for specified repositories.xml files...
        repositoriesMeta.readData(); // Read from the default
        // $HOME/.kettle/repositories.xml
        // file.
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0018_META_REPOSITORY_NOT_POPULATED" ), e ); //$NON-NLS-1$
        return null;
      }
      if ( ( repositoriesXMLFile != null ) && !"".equals( repositoriesXMLFile ) ) //$NON-NLS-1$
      {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0017_XML_REPOSITORY_NOT_SUPPORTED" ) ); //$NON-NLS-1$
        debug( getKettleLog( true ) );
        return null;
      }

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_FINDING_REPOSITORY" ) ); //$NON-NLS-1$
      }
      // Find the specified repository.
      RepositoryMeta repositoryMeta = null;
      try {
        repositoryMeta = repositoriesMeta.findRepository( repositoryName );
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName ), e ); //$NON-NLS-1$
        return null;
      }

      if ( repositoryMeta == null ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0004_REPOSITORY_NOT_FOUND", repositoryName ) ); //$NON-NLS-1$
        debug( getKettleLog( true ) );
        return null;
      }

      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_GETTING_REPOSITORY" ) ); //$NON-NLS-1$
      }
      Repository repository = null;
      try {

        repository =
            PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta.getId(),
                Repository.class );
        repository.init( repositoryMeta );

      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "Kettle.ERROR_0016_COULD_NOT_GET_REPOSITORY_INSTANCE" ), e ); //$NON-NLS-1$
        return null;
      }

      // OK, now try the username and password
      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_CONNECTING" ) ); //$NON-NLS-1$
      }
      repository.connect( username, password );

      // OK, the repository is open and ready to use.
      if ( ComponentBase.debug ) {
        debug( Messages.getInstance().getString( "Kettle.DEBUG_FINDING_DIRECTORY" ) ); //$NON-NLS-1$
      }

      return repository;

    } catch ( Exception e ) {
      error( Messages.getInstance().getErrorString( "Kettle.ERROR_0008_ERROR_RUNNING", e.toString() ), e ); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  public void done() {

  }

  public void rowReadEvent( final RowMetaInterface row, final Object[] values ) {
  }

  public void rowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) throws KettleStepException {
    processRow( results, rowMeta, row );
  }

  public void errorRowWrittenEvent( final RowMetaInterface rowMeta, final Object[] row ) throws KettleStepException {
    processRow( errorResults, rowMeta, row );
  }

  public void processRow( MemoryResultSet memResults, final RowMetaInterface rowMeta, final Object[] row )
    throws KettleStepException {
    if ( memResults == null ) {
      return;
    }
    try {
      Object[] pentahoRow = new Object[memResults.getColumnCount()];
      for ( int columnNo = 0; columnNo < memResults.getColumnCount(); columnNo++ ) {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( columnNo );

        switch ( valueMeta.getType() ) {
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoRow[columnNo] = rowMeta.getBigNumber( row, columnNo );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoRow[columnNo] = rowMeta.getBoolean( row, columnNo );
            break;
          case ValueMetaInterface.TYPE_DATE:
            pentahoRow[columnNo] = rowMeta.getDate( row, columnNo );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoRow[columnNo] = rowMeta.getInteger( row, columnNo );
            break;
          case ValueMetaInterface.TYPE_NONE:
            pentahoRow[columnNo] = rowMeta.getString( row, columnNo );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoRow[columnNo] = rowMeta.getNumber( row, columnNo );
            break;
          case ValueMetaInterface.TYPE_STRING:
            pentahoRow[columnNo] = rowMeta.getString( row, columnNo );
            break;
          default:
            pentahoRow[columnNo] = rowMeta.getString( row, columnNo );
        }
      }
      memResults.addRow( pentahoRow );
    } catch ( KettleValueException e ) {
      throw new KettleStepException( e );
    }
  }

}
