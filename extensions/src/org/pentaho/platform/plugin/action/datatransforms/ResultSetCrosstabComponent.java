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

package org.pentaho.platform.plugin.action.datatransforms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.PentahoDataTransmuter;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Set;

public class ResultSetCrosstabComponent extends ComponentBase {

  private static final long serialVersionUID = -868492439427599791L;

  private static final String RESULT_SET = "result_set"; //$NON-NLS-1$

  private static final String PIVOT_COLUMN = "pivot_column"; //$NON-NLS-1$

  private static final String MEASURES_COLUMN = "measures_column"; //$NON-NLS-1$

  private static final String FORMAT_TYPE = "format_type"; //$NON-NLS-1$

  private static final String FORMAT_STRING = "format_string"; //$NON-NLS-1$

  private static final String ORDERED_MAPS = "ordered_maps"; //$NON-NLS-1$

  private static final String TRANSFORM_SORTBYCOL = "sort_by_col"; //$NON-NLS-1$

  private static final String SORT_FORMAT_TYPE = "sort_format_type"; //$NON-NLS-1$

  private static final String SORT_FORMAT_STRING = "sort_format_string"; //$NON-NLS-1$

  private static final String OLD_STYLE_CROSSTAB = "non_ordered"; //$NON-NLS-1$

  private static final String UNIQUE_ROW_IDENTIFIER_COLUMN = "unique_row_identifier_column"; //$NON-NLS-1$ 

