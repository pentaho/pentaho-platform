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

package org.pentaho.platform.plugin.action.pentahometadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.metadata.model.IMetadataQueryExec;
import org.pentaho.metadata.query.model.Parameter;
import org.pentaho.metadata.query.model.Query;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.util.Map;
import java.util.Properties;

/**
 * This is the BI Platform Pojo Component for Pentaho Metadata Queries. It currently supports executing the inline etl
 * and sql physical models.
 * 
 * 
 * 
 * TODO: We should eventually move the copy and pasted code that executes the SQL into a pojo SQL Component.
 * 
 * @author Will Gorman
 * 
 */
public class MetadataQueryComponent {

  static final Log logger = LogFactory.getLog( MetadataQueryComponent.class );
  Query queryObject; // An optional query model to execute instead of the string query
  String query;
  Integer maxRows; // -1;
  Integer timeout; // -1;
  boolean readOnly; // false;

  boolean live = false;
  boolean useForwardOnlyResultSet = false;
  boolean logSql = false;
  boolean forceDbDialect = false;
  IPentahoSession session = null;
  IPentahoResultSet resultSet = null;

  String xmlHelperClass = "org.pentaho.metadata.query.model.util.QueryXmlHelper"; //$NON-NLS-1$
  String sqlGeneratorClass = null;

  Map<String, Object> inputs = null;

  /*
   * The list of inputs to this component, used when resolving parameter values.
   * 
   * @param inputs map of inputs
   */
  public void setInputs( Map<String, Object> inputs ) {
    this.inputs = inputs;
  }

  public void setLogSql( boolean logSql ) {
    this.logSql = logSql;
  }

  public void setQuery( String query ) {
    this.query = query;
  }

  /**
   * Sets the query to be executed. This is a query model that will be executed in preference to a string-based query.
   * 
   * @param queryObject
   */
  public void setQueryObject( Query queryObject ) {
    this.queryObject = queryObject;
  }

  public void setMaxRows( Integer maxRows ) {
    this.maxRows = maxRows;
  }

  public void setTimeout( Integer timeout ) {
    this.timeout = timeout;
  }

  public void setLive( boolean live ) {
    this.live = live;
  }

  /**
   * This sets the read only property in the Pentaho SQLConnection API
   * 
   * @param readOnly
   *          true if read only
   */
  public void setReadOnly( Boolean readOnly ) {
    this.readOnly = readOnly;
  }

  public void setUseForwardOnlyResultSet( boolean useForwardOnlyResultSet ) {
    this.useForwardOnlyResultSet = useForwardOnlyResultSet;
  }

  public void setQueryModelXmlHelper( String xmlHelperClass ) {
    this.xmlHelperClass = xmlHelperClass;
  }

  /*
   * TODO handle these generically public void setQueryModelSqlGenerator(String sqlGeneratorClass) {
   * this.sqlGeneratorClass = sqlGeneratorClass; }
   * 
   * public void setForceDbDialect(boolean forceDbDialect) { this.forceDbDialect = forceDbDialect; }
   */

  @SuppressWarnings( "unchecked" )
  private QueryXmlHelper createQueryXmlHelper() throws Exception {
    Class clazz = Class.forName( xmlHelperClass );
    return (QueryXmlHelper) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
  }

