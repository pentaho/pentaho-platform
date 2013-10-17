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
    Assert.notEmpty( searches );
  }

}
