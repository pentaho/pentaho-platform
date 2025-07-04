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

import java.util.List;

/**
 * Executes a search against a directory context using the given filter arguments plus other static search parameters
 * (in the form of instance variables) known at deploy time.
 * 
 * <p>
 * Can also be seen as a generalization of <code>org.springframework.security.ldap.LdapUserSearch</code>.
 * </p>
 * 
 * @author mlowery
 * @see javax.naming.directory.DirContext.search()
 */
public interface LdapSearch {

  /**
   * Executes a search against a directory context using the given filter arguments.
   * 
   * @param filterArgs
   *          the filter arguments
   * @return the result set as a list
   */
  List search( Object[] filterArgs );
}
