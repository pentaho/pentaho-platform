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


package org.pentaho.platform.repository.hibernate.usertypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.hibernate.type.internal.NamedBasicTypeImpl;
import org.hibernate.usertype.UserType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class EmptyStringUserType implements UserType {
  private static final Log log = LogFactory.getLog( EmptyStringUserType.class );

  private static final boolean debug = PentahoSystem.debug;

  private static final String PENTAHOEMPTY = Messages.getInstance()
      .getString( "EMPTYSTRTYPE.CODE_PENTAHO_EMPTY_STRING" ); //$NON-NLS-1$

  private static final int SQLTYPE = Types.VARCHAR ;

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#getSqlType()
   */
  @Override
  public int getSqlType() {
    return SQLTYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  public Class returnedClass() {
    return String.class;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  public boolean equals( final Object arg0, final Object arg1 ) throws HibernateException {
    return equals(  arg0, arg1  );
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
    if ( EmptyStringUserType.debug ) {
      EmptyStringUserType.log.debug( Messages.getInstance().getString( "EMPTYSTRTYPE.DEBUG_NULL_SAFE_GET" ) ); //$NON-NLS-1$
    }

    String colValue = arg0.getString( arg1[ 0 ] );
    // _PENTAHOEMPTY_ shouldn't appear in the wild. So, check the string in
    // the DB for this flag,
    // and if it's there, then this must be an empty string.
    return ( ( colValue != null ) ? ( colValue.equals( EmptyStringUserType.PENTAHOEMPTY ) ? "" : colValue ) : null ); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
   */
  public void nullSafeSet( final PreparedStatement arg0, final Object arg1, final int arg2 ) throws HibernateException,
    SQLException {
    // If this is an empty string, write _PENTAHOEMPTY_ into the database.
    if ( EmptyStringUserType.debug ) {
      EmptyStringUserType.log.debug( Messages.getInstance().getString( "EMPTYSTRTYPE.DEBUG_NULL_SAFE_SET" ) ); //$NON-NLS-1$
    }

     new NamedBasicTypeImpl( StringJavaType.INSTANCE, VarcharJdbcType.INSTANCE, "String" ).nullSafeSet( arg0, ( arg1 != null ) ? ( ( ( (String) arg1 ).length() > 0 ) ? arg1
        : EmptyStringUserType.PENTAHOEMPTY ) : arg1, arg2, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  public Object deepCopy( final Object arg0 ) throws HibernateException {
    return arg0;
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
  public Serializable disassemble( final Object arg0 ) throws HibernateException {
    return (Serializable) arg0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  public Object assemble( final Serializable arg0, final Object arg1 ) throws HibernateException {
    return arg0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public Object replace( final Object arg0, final Object arg1, final Object arg2 ) throws HibernateException {
    return arg0;
  }

}
