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

/*
 * This class is built to support saving and loading of long strings from hibernate. Specifically,
 * strings greater than 254 characters will be saved as CLOBs instead of varchars. The 254 character
 * limit was specifically chosen as the limit because MySQL has a 255 character limit on the length
 * of a varchar.
 * 
 * This implementation will return the CLOBs as StringBuffers.
 */

package org.pentaho.platform.repository.hibernate.usertypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LongStringUserType implements UserType {
  private static final Log log = LogFactory.getLog( LongStringUserType.class );

  private static final boolean debug = PentahoSystem.debug;

  private static final int[] SQLTYPE = { Types.CLOB }; // Persists as CLOBs

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  public int[] sqlTypes() {
    return LongStringUserType.SQLTYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  public Class returnedClass() {
    return StringBuffer.class;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  public boolean equals( final Object x, final Object y ) throws HibernateException {
    if ( x == y ) {
      return true;
    }
    if ( ( x == null ) || ( y == null ) || ( !( x instanceof StringBuffer ) ) || ( !( y instanceof StringBuffer ) ) ) {
      return false;
    }
    return x.toString().equals( y.toString() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
   */
  public int hashCode( final Object x ) throws HibernateException {
    return x.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
   */
  public Object nullSafeGet( final ResultSet rs, final String[] names, final Object owner ) throws HibernateException,
    SQLException {
    if ( LongStringUserType.debug ) {
      LongStringUserType.log.debug( Messages.getInstance().getString( "LONGSTRTYPE.DEBUG_NULL_SAFE_GET" ) ); //$NON-NLS-1$
    }
    String longStr = rs.getString( names[0] );
    return ( longStr != null ) ? new StringBuffer( longStr ) : null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
   */
  public void nullSafeSet( final PreparedStatement st, final Object value, final int index ) throws HibernateException,
    SQLException {
    if ( LongStringUserType.debug ) {
      LongStringUserType.log.debug( Messages.getInstance().getString( "LONGSTRTYPE.DEBUG_NULL_SAFE_SET" ) ); //$NON-NLS-1$
    }
    if ( value != null ) {
      StringReader rdr = new StringReader( value.toString() );
      int sLen = ( (StringBuffer) value ).length();
      st.setCharacterStream( index, rdr, sLen );
    } else {
      st.setNull( index, LongStringUserType.SQLTYPE[0] );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  public Object deepCopy( final Object value ) throws HibernateException {
    return new StringBuffer( value.toString() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#isMutable()
   */
  public boolean isMutable() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
   */
  public Serializable disassemble( final Object value ) throws HibernateException {
    return (Serializable) value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  public Object assemble( final Serializable cached, final Object owner ) throws HibernateException {
    return cached;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public Object replace( final Object original, final Object target, final Object owner ) throws HibernateException {
    return original;
  }
}
