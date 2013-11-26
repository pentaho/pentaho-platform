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

package org.pentaho.platform.engine.core.solution;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.util.ParameterHelper;

import java.math.BigDecimal;
import java.util.Date;

public abstract class BaseParameterProvider implements IParameterProvider {

  protected abstract String getValue( String name );

  public abstract Object getParameter( String name );

  public String getStringParameter( final String name, final String defaultValue ) {
    return ParameterHelper.parameterToString( getValue( name ), defaultValue );
  }

  public IPentahoResultSet getListParameter( final String name ) {
    return (IPentahoResultSet) getParameter( name );
  }

  public long getLongParameter( final String name, final long defaultValue ) {
    return ParameterHelper.parameterToLong( getValue( name ), defaultValue );
  }

  public Date getDateParameter( final String name, final Date defaultValue ) {
    Object value = getParameter( name );
    if ( value == null ) {
      return defaultValue;
    }
    if ( value instanceof Date ) {
      return (Date) value;
    }
    return ParameterHelper.parameterToDate( getValue( name ), defaultValue );
  }

  public BigDecimal getDecimalParameter( final String name, final BigDecimal defaultValue ) {
    return ParameterHelper.parameterToDecimal( getStringParameter( name, "" ), defaultValue ); //$NON-NLS-1$
  }

  public Object[] getArrayParameter( final String name, final Object[] defaultValue ) {
    return ParameterHelper.parameterToObjectArray( getParameter( name ), defaultValue );
  }

  public String[] getStringArrayParameter( final String name, final String[] defaultValue ) {
    return ParameterHelper.parameterToStringArray( getParameter( name ), defaultValue );
  }

  public boolean hasParameter( String name ) {
    return this.getParameter( name ) != null;
  }

}
