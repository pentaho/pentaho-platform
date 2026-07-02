/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
