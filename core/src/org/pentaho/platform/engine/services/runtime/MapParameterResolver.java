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

package org.pentaho.platform.engine.services.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.messages.Messages;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * This is a utility class that implements the IParameterResolver and resolves parameters based on a lookup map
 * provided.
 * 
 * @author Will Gorman
 * 
 * @see MDXBaseComponent
 * @see HQLBaseComponent
 * @see XQueryBaseComponent
 */
public class MapParameterResolver extends PentahoMessenger implements IParameterResolver {

  private static final long serialVersionUID = -93516661348245465L;

  Map lookupMap;

  String prefix = null;

  IRuntimeContext runtimecontext = null;

  public MapParameterResolver( final Map map, final String prefix, final IRuntimeContext runtime ) {
    lookupMap = map;
    this.prefix = prefix;
    runtimecontext = runtime;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( MapParameterResolver.class );
  }

  /**
   * This method is called when TemplateUtil.applyTemplate() encounters a parameter.
   * 
   * @param template
   *          the source string
   * @param parameter
   *          the parameter value
   * @param parameterMatcher
   *          the regex parameter matcher
   * @param copyStart
   *          the start of the copy
   * @param results
   *          the output result
   * @return the next copystart
   */
  public int resolveParameter( final String template, final String parameter, final Matcher parameterMatcher,
      int copyStart, final StringBuffer results ) {

    StringTokenizer tokenizer = new StringTokenizer( parameter, ":" ); //$NON-NLS-1$
    if ( tokenizer.countTokens() == 2 ) { // Currently, the component only handles one bit of metadata
      String parameterPrefix = tokenizer.nextToken();
      String inputName = tokenizer.nextToken();

      if ( parameterPrefix.equals( prefix ) ) {
        // We know this parameter is for us.
        // First, is this a special input
        Object parameterValue = TemplateUtil.getSystemInput( inputName, runtimecontext );
        if ( ( parameterValue == null ) && lookupMap.containsKey( inputName ) ) {
          parameterValue = lookupMap.get( inputName );
        }
        if ( parameterValue != null ) {
          // We have a parameter value - now, it's time to create a parameter and build up the
          // parameter string
          int start = parameterMatcher.start();
          int end = parameterMatcher.end();

          // We now have a valid start and end. It's time to see whether we're dealing
          // with an array, a result set, or a scalar.
          StringBuffer parameterBuffer = new StringBuffer();

          // find and remove the next placeholder, to be replaced by the new value

          if ( parameterValue instanceof String ) {
            parameterBuffer.append( ( (String) parameterValue ).replaceAll( "'", "\\'" ) ); //$NON-NLS-1$ //$NON-NLS-2$
          } else if ( parameterValue instanceof Object[] ) {
            Object[] pObj = (Object[]) parameterValue;
            for ( Object element : pObj ) {
              // TODO: escape quotes!
              parameterBuffer.append( ( parameterBuffer.length() == 0 ) ? "'" + element + "'" : ",'" + element + "'" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
          } else if ( parameterValue instanceof IPentahoResultSet ) {
            IPentahoResultSet rs = (IPentahoResultSet) parameterValue;
            // See if we can find a column in the metadata with the same
            // name as the input
            IPentahoMetaData md = rs.getMetaData();
            int columnIdx = -1;
            if ( md.getColumnCount() == 1 ) {
              columnIdx = 0;
            } else {
              columnIdx = md.getColumnIndex( new String[] { parameter } );
            }
            if ( columnIdx < 0 ) {
              error( Messages.getInstance().getErrorString( "Template.ERROR_0005_COULD_NOT_DETERMINE_COLUMN" ) ); //$NON-NLS-1$
              return -1;
            }
            int rowCount = rs.getRowCount();
            Object valueCell = null;
            // TODO support non-string columns
            for ( int i = 0; i < rowCount; i++ ) {
              valueCell = rs.getValueAt( i, columnIdx );

              // TODO: escape quotes!
              parameterBuffer.append( ( parameterBuffer.length() == 0 )
                  ? "'" + valueCell + "'" : ",'" + valueCell + "'" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
          } else if ( parameterValue instanceof List ) {
            List pObj = (List) parameterValue;
            for ( int i = 0; i < pObj.size(); i++ ) {
              parameterBuffer.append( ( parameterBuffer.length() == 0 )
                  ? "'" + pObj.get( i ) + "'" : ",'" + pObj.get( i ) + "'" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
          } else {
            // If we're here, we know parameterValue is not null and not a string
            parameterBuffer.append( parameterValue.toString().replaceAll( "'", "\\'" ) ); //$NON-NLS-1$ //$NON-NLS-2$
          }

          // OK - We have a parameterBuffer and have filled out the preparedParameters
          // list. It's time to change the SQL to insert our parameter marker and tell
          // the caller we've done our job.
          results.append( template.substring( copyStart, start ) );
          copyStart = end;
          results.append( parameterBuffer );
          return copyStart;
        }
      }
    }

    return -1; // Nothing here for us - let default behavior through
  }
}
