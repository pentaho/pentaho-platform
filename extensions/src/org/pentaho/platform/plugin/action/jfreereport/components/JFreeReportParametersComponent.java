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
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;

import javax.swing.table.TableModel;
import java.util.Iterator;
import java.util.Set;

/**
 * This sets the report parameters. This is another data preparation step and should be executed before the query is
 * fired. The data-factory may depend on these parameters.
 * 
 * @deprecated
 * @author Thomas Morgner
 */
@Deprecated
public class JFreeReportParametersComponent extends AbstractJFreeReportComponent {
  private static final long serialVersionUID = -3760254131944082953L;

  public JFreeReportParametersComponent() {
  }

  @Override
  protected boolean validateAction() {
    return isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT );
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {

  }

  protected MasterReport getReport() {
    Object maybeReport = getInputValue( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT );
    if ( maybeReport instanceof MasterReport ) {
      return (MasterReport) maybeReport;
    }
    error( Messages.getInstance().getString( "JFreeReportParametersComponent.ERROR_0033_NO_REPORT_BOUND" ) + maybeReport ); //$NON-NLS-1$
    return null;
  }

  private boolean initReportInputs() throws CloneNotSupportedException {
    MasterReport report = getReport();
    if ( report == null ) {
      error( Messages.getInstance().getString( "JFreeReportParametersComponent.ERROR_0034_NO_REPORT_DEFINITION" ) ); //$NON-NLS-1$
      return false;
    }

    final boolean privateCopy =
        getInputBooleanValue( AbstractJFreeReportComponent.REPORTPARAMCOMPONENT_PRIVATEREPORT_OUTPUT, false );
    if ( privateCopy && isDefinedOutput( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT ) ) {
      report = (MasterReport) report.clone();
    }

    // Get input parameters, and set them as properties in the report
    // object.
    final Set paramNames = getInputNames();
    final Iterator it = paramNames.iterator();
    while ( it.hasNext() ) {
      String paramName = (String) it.next();
      Object paramValue = getInputValue( paramName );
      if ( ( paramValue == null ) || "".equals( paramValue ) ) { //$NON-NLS-1$
        continue;
      }

      // we filter some well-known bad-guys. It is dangerous to have the
      // report-object (the parsed JFreeReport object), the "report-data"
      // (the tablemodel) or the "data" reference copied to the report.
      // also dangerous are result sets and table models.

      if ( paramValue instanceof IPentahoResultSet ) {
        continue;
      }
      if ( paramValue instanceof TableModel ) {
        continue;
      }
      if ( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT.equals( paramName ) ) {
        continue;
      }
      if ( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_DATAINPUT.equals( paramName ) ) {
        continue;
      }
      if ( AbstractJFreeReportComponent.DATACOMPONENT_DATAINPUT.equals( paramName ) ) {
        continue;
      }
      /*
       * WG: Commenting out because this change (SVN: 44880) breaks bi-developers / reporting / subreport.xaction we'll
       * need to revisit this when reving to the 4.0 reporting engine.
       * 
       * final ParameterDefinitionEntry[] parameterDefinitions =
       * report.getParameterDefinition().getParameterDefinitions(); boolean foundParameter = false; for (int j = 0; j <
       * parameterDefinitions.length; j++) { final ParameterDefinitionEntry definition = parameterDefinitions[j]; if
       * (paramName.equals(definition.getName())) { foundParameter = true; break; } } if (foundParameter == false) { if
       * (report.getParameterDefinition() instanceof ModifiableReportParameterDefinition) { final
       * ModifiableReportParameterDefinition parameterDefinition = (ModifiableReportParameterDefinition)
       * report.getParameterDefinition(); parameterDefinition.addParameterDefinition(new PlainParameter(paramName)); } }
       */

      if ( paramValue instanceof Object[] ) {
        Object[] values = (Object[]) paramValue;
        StringBuffer valuesBuffer = new StringBuffer();
        // TODO support non-string items
        for ( int j = 0; j < values.length; j++ ) {
          if ( j == 0 ) {
            valuesBuffer.append( values[j].toString() );
          } else {
            valuesBuffer.append( ',' ).append( values[j].toString() );
          }
        }
        report.getParameterValues().put( paramName, valuesBuffer.toString() );
        // report.setProperty(paramName, valuesBuffer.toString());
      } else {
        report.getParameterValues().put( paramName, paramValue );
        // report.setProperty(paramName, paramValue);
      }
    }

    if ( privateCopy && isDefinedInput( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT ) ) {
      addTempParameterObject( AbstractJFreeReportComponent.DATACOMPONENT_REPORTTEMP_OBJINPUT, report );
    }
    return true;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    return initReportInputs();
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportParametersComponent.class );
  }
}
