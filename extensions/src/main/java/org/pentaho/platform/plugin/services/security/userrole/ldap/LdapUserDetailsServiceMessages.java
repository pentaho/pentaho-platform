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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LdapUserDetailsServiceMessages {
  private static final String BUNDLE_NAME =
      "org.pentaho.platform.engine.security.messages.ldapuserdetailsservice_messages"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle( LdapUserDetailsServiceMessages.BUNDLE_NAME );

  private LdapUserDetailsServiceMessages() {
  }

  public static String getString( final String key ) {
    try {
      return LdapUserDetailsServiceMessages.RESOURCE_BUNDLE.getString( key );
    } catch ( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }
}
