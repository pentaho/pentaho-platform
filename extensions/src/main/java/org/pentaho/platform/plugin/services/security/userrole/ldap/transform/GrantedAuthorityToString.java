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


package org.pentaho.platform.plugin.services.security.userrole.ldap.transform;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Transforms a <code>GrantedAuthority</code> into a <code>String</code>. Can handle either a single
 * <code>GrantedAuthority</code> or a collection (or array) of <code>GrantedAuthority</code> instances. Always returns a
 * collection or array if collection or array was the input.
 * 
 * <p>
 * Transformer input: <code>GrantedAuthority</code> instance, <code>Collection</code> of <code>GrantedAuthority</code>
 * instances, or array of <code>GrantedAuthority</code> instances.
 * </p>
 * <p>
 * Transformer output: <code>String</code> instance, <code>Collection</code> of <code>String</code> instances, or array
 * of <code>String</code> instances.
 * </p>
 * 
 * @author mlowery
 */
public class GrantedAuthorityToString implements Transformer {
  // ~ Static fields/initializers ============================================

  private static final Log logger = LogFactory.getLog( GrantedAuthorityToString.class );

  // ~ Instance fields =======================================================

  private String rolePrefix = "ROLE_"; //$NON-NLS-1$

  // ~ Constructors ==========================================================

  public GrantedAuthorityToString() {
    super();
  }

  // ~ Methods ===============================================================

  public Object transform( final Object obj ) {
    if ( GrantedAuthorityToString.logger.isDebugEnabled() ) {
      String input = ( obj instanceof Object[] ? Arrays.asList( (Object[]) obj ).toString() : obj.toString() );
      GrantedAuthorityToString.logger.debug( Messages.getInstance().getString(
        "GrantedAuthorityToString.DEBUG_INPUT_TO_TRANSFORM", //$NON-NLS-1$
        input ) );
    }

    Object transformed = obj;
    if ( obj instanceof GrantedAuthority ) {
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
    if ( obj instanceof GrantedAuthority ) {
      String auth = ( (GrantedAuthority) obj ).getAuthority();
      if ( auth.startsWith( rolePrefix ) ) {
        auth = auth.substring( rolePrefix.length() );
      }
      transformed = auth;
    }
    return transformed;
  }

  public void setRolePrefix( final String rolePrefix ) {
    Assert.hasLength( rolePrefix, "Role prefix must not be null or empty" );
    this.rolePrefix = rolePrefix;
  }

}
