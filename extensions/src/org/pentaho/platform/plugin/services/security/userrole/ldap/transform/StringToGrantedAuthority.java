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

package org.pentaho.platform.plugin.services.security.userrole.ldap.transform;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.security.GrantedAuthorityImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Transforms a <code>String</code> into a <code>GrantedAuthority</code>. Can handle either a single <code>String</code>
 * or a collection (or array) of <code>String</code> instances. Always returns a collection or array if collection or
 * array was the input.
 * 
 * <p>
 * Transformer input: <code>String</code> instance, <code>Collection</code> of <code>String</code> instances, or array
 * of <code>String</code> instances.
 * </p>
 * <p>
 * Transformer output: <code>GrantedAuthority</code> instance, <code>Collection</code> of <code>GrantedAuthority</code>
 * instances, or array of <code>GrantedAuthority</code> instances.
 * </p>
 * 
 * @author mlowery
 */
public class StringToGrantedAuthority implements Transformer {
  // ~ Static fields/initializers ============================================
  private static final Log logger = LogFactory.getLog( StringToGrantedAuthority.class );

  // ~ Instance fields =======================================================

  private String rolePrefix = "ROLE_"; //$NON-NLS-1$

  private boolean convertToUpperCase = true;

  // ~ Constructors ==========================================================
  public StringToGrantedAuthority() {
    super();
  }

  // ~ Methods ===============================================================

  public Object transform( final Object obj ) {
    if ( StringToGrantedAuthority.logger.isDebugEnabled() ) {
      StringToGrantedAuthority.logger.debug( Messages.getInstance().getString(
          "StringToGrantedAuthority.DEBUG_INPUT_TO_TRANSFORM", ( null != obj ) ? obj.toString() : "null" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    Object transformed = obj;
    if ( obj instanceof String ) {
      transformed = transformItem( obj );
    } else if ( obj instanceof Collection ) {
      transformed = new HashSet();
      Set authSet = (Set) transformed;
      Iterator iter = ( (Collection) obj ).iterator();
      while ( iter.hasNext() ) {
        authSet.add( transformItem( iter.next() ) );
      }
    } else if ( obj instanceof Object[] ) {
      transformed = new HashSet();
      Set authSet = (Set) transformed;
      Object[] objArray = (Object[]) obj;
      for ( Object element : objArray ) {
        authSet.add( transformItem( element ) );
      }
      transformed = authSet.toArray();
    }
    return transformed;
  }

  protected Object transformItem( final Object obj ) {
    Object transformed = obj;
    if ( obj instanceof String ) {
      String converted = rolePrefix + ( convertToUpperCase ? ( (String) obj ).toUpperCase() : obj.toString() );
      transformed = new GrantedAuthorityImpl( converted );
    }
    return transformed;
  }

  public void setConvertToUpperCase( final boolean convertToUpperCase ) {
    this.convertToUpperCase = convertToUpperCase;
  }

  public void setRolePrefix( final String rolePrefix ) {
    this.rolePrefix = rolePrefix;
  }

}