  @Override
  public void done() {
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean validateAction() {
    if ( !isDefinedInput( ResultSetCrosstabComponent.PIVOT_COLUMN ) ) {
      error( Messages.getInstance().getErrorString( "ResultSetCrosstabComponent.ERROR_0001_PIVOT_COLUMN_IS_REQUIRED" ) ); //$NON-NLS-1$
      return false;
    }
    if ( !isDefinedInput( ResultSetCrosstabComponent.MEASURES_COLUMN ) ) {
      error( Messages.getInstance()
          .getErrorString( "ResultSetCrosstabComponent.ERROR_0002_MEASURES_COLUMN_IS_REQUIRED" ) ); //$NON-NLS-1$
      return false;
    }
    if ( isDefinedInput( ResultSetCrosstabComponent.FORMAT_TYPE ) ) {
      if ( !isDefinedInput( ResultSetCrosstabComponent.FORMAT_STRING ) ) {
        error( Messages.getInstance().getErrorString( "ResultSetCrosstabComponent.ERROR_0003_FORMAT_PARAMETERS_BAD" ) ); //$NON-NLS-1$
        return false;
      }
    }
    if ( isDefinedInput( ResultSetCrosstabComponent.SORT_FORMAT_TYPE ) ) {
      if ( !isDefinedInput( ResultSetCrosstabComponent.SORT_FORMAT_STRING ) ) {
        error( Messages.getInstance().getErrorString(
          "ResultSetCrosstabComponent.ERROR_0004_SORT_FORMAT_PARAMETERS_BAD" ) ); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  @Override
  protected boolean executeAction() throws Throwable {
    Object resultSetObject = getInputValue( ResultSetCrosstabComponent.RESULT_SET );
    String outputName = getResultOutputName();
    if ( outputName == null ) {
      return false;
    }
    if ( resultSetObject instanceof IPentahoResultSet ) {
      int columnToPivot = Integer.parseInt( getInputStringValue( ResultSetCrosstabComponent.PIVOT_COLUMN ) );
      int measuresColumn = Integer.parseInt( getInputStringValue( ResultSetCrosstabComponent.MEASURES_COLUMN ) );
      String formatType = null;
      String formatString = null;
      if ( isDefinedInput( ResultSetCrosstabComponent.FORMAT_TYPE ) ) {
        formatType = getInputStringValue( ResultSetCrosstabComponent.FORMAT_TYPE );
        formatString = getInputStringValue( ResultSetCrosstabComponent.FORMAT_STRING );
      }
      Format format = null;

      // this is that rSet is now the valid result Create the format if there is one
      if ( ( formatType != null ) && ( formatType.length() > 0 ) ) {
        if ( StandardSettings.DECIMAL_FORMAT_TYPE.equalsIgnoreCase( formatType ) ) {
          format = new DecimalFormat( formatString );
        } else if ( StandardSettings.DATE_FORMAT_TYPE.equalsIgnoreCase( formatType ) ) {
          format = new SimpleDateFormat( formatString );
        }
      }
      // transform rSet here
      String orderedMaps = getInputStringValue( ResultSetCrosstabComponent.ORDERED_MAPS );
      boolean orderOutputColumns = "true".equalsIgnoreCase( orderedMaps ); //$NON-NLS-1$

      // Sort-by column information
      int transformSortByColumn = 0;
      String sortColumn = getInputStringValue( ResultSetCrosstabComponent.TRANSFORM_SORTBYCOL );
      if ( sortColumn != null ) {
        transformSortByColumn = Integer.parseInt( sortColumn );
      }

      //
      // Column that uniquely identifies a row
      // If supplied, will allow input rows to be un-ordered.
      //
      int uniqueRowIdentifierColumn = -1;
      if ( isDefinedInput( ResultSetCrosstabComponent.UNIQUE_ROW_IDENTIFIER_COLUMN ) ) {
        String tmp = getInputStringValue( ResultSetCrosstabComponent.UNIQUE_ROW_IDENTIFIER_COLUMN );
        uniqueRowIdentifierColumn = Integer.parseInt( tmp );
      }

      String sortFormatType = null;
      String sortFormatString = null;
      if ( isDefinedInput( ResultSetCrosstabComponent.SORT_FORMAT_TYPE ) ) {
        sortFormatString = getInputStringValue( ResultSetCrosstabComponent.SORT_FORMAT_STRING );
        sortFormatType = getInputStringValue( ResultSetCrosstabComponent.SORT_FORMAT_TYPE );
      }
      Format sortFormat = null;
      if ( ( sortFormatType != null ) && ( sortFormatType.length() > 0 ) ) {
        if ( StandardSettings.DECIMAL_FORMAT_TYPE.equalsIgnoreCase( sortFormatString ) ) {
          sortFormat = new DecimalFormat( sortFormatString );
        } else if ( StandardSettings.DATE_FORMAT_TYPE.equalsIgnoreCase( sortFormatType ) ) {
          sortFormat = new SimpleDateFormat( sortFormatString );
        }
      }

      IPentahoResultSet rSet = null;

      if ( isDefinedInput( ResultSetCrosstabComponent.OLD_STYLE_CROSSTAB ) ) {
        warn( Messages.getInstance().getString( "ResultSetCrosstabComponent.WARN_DEPRECATED" ) ); //$NON-NLS-1$
        rSet =
            PentahoDataTransmuter.crossTab( (IPentahoResultSet) resultSetObject, columnToPivot - 1, measuresColumn - 1,
                transformSortByColumn - 1, format, sortFormat, orderOutputColumns );
      } else {
        rSet =
            PentahoDataTransmuter.crossTabOrdered( (IPentahoResultSet) resultSetObject, columnToPivot - 1,
                measuresColumn - 1, transformSortByColumn - 1, format, sortFormat, orderOutputColumns,
                uniqueRowIdentifierColumn - 1 );
      }
      // then set the outputResult
      setOutputValue( outputName, rSet );

      return true;

    }
    return false;
  }

  @Override
  public boolean init() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true; // nothing here...
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ResultSetCrosstabComponent.class );
  }

  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() != 1 ) ) {
      error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
      return null;
    }
    String outputName = null;
    try {
      outputName = getInputStringValue( StandardSettings.OUTPUT_NAME );
    } catch ( Exception e ) {
      //ignore
    }
    if ( outputName == null ) { // Drop back to the old behavior
      outputName = (String) outputs.iterator().next();
    }
    return outputName;
  }

}
