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

package org.pentaho.platform.engine.services.solution;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.util.ParameterHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FlashChartRequestMapper extends SimpleParameterProvider {

  private IParameterProvider request;

  private final HashMap chartParams = new HashMap();

  private final Set keySet = new HashSet();

  public FlashChartRequestMapper( final IParameterProvider request ) {
    this.request = request;
    Iterator it = request.getParameterNames();
    while ( it.hasNext() ) {
      keySet.add( it.next() );
    }
    boolean isChart = "prochart".equals( request.getStringParameter( "client", null ) ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( isChart ) {
      String catName = request.getStringParameter( "categoryName", null ); //$NON-NLS-1$
      if ( catName != null ) {
        String value = request.getStringParameter( "category", null ); //$NON-NLS-1$
        if ( value != null ) {
          chartParams.put( catName, value );
          keySet.add( catName );
          keySet.remove( "category" ); //$NON-NLS-1$
          keySet.remove( "categoryName" ); //$NON-NLS-1$
        }
      }
      String seriesName = request.getStringParameter( "seriesName", null ); //$NON-NLS-1$
      if ( seriesName != null ) {
        String value = request.getStringParameter( "series", null ); //$NON-NLS-1$
        if ( value != null ) {
          chartParams.put( seriesName, value );
          keySet.add( seriesName );
          keySet.remove( "series" ); //$NON-NLS-1$
          keySet.remove( "seriesName" ); //$NON-NLS-1$
        }
      }
    }
  }

  @Override
  public String getStringParameter( final String name, final String defaultValue ) {
    Object value = chartParams.get( name );
    if ( value != null ) {
      return value.toString();
    } else {
      return request.getStringParameter( name, defaultValue );
    }
  }

  @Override
  public long getLongParameter( final String name, final long defaultValue ) {
    String value = (String) chartParams.get( name );
    if ( value != null ) {
      return ParameterHelper.parameterToLong( value, defaultValue );
    } else {
      return request.getLongParameter( name, defaultValue );
    }
  }

  @Override
  public Date getDateParameter( final String name, final Date defaultValue ) {
    String value = (String) chartParams.get( name );
    if ( value != null ) {
      return ParameterHelper.parameterToDate( value, defaultValue );
    } else {
      return request.getDateParameter( name, defaultValue );
    }
  }

  @Override
  public BigDecimal getDecimalParameter( final String name, final BigDecimal defaultValue ) {
    String value = (String) chartParams.get( name );
    if ( value != null ) {
      return ParameterHelper.parameterToDecimal( value, defaultValue );
    } else {
      return request.getDecimalParameter( name, defaultValue );
    }
  }

  @Override
  public Iterator getParameterNames() {
    return keySet.iterator();
  }

  @Override
  public Object getParameter( final String name ) {
    Object value = chartParams.get( name );
    if ( value != null ) {
      return value;
    } else {
      return request.getParameter( name );
    }
  }

}
