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
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.base.config.ModifiableConfiguration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @deprecated
 */
@Deprecated
public class JFreeReportConfigParameterComponent extends AbstractJFreeReportComponent {

  private static final long serialVersionUID = 6599442849458920502L;

  private static final String REPORT_CONFIG_INPUT_PARAM = "config_parameters"; //$NON-NLS-1$

  private static final String REPORT_CONFIG_COMPDEFN = "report_configuration_parameters"; //$NON-NLS-1$

  public JFreeReportConfigParameterComponent() {
    // TODO Auto-generated constructor stub
    super();
  }

  @Override
  public void done() {
    // Nothing to do here...
  }

  private boolean initReportConfigParameters() {
    boolean result = true;
    if ( isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT ) ) {
      Object maybeReport = getInputValue( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT );
      if ( maybeReport instanceof MasterReport ) {
        MasterReport report = (MasterReport) maybeReport;
        // We should have our report object at this point.
        if ( isDefinedInput( JFreeReportConfigParameterComponent.REPORT_CONFIG_INPUT_PARAM ) ) {
          // It's coming in as an input parameter
          Object reportConfigParams =
              this.getInputValue( JFreeReportConfigParameterComponent.REPORT_CONFIG_INPUT_PARAM );
          if ( reportConfigParams instanceof IPentahoResultSet ) {
            setReportConfigParameters( report, (IPentahoResultSet) reportConfigParams );
          } else if ( reportConfigParams instanceof Map ) {
            setReportConfigParameters( report, (Map) reportConfigParams );
          } else {
            error( Messages.getInstance().getErrorString(
              "JFreeReport.ERROR_0026_UNKNOWN_REPORT_CONFIGURATION_PARAMETERS" ) ); //$NON-NLS-1$
            result = false;
          }

        } else {
          Node compDef = getComponentDefinition();
          List configNodes = compDef.selectNodes( JFreeReportConfigParameterComponent.REPORT_CONFIG_COMPDEFN + "/*" ); //$NON-NLS-1$
          if ( ( configNodes != null ) && ( configNodes.size() > 0 ) ) {
            setReportConfigParameters( report, configNodes );
          }
        }
      }
    }
    return result;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    return initReportConfigParameters();
  }

  @Override
  public boolean init() {
    return true;
  }

  private void setReportConfigParameters( final MasterReport report, final List configNodes ) {
    // We have some configuration parameters in the component definition
    String parmName = null;
    String parmValue = null;
    for ( int i = 0; i < configNodes.size(); i++ ) {
      Node aNode = (Node) configNodes.get( i );
      parmName = XmlDom4JHelper.getNodeText( "@name", aNode, null ); //$NON-NLS-1$
      if ( ( parmName == null ) || ( parmName.length() == 0 ) ) {
        // Ignore configuration settings without name=
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0027_REPORT_CONFIGURATION_PARAMETER_IGNORED"
        ) ); //$NON-NLS-1$
        continue;
      }
      parmValue = aNode.getText();
      if ( parmValue != null ) {
        parmValue = parmValue.trim();
        if ( parmValue.length() > 0 ) {
          report.getReportConfiguration().setConfigProperty( parmName, applyInputsToFormat( parmValue ) );
        } else {
          error( Messages.getInstance()
              .getErrorString( "JFreeReport.ERROR_0027_REPORT_CONFIGURATION_PARAMETER_IGNORED" ) ); //$NON-NLS-1$            
        }
      } else {
        error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0027_REPORT_CONFIGURATION_PARAMETER_IGNORED" ) ); //$NON-NLS-1$          
      }
    }

  }

  private void setReportConfigParameters( final MasterReport report, final Map values ) {
    Map.Entry ent;
    ModifiableConfiguration config = report.getReportConfiguration();
    Iterator it = values.entrySet().iterator();
    while ( it.hasNext() ) {
      ent = (Map.Entry) it.next();
      if ( ( ent.getKey() != null ) && ( ent.getValue() != null ) ) {
        config.setConfigProperty( ent.getKey().toString(), applyInputsToFormat( ent.getValue().toString() ) );
      }
    }
  }

  private void setReportConfigParameters( final MasterReport report, final IPentahoResultSet values ) {
    int rowCount = values.getRowCount();
    int colCount = values.getColumnCount();
    ModifiableConfiguration config = report.getReportConfiguration();
    if ( colCount >= 2 ) {
      IPentahoMetaData md = values.getMetaData();
      int nameIdx = md.getColumnIndex( "name" ); //$NON-NLS-1$
      int valIdx = md.getColumnIndex( "value" ); //$NON-NLS-1$
      if ( nameIdx < 0 ) {
        nameIdx = 0;
      }
      if ( valIdx < 0 ) {
        valIdx = 1;
      }
      for ( int i = 0; i < rowCount; i++ ) {
        Object[] aRow = values.getDataRow( i );
        if ( ( aRow[nameIdx] != null ) && ( aRow[valIdx] != null ) ) {
          config.setConfigProperty( aRow[nameIdx].toString(), applyInputsToFormat( aRow[valIdx].toString() ) );
        }
      }
    } else {
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0025_INVALID_REPORT_CONFIGURATION_PARAMETERS" ) ); //$NON-NLS-1$
    }
  }

  @Override
  protected boolean validateAction() {
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportConfigParameterComponent.class );
  }

}
