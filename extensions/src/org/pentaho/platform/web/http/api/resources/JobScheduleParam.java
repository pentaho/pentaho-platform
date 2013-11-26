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

package org.pentaho.platform.web.http.api.resources;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JobScheduleParam implements Serializable {

  private static final long serialVersionUID = -4214459740606299083L;

  private static final SimpleDateFormat isodatetime = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ" ); //$NON-NLS-1$

  String name;

  String type;

  ArrayList<String> stringValue = new ArrayList<String>();

  public JobScheduleParam() {
  }

  public JobScheduleParam( String name, String value ) {
    this.name = name;
    this.type = "string"; //$NON-NLS-1$
    stringValue.add( value );
  }

  public JobScheduleParam( String name, Number value ) {
    this.name = name;
    this.type = "number"; //$NON-NLS-1$
    stringValue.add( value != null ? value.toString() : null );
  }

  public JobScheduleParam( String name, Date value ) {
    this.name = name;
    this.type = "date"; //$NON-NLS-1$
    stringValue.add( value != null ? isodatetime.format( value ) : null );
  }

  public JobScheduleParam( String name, Boolean value ) {
    this.name = name;
    this.type = "boolean"; //$NON-NLS-1$
    stringValue.add( value != null ? value.toString() : null );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public ArrayList<String> getStringValue() {
    return stringValue;
  }

  public void setStringValue( ArrayList<String> value ) {
    this.stringValue = value;
  }

  public Serializable getValue() {
    Serializable object = null;
    if ( type.equals( "string" ) ) { //$NON-NLS-1$
      if ( stringValue.size() > 0 ) {
        object = stringValue.get( 0 );
      }
    } else if ( type.equals( "number" ) ) { //$NON-NLS-1$
      if ( stringValue.size() > 0 ) {

        if ( !( stringValue.get( 0 ).indexOf( "." ) < 0 ) ) { //$NON-NLS-1$
          // Parse DOUBLE/FLOAT
          object = Double.parseDouble( stringValue.get( 0 ) );

          if ( (Double) object <= Float.MAX_VALUE ) {
            object = Float.parseFloat( stringValue.get( 0 ) );
          }
        } else {
          // Parse LONG/INT
          object = Long.parseLong( stringValue.get( 0 ) );

          if ( (Long) object <= Integer.MAX_VALUE ) {
            object = Integer.parseInt( stringValue.get( 0 ) );
          }
        }
      }
    } else if ( type.equals( "boolean" ) ) { //$NON-NLS-1$
      if ( stringValue.size() > 0 ) {
        object = Boolean.valueOf( stringValue.get( 0 ) );
      }
    } else if ( type.equals( "date" ) ) { //$NON-NLS-1$
      if ( stringValue.size() > 0 ) {
        try {
          object = isodatetime.parse( stringValue.get( 0 ) );
        } catch ( ParseException e ) {
          throw new IllegalArgumentException( e );
        }
      }
    } else if ( type.equals( "string[]" ) ) { //$NON-NLS-1$
      object = new String[stringValue.size()];
      int i = 0;
      for ( String string : stringValue ) {
        ( (String[]) object )[i++] = string;
      }
    } else if ( type.equals( "number[]" ) ) { //$NON-NLS-1$
      object = new Number[stringValue.size()];
      int i = 0;
      for ( String string : stringValue ) {
        ( (Number[]) object )[i++] = string.indexOf( "." ) < 0 ? Integer.parseInt( string ) : Float.parseFloat( string ); //$NON-NLS-1$
      }
    } else if ( type.equals( "boolean[]" ) ) { //$NON-NLS-1$
      object = new Boolean[stringValue.size()];
      int i = 0;
      for ( String string : stringValue ) {
        ( (Boolean[]) object )[i++] = Boolean.valueOf( string );
      }
    } else if ( type.equals( "date[]" ) ) { //$NON-NLS-1$
      object = new Date[stringValue.size()];
      int i = 0;
      for ( String string : stringValue ) {
        try {
          ( (Date[]) object )[i++] = isodatetime.parse( string );
        } catch ( ParseException e ) {
          throw new IllegalArgumentException( e );
        }
      }
    }
    return object;
  }
}
