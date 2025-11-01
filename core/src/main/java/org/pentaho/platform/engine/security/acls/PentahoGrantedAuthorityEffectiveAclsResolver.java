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


package org.pentaho.platform.engine.security.acls;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.api.engine.IAclEntry;
import org.pentaho.platform.api.engine.IPentahoBasicAclEntry;

import java.util.List;
import java.util.Vector;

/**
 * This is a port from spring-security 2.0.8.RELEASE
 * @see https://github.com/spring-projects/spring-security/blob/2.0.8.RELEASE/core/src/main/java/org/springframework/security/acl/basic/GrantedAuthorityEffectiveAclsResolver.java
 */
public class PentahoGrantedAuthorityEffectiveAclsResolver {

  //~ Static fields/initializers =====================================================================================

  private static final Log logger = LogFactory.getLog( PentahoGrantedAuthorityEffectiveAclsResolver.class );

  //~ Methods ========================================================================================================

  public IAclEntry[] resolveEffectiveAcls( IAclEntry[] allAcls, Authentication filteredBy ) {
    if ( ( allAcls == null ) || ( allAcls.length == 0 ) ) {
      return null;
    }

    List list = new Vector();

    if ( logger.isDebugEnabled() ) {
      logger.debug( "Locating AclEntry[]s (from set of " + ( ( allAcls == null ) ? 0 : allAcls.length )
        + ") that apply to Authentication: " + filteredBy );
    }

    for ( int i = 0; i < allAcls.length; i++ ) {
      if ( !( allAcls[i] instanceof IPentahoBasicAclEntry ) ) {
        continue;
      }

      Object recipient = ( (IPentahoBasicAclEntry) allAcls[i] ).getRecipient();

      // Allow the Authentication's getPrincipal to decide whether
      // the presented recipient is "equal" (allows BasicAclDaos to
      // return Strings rather than proper objects in simple cases)
      if ( filteredBy.getPrincipal().equals( recipient ) ) {
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Principal matches AclEntry recipient: " + recipient );
        }

        list.add( allAcls[i] );
      } else if ( filteredBy.getPrincipal() instanceof UserDetails
        && ( (UserDetails) filteredBy.getPrincipal() ).getUsername().equals( recipient ) ) {
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Principal (from UserDetails) matches AclEntry recipient: " + recipient );
        }

        list.add( allAcls[i] );
      } else {
        // No direct match against principal; try each authority.
        // As with the principal, allow each of the Authentication's
        // granted authorities to decide whether the presented
        // recipient is "equal"
        Collection<? extends GrantedAuthority> authoritiesCollection = filteredBy.getAuthorities();
        GrantedAuthority[] authorities = authoritiesCollection.toArray( new GrantedAuthority[]{} );

        if ( ( authorities == null ) || ( authorities.length == 0 ) ) {
          if ( logger.isDebugEnabled() ) {
            logger.debug( "Did not match principal and there are no granted authorities, "
              + "so cannot compare with recipient: " + recipient );
          }

          continue;
        }

        for ( int k = 0; k < authorities.length; k++ ) {
          if ( authorities[k].equals( recipient ) ) {
            if ( logger.isDebugEnabled() ) {
              logger.debug( "GrantedAuthority: " + authorities[k] + " matches recipient: " + recipient );
            }

            list.add( allAcls[i] );
          }
        }
      }
    }

    // return null if appropriate (as per interface contract)
    if ( list.size() > 0 ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Returning effective AclEntry array with " + list.size() + " elements" );
      }

      return (IPentahoBasicAclEntry[]) list.toArray( new IPentahoBasicAclEntry[] {} );
    } else {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Returning null AclEntry array as zero effective AclEntrys found" );
      }

      return null;
    }
  }
}