  public boolean execute() {

    // get the xml parser
    QueryXmlHelper helper = null;
    try {
      helper = createQueryXmlHelper();
    } catch ( Exception e ) {
      logger.error( "error", e ); //$NON-NLS-1$
      return false;
    }

    // parse the metadata query
    IMetadataDomainRepository repo = PentahoSystem.get( IMetadataDomainRepository.class, null );

    if ( queryObject == null ) {
      // there is no query model, so create one from the query string
      // apply templates to the query
      String templatedQuery = null;
      if ( inputs != null ) {
        Properties properties = new Properties();
        for ( String name : inputs.keySet() ) {
          if ( !( inputs.get( name ) == null ) ) {
            properties.put( name, inputs.get( name ).toString() );
          }
        }
        templatedQuery = TemplateUtil.applyTemplate( query, properties, null );
      } else {
        templatedQuery = query;
      }

      try {
        queryObject = helper.fromXML( repo, templatedQuery );
      } catch ( Exception e ) {
        logger.error( "error", e ); //$NON-NLS-1$
        return false;
      }
    }

    if ( queryObject == null ) {
      logger.error( "error query object null" ); //$NON-NLS-1$
      return false;
    }

    // Read metadata for new timeout/max_rows and set in superclass
    // Can still be overridden in the action sequence
    if ( timeout == null ) {
      Object timeoutProperty = queryObject.getLogicalModel().getProperty( "timeout" ); //$NON-NLS-1$
      if ( timeoutProperty != null && timeoutProperty instanceof Number ) {
        int timeoutVal = ( (Number) timeoutProperty ).intValue();
        this.setTimeout( timeoutVal );
      }
    }

    if ( maxRows == null ) {
      Object maxRowsProperty = queryObject.getLogicalModel().getProperty( "max_rows" ); //$NON-NLS-1$
      if ( maxRowsProperty != null && maxRowsProperty instanceof Number ) {
        int maxRowsVal = ( (Number) maxRowsProperty ).intValue();
        this.setMaxRows( maxRowsVal );
      }
    }

    String queryExecName = queryObject.getLogicalModel().getPhysicalModel().getQueryExecName();
    String queryExecDefault = queryObject.getLogicalModel().getPhysicalModel().getDefaultQueryClassname();
    // String modelType = (String) inputs.get("modeltype");
    IMetadataQueryExec executor = PentahoSystem.get( IMetadataQueryExec.class, queryExecName, session );

    if ( executor == null ) {
      // get the executor from a plugin possibly?
      Class clazz;
      try {
        clazz =
            Class.forName( queryExecDefault, true, queryObject.getLogicalModel().getPhysicalModel().getClass()
                .getClassLoader() );
        executor = (IMetadataQueryExec) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
      } catch ( Exception e ) {
        logger.warn( Messages.getInstance().getErrorString(
          "MetadataQueryComponent.ERROR_0002_NO_EXECUTOR", queryExecName ) ); //$NON-NLS-1$
      }
    }

    if ( executor == null ) {
      // the query exec class is not defined thru configuration, go with the default
      Class clazz;
      try {
        clazz = Class.forName( queryExecDefault );
        executor = (IMetadataQueryExec) clazz.getConstructor( new Class[] {} ).newInstance( new Object[] {} );
      } catch ( Exception e ) {
        logger.error( Messages.getInstance().getErrorString(
          "MetadataQueryComponent.ERROR_0002_NO_EXECUTOR", queryExecName ) ); //$NON-NLS-1$
        return false;
      }
    }
    // determine parameter values
    if ( queryObject.getParameters() != null ) {
      for ( Parameter param : queryObject.getParameters() ) {

        Object value = null;
        if ( inputs != null ) {
          value = inputs.get( param.getName() );
        }

        executor.setParameter( param, value );

      }
    }

    try {
      executor.setDoQueryLog( logSql );
      executor.setForwardOnly( this.useForwardOnlyResultSet );
      executor.setMaxRows( this.maxRows );
      executor.setMetadataDomainRepository( repo );
      executor.setReadOnly( this.readOnly );
      executor.setTimeout( this.timeout );
      if ( this.inputs != null ) {
        executor.setInputs( this.inputs );
      }

      resultSet = executor.executeQuery( queryObject );
      if ( resultSet != null && !live && executor.isLive() ) {
        // read the results and cache them
        MemoryResultSet cachedResultSet = new MemoryResultSet( resultSet.getMetaData() );
        Object[] rowObjects = resultSet.next();
        while ( rowObjects != null ) {
          cachedResultSet.addRow( rowObjects );
          rowObjects = resultSet.next();
        }
        resultSet.close();
        resultSet.closeConnection();
        resultSet = cachedResultSet;
      }

      return resultSet != null;
    } catch ( Exception e ) {
      logger.error( "error", e ); //$NON-NLS-1$
      throw new RuntimeException( e.getLocalizedMessage(), e );
    }

  }

  public boolean validate() {
    if ( query == null ) {
      logger.error( "no query specified" ); //$NON-NLS-1$
      return false;
    }

    return true;
  }

  public IPentahoResultSet getResultSet() {
    return resultSet;
  }

}
