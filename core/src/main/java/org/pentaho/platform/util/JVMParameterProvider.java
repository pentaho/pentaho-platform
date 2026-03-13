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


package org.pentaho.platform.util;

import org.pentaho.platform.api.engine.IParameterProvider;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

public class JVMParameterProvider implements IParameterProvider {

  public String getStringParameter( final String name, final String defaultValue ) {
    return System.getProperty( name, defaultValue );
  }

  public long getLongParameter( final String name, final long defaultValue ) {
    String pValue = this.getStringParameter( name, null );
    if ( pValue == null ) {
      return defaultValue;
    } else {
      try {
        return Long.parseLong( pValue );
      } catch ( NumberFormatException nfe ) {
        return defaultValue;
      }
    }
  }

  public boolean hasParameter( String name ) {
    return this.getParameter( name ) != null;
  }

  public Date getDateParameter( final String name, final Date defaultValue ) {
    return ParameterHelper.parameterToDate( getStringParameter( name, null ), defaultValue );
  }

  public Object getDecimalParameter( final String name, final Object defaultValue ) {
    String pValue = this.getStringParameter( name, null );
    if ( pValue == null ) {
      return defaultValue;
    } else {
      try {
        return new BigDecimal( pValue );
      } catch ( NumberFormatException ex ) {
        return defaultValue;
      }
    }
  }

  public Iterator getParameterNames() {
    Properties props = System.getProperties();
    return props.keySet().iterator();
  }

  public String getParameterType( final String name ) {
    return "string"; //$NON-NLS-1$
  }

  public Object getParameter( final String name ) {
    return System.getProperty( name );
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

}
