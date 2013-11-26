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
 * Class for persisting lists and other collections. Using serialization to persist these items.
 * I'm using this class because I have a requirement to have a map element that may be a map or
 * some other collection of strings.
 */

package org.pentaho.platform.repository.hibernate.usertypes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.hibernate.util.EqualsHelper;
import org.hibernate.util.SerializationHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BlobUserType implements UserType {
  private static final Log log = LogFactory.getLog( BlobUserType.class );

  private static final boolean debug = PentahoSystem.debug;

  private static final int[] SQLTYPE = { Types.BLOB };

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  public int[] sqlTypes() {
    return BlobUserType.SQLTYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  public Class returnedClass() {
    return Serializable.class;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  public boolean equals( final Object arg0, final Object arg1 ) throws HibernateException {
    return EqualsHelper.equals( arg0, arg1 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
   */
  public int hashCode( final Object arg0 ) throws HibernateException {
    return arg0.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
   */
  public Object nullSafeGet( final ResultSet arg0, final String[] arg1, final Object arg2 ) throws HibernateException,
    SQLException {
    if ( BlobUserType.debug ) {
      BlobUserType.log.debug( Messages.getInstance().getString( "BLOBUTYPE.DEBUG_NULL_SAFE_GET" ) ); //$NON-NLS-1$
    }
    InputStream is = arg0.getBinaryStream( arg1[0] );
    if ( is != null ) {
      return SerializationHelper.deserialize( is );
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
    if ( BlobUserType.debug ) {
      BlobUserType.log.debug( Messages.getInstance().getString( "BLOBUTYPE.DEBUG_NULL_SAFE_SET" ) ); //$NON-NLS-1$
    }
    if ( arg1 != null ) {
      try {
        arg0.setBytes( arg2, SerializationHelper.serialize( (Serializable) arg1 ) );
      } catch ( SerializationException ex ) {
        BlobUserType.log.error( Messages.getInstance().getErrorString( "BLOBUTYPE.ERROR_0001_SETTING_BLOB" ), ex ); //$NON-NLS-1$
        throw new HibernateException( Messages.getInstance().getErrorString( "BLOBUTYPE.ERROR_0001_SETTING_BLOB" ), ex ); //$NON-NLS-1$
      }
    } else {
      arg0.setNull( arg2, sqlTypes()[0] );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  public Object deepCopy( final Object arg0 ) throws HibernateException {
    return SerializationHelper.clone( (Serializable) arg0 );
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
    return (Serializable) SerializationHelper.clone( (Serializable) arg0 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  public Object assemble( final Serializable arg0, final Object arg1 ) throws HibernateException {
    return SerializationHelper.clone( arg0 );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public Object replace( final Object arg0, final Object arg1, final Object arg2 ) throws HibernateException {
    return SerializationHelper.clone( (Serializable) arg0 );
  }

}
