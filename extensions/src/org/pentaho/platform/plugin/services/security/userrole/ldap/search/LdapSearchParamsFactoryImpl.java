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

import javax.naming.directory.SearchControls;

public class LdapSearchParamsFactoryImpl implements LdapSearchParamsFactory, InitializingBean {

  // ~ Static fields/initializers ============================================

  // private static final Log logger = LogFactory.getLog(LdapSearchParamsFactoryImpl.class);

  // ~ Instance fields =======================================================

  private String base;

  private String filter;

  private SearchControls searchControls;

  // ~ Constructors ==========================================================

  public LdapSearchParamsFactoryImpl() {
    super();
  }

  // ~ Methods ===============================================================

  public LdapSearchParamsFactoryImpl( final String base, final String filter ) {
    this( base, filter, new SearchControls() );
  }

  public LdapSearchParamsFactoryImpl( final String base, final String filter, final SearchControls searchControls ) {
    this.base = base;
    this.filter = filter;
    this.searchControls = searchControls;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( base );
    Assert.hasLength( filter );
  }

  /**
   * Private so it cannot be instantiated except by this class.
   */
  private class LdapSearchParamsImpl implements LdapSearchParams {
    private String implBase;

    private String implFilter;

    private Object[] filterArgs;

    private SearchControls implSearchControls;

    private LdapSearchParamsImpl( final String base, final String filter, final Object[] filterArgs,
        final SearchControls searchControls ) {
      this.implBase = base;
      this.implFilter = filter;
      this.filterArgs = filterArgs;
      this.implSearchControls = searchControls;
    }

    public String getBase() {
      return implBase;
    }

    public String getFilter() {
      return implFilter;
    }

    public Object[] getFilterArgs() {
      return filterArgs;
    }

    public SearchControls getSearchControls() {
      return implSearchControls;
    }

  }

  public LdapSearchParams createParams( final Object[] filterArgs ) {
    return new LdapSearchParamsImpl( base, filter, filterArgs, searchControls );
  }

}
