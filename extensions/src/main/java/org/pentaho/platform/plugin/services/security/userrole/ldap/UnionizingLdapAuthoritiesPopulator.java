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


package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Delegates to populators and unions the results. Use in conjunction with {@code UnionizingLdapSearch}.
 * 
 * @author mlowery
 */
public class UnionizingLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private Set<LdapAuthoritiesPopulator> populators;

  // ~ Constructors ====================================================================================================

  public UnionizingLdapAuthoritiesPopulator() {
    super();
  }

  // ~ Methods =========================================================================================================

  public Collection<? extends GrantedAuthority> getGrantedAuthorities( final DirContextOperations userData, final String username ) {
    Set<GrantedAuthority> allAuthorities = new HashSet<GrantedAuthority>();
    for ( LdapAuthoritiesPopulator populator : populators ) {
      Collection<? extends GrantedAuthority> auths = populator.getGrantedAuthorities( userData, username );
      if ( ( null != auths ) && ( auths.size() > 0 ) ) {
        allAuthorities.addAll( auths );
      }

    }
    return allAuthorities;
  }

  public void setPopulators( final Set<LdapAuthoritiesPopulator> populators ) {
    this.populators = populators;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( populators, Messages.getInstance().getString(
      "UnionizingLdapAuthoritiesPopulator.ERROR_0001_POPULATOR_NULL" ) ); //$NON-NLS-1$
  }

}
