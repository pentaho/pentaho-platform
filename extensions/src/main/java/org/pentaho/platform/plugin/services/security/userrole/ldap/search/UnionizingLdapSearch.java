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


package org.pentaho.platform.plugin.services.security.userrole.ldap.search;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Iterates over <code>LdapSearch</code> instances in <code>searches</code> and unions the results. Use in conjunction
 * with {@code UnionizingLdapAuthoritiesPopulator}.
 * 
 * @author mlowery
 */
public class UnionizingLdapSearch implements LdapSearch, InitializingBean {
  // ~ Static fields/initializers ============================================

  // private static final Log logger = LogFactory.getLog(UnionizingLdapSearch.class);

  // ~ Instance fields =======================================================

  private Set searches;

  // ~ Constructors ==========================================================

  public UnionizingLdapSearch() {
    super();
  }

  public UnionizingLdapSearch( final Set searches ) {
    this.searches = searches;
  }

  // ~ Methods ===============================================================

  public List search( final Object[] filterArgs ) {
    Set results = new HashSet();
    Iterator iter = searches.iterator();
    while ( iter.hasNext() ) {
      results.addAll( ( (LdapSearch) iter.next() ).search( filterArgs ) );
    }
    return new ArrayList( results );
  }

  public void setSearches( final Set searches ) {
    this.searches = searches;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notEmpty( searches, "Searches set must not be empty" );
  }

}
