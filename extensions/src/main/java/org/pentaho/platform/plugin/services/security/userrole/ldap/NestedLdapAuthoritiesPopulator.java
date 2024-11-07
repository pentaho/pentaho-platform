/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Uses a map, defined in Spring, that maps child roles to parent roles. Using this map, one can specify a hierarchy of
 * roles that doesn't necessarily exist in the directory. Why would you need this? It is potentially prohibitive to
 * repeatedly query the directory to recursively find all parents of a given child role.
 * 
 * <p>
 * The map below specifies that the Marketing and Sales roles are child roles of the BIReporting role. So if user suzy
 * belongs to the Marketing role, she will be assigned both Marketing and BIReporting roles by the time this populator
 * returns.
 * </p>
 * 
 * <p>
 * Any role prefix and/or case manipulation must be present in this mapping. In other words, if a role prefix has been
 * set to <code>ROLE_</code>, and <code>convertToUpperCase</code> has been set to <code>true</code>, then both the keys
 * and values must begin with <code>ROLE_</code> and be all uppercase.
 * </p>
 * 
 * <pre>
 * &lt;property name="extraRolesMapping">
 *   &lt;map>
 *     &lt;entry key="Marketing" value="BIReporting" />
 *     &lt;entry key="Sales" value="BIReporting" />
 *   &lt;/map>
 * &lt;/property>
 * </pre>
 * 
 * <p>
 * Based on http://forum.springframework.org/showthread.php?t=28007
 * </p>
 * 
 * @author mlowery
 */
public class NestedLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator implements InitializingBean {

  // ~ Instance fields =======================================================

  /**
   * Map where keys are child roles and values are parent roles.
   */
  private Map extraRolesMapping;

  // ~ Constructors ==========================================================

  public NestedLdapAuthoritiesPopulator( final ContextSource contextSource, final String groupSearchBase ) {
    super( contextSource, groupSearchBase );
  }

  // ~ Methods ===============================================================

  /**
   * Calls super's implementation then adds extra roles.
   */
  public Set getGroupMembershipRoles( String userDn, String username ) {
    Set roles = super.getGroupMembershipRoles( userDn, username );
    Set newRolesFromPreviousPass = new HashSet( roles );
    Set allNewRoles = new HashSet();
    // keep going until no new roles are found
    while ( !newRolesFromPreviousPass.isEmpty() ) {
      newRolesFromPreviousPass = getParentRoles( newRolesFromPreviousPass );
      allNewRoles.addAll( newRolesFromPreviousPass );
    }
    roles.addAll( toGrantedAuthorities( allNewRoles ) );
    return roles;
  }

  /**
   * Iterates over the set, using the items as keys into the extraRolesMapping.
   * 
   * @param children
   *          <code>Set</code> of keys
   * @return <code>Set</code> of values retrieved from keys
   */
  protected Set getParentRoles( final Set children ) {
    Set parents = new HashSet();
    Iterator iter = children.iterator();
    while ( iter.hasNext() ) {
      Object parent = extraRolesMapping.get( iter.next() );
      if ( null != parent ) {
        parents.add( parent );
      }
    }
    return parents;
  }

  /**
   * Converts a set of strings into a set of granted authorities.
   * 
   * @param rolesAsStringsSet
   *          <code>Set</code> of <code>String</code> instances
   * @return <code>Set</code> of <code>GrantedAuthority</code> instances
   */
  protected Set toGrantedAuthorities( final Set rolesAsStringsSet ) {
    Set grantedAuthorities = new HashSet();
    Iterator iter = rolesAsStringsSet.iterator();
    while ( iter.hasNext() ) {
      String auth = (String) iter.next();
      grantedAuthorities.add( new SimpleGrantedAuthority( auth ) );
    }
    return grantedAuthorities;
  }

  public Map getExtraRolesMapping() {
    return extraRolesMapping;
  }

  public void setExtraRolesMapping( final Map extraRolesMapping ) {
    this.extraRolesMapping = extraRolesMapping;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( extraRolesMapping );
  }

}
