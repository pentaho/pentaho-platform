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

/**
 * A factory for creating <code>LdapSearchParams</code> instances.
 * 
 * @author mlowery
 * 
 */
public interface LdapSearchParamsFactory {

  /**
   * Create a parameters object with the given arguments. The assumption is that the filter arguments will be the only
   * parameter not known until runtime.
   * 
   * @param filterArgs
   *          arguments that will be merged with the <code>filter</code> property of an <code>LdapSearchParams</code>
   *          instance
   * @return a new parameters object
   */
  LdapSearchParams createParams( Object[] filterArgs );

}
