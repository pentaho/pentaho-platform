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
    Assert.notNull( base, "Base must not be null" );
    Assert.hasLength( filter, "Filter must not be null or empty" );
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
