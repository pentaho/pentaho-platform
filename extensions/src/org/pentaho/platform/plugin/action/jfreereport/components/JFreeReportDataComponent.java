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

package org.pentaho.platform.plugin.action.jfreereport.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDataComponent;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoDataFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableDataFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableModel;
import org.pentaho.platform.plugin.action.jfreereport.helper.ReportUtils;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.util.messages.LocaleHelper;

import javax.swing.table.TableModel;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * This is step 2 out of 3. This class is a wrapper around an other component, for instance the SQL- or MDX query
 * component.
 * 
 * @deprecated
 * @author Thomas Morgner
 */
@Deprecated
public class JFreeReportDataComponent extends AbstractJFreeReportComponent {

  private static final long serialVersionUID = -1708477862117476001L;

  private IDataComponent dataComponent;

  public JFreeReportDataComponent() {
  }

  /**
   * Validates the parameters of this action.
   * 
   * @return
   */
  @Override
  protected boolean validateAction() {
    return true;
  }

  public IDataComponent getDataComponent() {
    return dataComponent;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
    // help the garbage collector ...
    if ( dataComponent != null ) {
      dataComponent.dispose();
      dataComponent.done();
    }
    dataComponent = null;
  }

  @SuppressWarnings( "unused" )
  private PentahoTableDataFactory getQueryComponentDataFactory() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, Exception {
    PentahoTableDataFactory factory = null;
    dataComponent = null;
    final Node sourceNode =
        getComponentDefinition().selectSingleNode( AbstractJFreeReportComponent.DATACOMPONENT_SOURCE );
    if ( sourceNode != null ) {
      String dataComponentClass = sourceNode.getText();
      if ( AbstractJFreeReportComponent.DATACOMPONENT_SQLSOURCE.equalsIgnoreCase( dataComponentClass ) ) {
        dataComponentClass = AbstractJFreeReportComponent.DATACOMPONENT_SQLCLASS;
      } else if ( AbstractJFreeReportComponent.DATACOMPONENT_MDXSOURCE.equalsIgnoreCase( dataComponentClass ) ) {
        dataComponentClass = AbstractJFreeReportComponent.DATACOMPONENT_MDXCLASS;
      }
      if ( dataComponentClass != null ) {
        try {
          final Class componentClass = Class.forName( dataComponentClass );
          dataComponent = (IDataComponent) componentClass.newInstance();
          dataComponent.setInstanceId( getInstanceId() );
          dataComponent.setActionName( getActionName() );
          dataComponent.setProcessId( getProcessId() );
          dataComponent.setComponentDefinition( getComponentDefinition() );
          dataComponent.setRuntimeContext( getRuntimeContext() );
          dataComponent.setSession( getSession() );
          dataComponent.setLoggingLevel( getLoggingLevel() );
          dataComponent.setMessages( getMessages() );
          // if that fails, then we know we messed up again.
          // Abort, we cant continue anyway.
          if ( ( dataComponent.validate() == IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_OK ) && dataComponent.init()
              && ( dataComponent.execute() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) ) {
            final IPentahoResultSet resultset = dataComponent.getResultSet();
            factory =
                new PentahoTableDataFactory( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT,
                    new PentahoTableModel( resultset ) );
          } else {
            throw new IllegalArgumentException( Messages.getInstance().getErrorString(
                "JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED" ) ); //$NON-NLS-1$
          }
        } catch ( ClassNotFoundException e ) {
          //ignore
        } catch ( InstantiationException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch ( IllegalAccessException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return factory;
  }

  @SuppressWarnings( "unused" )
  private PentahoTableDataFactory getJarDataFactory() throws Exception {
    PentahoTableDataFactory factory = null;
    if ( isDefinedResource( AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT ) ) {
      final IActionSequenceResource resource = getResource( AbstractJFreeReportComponent.DATACOMPONENT_JARINPUT );
      final InputStream in;
      try {
        in = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
        try {
          // not being able to read a single char is definitly a big boo ..
          if ( in.read() == -1 ) {
            throw new Exception( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE" ) ); //$NON-NLS-1$
          } else {
            final ClassLoader loader = ReportUtils.createJarLoader( getSession(), resource );
            if ( loader == null ) {
              throw new Exception( Messages.getInstance().getString(
                "JFreeReportDataComponent.ERROR_0035_COULD_NOT_CREATE_CLASSLOADER" ) ); //$NON-NLS-1$
            } else if ( !isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_CLASSLOCINPUT ) ) {
              throw new Exception( Messages.getInstance().getErrorString(
                "JFreeReport.ERROR_0012_CLASS_LOCATION_MISSING" ) ); //$NON-NLS-1$
            } else {
              final String classLocation =
                  getInputStringValue( AbstractJFreeReportComponent.DATACOMPONENT_CLASSLOCINPUT );
              // Get input parameters, and set them as properties in the report
              // object.
              final ReportParameterValues reportProperties = new ReportParameterValues();
              final Set paramNames = getInputNames();
              final Iterator it = paramNames.iterator();
              while ( it.hasNext() ) {
                final String paramName = (String) it.next();
                final Object paramValue = getInputValue( paramName );
                if ( paramValue instanceof Object[] ) {
                  final Object[] values = (Object[]) paramValue;
                  final StringBuffer valuesBuffer = new StringBuffer();
                  // TODO support non-string items
                  for ( int i = 0; i < values.length; i++ ) {
                    if ( i == 0 ) {
                      valuesBuffer.append( values[i].toString() );
                    } else {
                      valuesBuffer.append( ',' ).append( values[i].toString() );
                    }
                  }
                  reportProperties.put( paramName, valuesBuffer.toString() );
                } else {
                  reportProperties.put( paramName, paramValue );
                }
              }

              final DataFactory dataFactory = new PentahoDataFactory( loader );
              final TableModel model = dataFactory.queryData( classLocation, new ParameterDataRow( reportProperties ) );

              factory = new PentahoTableDataFactory( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT, model );
            }
          }
        } catch ( Exception e ) {
          throw new Exception( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0009_REPORT_JAR_UNREADABLE" ) ); //$NON-NLS-1$
        }
      } catch ( FileNotFoundException e1 ) {
        throw new Exception( Messages.getInstance().getErrorString(
          "JFreeReport.ERROR_0010_REPORT_JAR_MISSING", resource.getAddress() ) ); //$NON-NLS-1$
      }
    }
    return factory;
  }

  @SuppressWarnings( "unused" )
  private PentahoTableDataFactory getInputParamDataFactory() {

    PentahoTableDataFactory factory = null;
    if ( isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_DATAINPUT )
        || isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT ) ) {

      factory = new PentahoTableDataFactory();
      final Iterator iter = getInputNames().iterator();
      while ( iter.hasNext() ) {
        String name = (String) iter.next();
        final Object dataObject = getInputValue( name );
        // if input name is "data", rename to "default" which is the name that jfreereport is expecting.
        if ( name.equals( AbstractJFreeReportComponent.DATACOMPONENT_DATAINPUT ) ) {
          name = AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT;
        }
        if ( dataObject instanceof IPreparedComponent ) {
          final IPreparedComponent comp = (IPreparedComponent) dataObject;
          factory.addPreparedComponent( name, comp );
        } else if ( dataObject instanceof IPentahoResultSet ) {
          final IPentahoResultSet resultset = (IPentahoResultSet) dataObject;
          resultset.beforeFirst();
          factory.addTable( name, new PentahoTableModel( resultset ) );
        } else if ( dataObject instanceof TableModel ) {
          final TableModel model = (TableModel) dataObject;
          factory.addTable( name, model );
        }
      }
    }
    return factory;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    boolean result = false;
    // try {
    // PentahoTableDataFactory factory = getDataFactory();
    // if (factory != null) {
    // addTempParameterObject(DATACOMPONENT_REPORTTEMP_DATAFACTORY, factory);
    // }
    result = true;
    // } catch (ClassNotFoundException ex) {
    //      error(Messages.getInstance().getErrorString("JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED"), ex); //$NON-NLS-1$
    // } catch (InstantiationException ex) {
    //      error(Messages.getInstance().getErrorString("JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED"), ex); //$NON-NLS-1$
    // } catch (IllegalAccessException ex) {
    //      error(Messages.getInstance().getErrorString("JFreeReport.ERROR_0021_DATA_COMPONENT_FAILED"), ex); //$NON-NLS-1$
    // } catch (Exception ex) {
    //      error(ex.getMessage()); //$NON-NLS-1$
    // }
    return result;
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportDataComponent.class );
  }
}
