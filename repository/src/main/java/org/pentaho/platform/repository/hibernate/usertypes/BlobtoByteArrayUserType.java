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
 * Class for persisting lists and other collections. Using serialization to persist these items.
 * I'm using this class because I have a requirement to have a map element that may be a map or
 * some other collection of strings.
 */

package org.pentaho.platform.repository.hibernate.usertypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.pentaho.platform.repository.messages.Messages;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BlobtoByteArrayUserType implements UserType {
  private static final Log log = LogFactory.getLog( BlobtoByteArrayUserType.class );

  private static final int SQLTYPE =  Types.BLOB ;

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#getSqlType()
   */
  @Override
  public int getSqlType() {
    return BlobtoByteArrayUserType.SQLTYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  public Class returnedClass() {
    return byte[].class;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  public boolean equals( final Object x, final Object y ) throws HibernateException {
    return ( x == y ) || ( ( x != null ) && ( y != null ) && java.util.Arrays.equals( (byte[]) x, (byte[]) y ) );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
   */
  public int hashCode( final Object arg0 ) throws HibernateException {
    return arg0.hashCode();
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
  public Object nullSafeGet( final ResultSet arg0, final String[] arg1, final Object arg2 ) throws HibernateException,
    SQLException {

    Blob blob = arg0.getBlob( arg1[0] );
    if ( blob != null ) {
      byte[] bytes = blob.getBytes( 1, (int) blob.length() );
      return bytes;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
   */
  public void nullSafeSet( final PreparedStatement arg0, final Object arg1, final int arg2 ) throws HibernateException,
    SQLException {

    if ( arg1 != null ) {
      try {
        arg0.setBytes( arg2, (byte[]) arg1 );
      } catch ( SerializationException ex ) {
        BlobtoByteArrayUserType.log.error(
            Messages.getInstance().getErrorString( "BLOBUTYPE.ERROR_0001_SETTING_BLOB" ), ex ); //$NON-NLS-1$
        throw new HibernateException( Messages.getInstance().getErrorString( "BLOBUTYPE.ERROR_0001_SETTING_BLOB" ), ex ); //$NON-NLS-1$
      }
    } else {
      arg0.setNull( arg2, getSqlType());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  public Object deepCopy( final Object value ) {
    if ( value == null ) {
      return null;
    }

    byte[] bytes = (byte[]) value;
    byte[] result = new byte[bytes.length];
    System.arraycopy( bytes, 0, result, 0, bytes.length );

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#isMutable()
   */
  public boolean isMutable() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
   */
  public Serializable disassemble( final Object arg0 ) throws HibernateException {
    return (Serializable) deepCopy( arg0 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  public Object assemble( final Serializable arg0, final Object arg1 ) throws HibernateException {
    return ( deepCopy( arg0 ) );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public Object replace( final Object arg0, final Object arg1, final Object arg2 ) throws HibernateException {
    return ( deepCopy( arg0 ) );
  }

}
