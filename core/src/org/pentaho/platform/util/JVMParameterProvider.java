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
