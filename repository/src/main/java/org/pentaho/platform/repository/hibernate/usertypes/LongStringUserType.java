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
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

// UserType nullSafeGet() and nullSafeSet() different signature number of arguments we would have to implement the new methods
// nullSafeGet() and nullSafeSet()
public class LongStringUserType implements UserType {
  private static final Log log = LogFactory.getLog( LongStringUserType.class );

  private static final boolean debug = PentahoSystem.debug;

  private static final int SQLTYPE = Types.CLOB; // Persists as CLOBs

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#getSqlType()
   */
  @Override
  public int getSqlType() {
    return java.sql.Types.CLOB;
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

  @Override
  public Object nullSafeGet( ResultSet resultSet, int i, SharedSessionContractImplementor sharedSessionContractImplementor, Object o ) throws SQLException {
    return null;
  }

  @Override
  public void nullSafeSet( PreparedStatement st, Object value, int index, SharedSessionContractImplementor session )
    throws HibernateException, SQLException {

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
      st.setNull( index, LongStringUserType.SQLTYPE );
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
