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

import javax.naming.directory.SearchControls;

/**
 * Wrapper to encapsulate parameters of the <code>search</code> method of <code>javax.naming.directory.DirContext</code>
 * instances.
 * 
 * @author mlowery
 */
public interface LdapSearchParams {

  String getBase();

  String getFilter();

  Object[] getFilterArgs();

  SearchControls getSearchControls();

}
