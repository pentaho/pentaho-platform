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

package org.pentaho.platform.plugin.action.jfreereport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.JFreeReportAction;
import org.pentaho.commons.connection.ActivationHelper;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.jfreereport.castormodel.reportspec.ReportSpec;
import org.pentaho.jfreereport.wizard.utility.CastorUtility;
import org.pentaho.jfreereport.wizard.utility.report.ReportGenerationUtility;
import org.pentaho.jfreereport.wizard.utility.report.ReportParameterUtility;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableDataFactory;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoTableModel;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;

import javax.activation.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The report-wizard component generates a report definition from a report-spec file. Use this component, if want to use
 * the standard-report process only or if you have no need to tweak the processing.
 * <p/>
 * 
 * @created May 15, 2006
 * @author Michael D'Amour
 */
public class ReportWizardSpecComponent extends JFreeReportComponent {
  private static final long serialVersionUID = 3435921119638344882L;

  private ReportSpec reportSpec;

  public ReportWizardSpecComponent() {
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ReportWizardSpecComponent.class );
  }

  @Override
  public boolean validateAction() {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    return ( jFreeReportAction.getReportDefinition() != null ) && super.validateAction();
  }

  @Override
  protected boolean executeReportAction() {
    boolean result = true;
    try {
      reportSpec = getReportSpec();
      result = super.executeReportAction();
    } catch ( IOException ex ) {
      error( ex.getLocalizedMessage() );
      result = false;
    }

    return result;
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public MasterReport getReport() throws Exception {
    MasterReport report = null;
    if ( reportSpec != null ) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ReportGenerationUtility.createJFreeReportXML( reportSpec, outputStream, 0, 0, false, "", 0, 0 ); //$NON-NLS-1$
      String reportDefinition = new String( outputStream.toByteArray() );
      report = createReport( reportDefinition );
    } else {
      report = super.getReport();
    }
    return report;
  }

  @Override
  protected PentahoTableDataFactory getDataFactory() throws ClassNotFoundException, InstantiationException,
    IllegalAccessException, Exception {
    PentahoTableDataFactory factory = null;
    if ( reportSpec != null ) {
      if ( !isDefinedInput( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTTEMP_PERFQRY )
          || "true".equals( getInputParameter( AbstractJFreeReportComponent.REPORTGENERATEDEFN_REPORTTEMP_PERFQRY ) ) ) { //$NON-NLS-1$
        IPentahoResultSet pentahoResultSet = getResultSet( getReportSpec() );
        factory = new PentahoTableDataFactory();
        pentahoResultSet.beforeFirst();
        factory.addTable( AbstractJFreeReportComponent.DATACOMPONENT_DEFAULTINPUT, new PentahoTableModel(
              pentahoResultSet ) );

      } else {
        factory = super.getDataFactory();
      }
    } else {
      factory = super.getDataFactory();
    }
    return factory;
  }

  @SuppressWarnings( "deprecation" )
  public ReportSpec getReportSpec() throws IOException {
    JFreeReportAction jFreeReportAction = (JFreeReportAction) getActionDefinition();
    DataSource dataSource =
        new ActivationHelper.PentahoStreamSourceWrapper( jFreeReportAction.getReportDefinitionDataSource() );
    ReportSpec reportSpec = null;
    reportSpec = loadFromZip( dataSource.getInputStream() );
    if ( reportSpec == null ) {
      dataSource = new ActivationHelper.PentahoStreamSourceWrapper( jFreeReportAction.getReportDefinitionDataSource() );
      reportSpec =
          (ReportSpec) CastorUtility.getInstance().readCastorObject( dataSource.getInputStream(), ReportSpec.class );
    }
    return reportSpec;
  }

  @SuppressWarnings( "deprecation" )
  private ReportSpec loadFromZip( final InputStream reportSpecInputStream ) {
    try {
      ZipInputStream zis = new ZipInputStream( reportSpecInputStream );

      ZipEntry reportSpecEntry = findReportSpec( zis );

      if ( reportSpecEntry == null ) {
        return null;
      }

      // is this really sane? Blindly using the first zip entry is ... argh!
      // maybe you should use GZipped streams instead...
      return (ReportSpec) CastorUtility.getInstance().readCastorObject( zis, ReportSpec.class );
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Look through the Zip stream and find an entry whose name is .xreportspec. If the entry is found, return it,
   * otherwise return null.
   * 
   * @param zStrm
   * @return If the entry is found, return it, otherwise return null.
   * @throws IOException
   */
  private ZipEntry findReportSpec( final ZipInputStream zStrm ) throws IOException {
    ZipEntry reportSpecEntry = null;
    // for loop has no body
    for ( reportSpecEntry = zStrm.getNextEntry(); ( null != reportSpecEntry )
        && !reportSpecEntry.getName().endsWith( ".xreportspec" ); reportSpecEntry = zStrm //$NON-NLS-1$
        .getNextEntry() ) {
      boolean ignored = true;
    }

    return reportSpecEntry;
  }

  public IPentahoResultSet getResultSet( final ReportSpec reportSpec ) throws Exception {
    String jndiName = reportSpec.getReportSpecChoice().getJndiSource();
    IPentahoConnection connection = null;
    if ( reportSpec.getIsMDX() ) {
      // did this ever work??
      String connectStr = ""; //$NON-NLS-1$
      IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
      String dsName = datasourceService.getDSBoundName( jndiName );
      if ( dsName != null ) {
        connectStr = "dataSource=" + dsName + "; Catalog=mondrian"; //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        error( Messages.getInstance().getErrorString( "MDXBaseComponent.ERROR_0005_INVALID_CONNECTION" ) ); //$NON-NLS-1$
        return null;
      }
      Properties props = new Properties();
      props.setProperty( IPentahoConnection.CONNECTION, connectStr );
      props.setProperty( IPentahoConnection.PROVIDER, reportSpec.getMondrianCubeDefinitionPath() );

      connection =
          PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, props, getSession(), this );
    } else {
      connection =
          PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, jndiName, getSession(), this );
    }
    String query = ReportParameterUtility.setupParametersForActionSequence( reportSpec.getQuery() );
    query = setupQueryParameters( query );
    IPentahoResultSet res = connection.executeQuery( query );
    return res;
  }

  public String setupQueryParameters( String query ) {
    Set inputNames = getInputNames();
    Iterator iter = inputNames.iterator();
    while ( iter.hasNext() ) {
      String inputName = (String) iter.next();
      final IActionParameter inputParameter = getInputParameter( inputName );
      final Object value = inputParameter.getValue();
      if ( ( value instanceof String ) == false ) {
        continue;
      }
      String paramValue = (String) value;
      String param = "\\{" + inputName + "\\}"; //$NON-NLS-1$ //$NON-NLS-2$
      query = query.replaceAll( param, paramValue );
    }
    return query;
  }
}
