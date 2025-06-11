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

package org.pentaho.platform.security.policy.rolebased.actions;

import java.util.Locale;
import java.util.ResourceBundle;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * Abstract base class for authorization actions. This class provides
 * convenience methods for localization and resource bundle management.
 */
public abstract class AbstractAuthorizationAction implements IAuthorizationAction {

  protected ResourceBundle getResourceBundle( String localeString ) {
    return getResourceBundle( parseLocale( localeString ) );
  }

  protected ResourceBundle getResourceBundle( Locale locale ) {
    if ( locale == null ) {
      return Messages.getInstance().getBundle( LocaleHelper.getLocale() );
    }

    return Messages.getInstance().getBundle( locale );
  }

  protected Locale parseLocale( String localeString ) {
    final String UNDERSCORE = "_";

    if ( StringUtil.isEmpty( localeString ) ) {
      return LocaleHelper.getLocale();
    }

    String[] tokens = localeString.split( UNDERSCORE );
    if ( tokens.length == 3 ) {
      return new Locale( tokens[ 0 ], tokens[ 1 ], tokens[ 2 ] );
    } else if ( tokens.length == 2 ) {
      return new Locale( tokens[ 0 ], tokens[ 1 ] );
    } else {
      return new Locale( tokens[ 0 ] );
    }
  }

  /**
   * {@inheritDoc}
   */
  public String getLocalizedDisplayName( String localeString ) {
    return getResourceBundle( localeString ).getString( getName() );
  }

  /**
   * {@inheritDoc}
   */
  public String getLocalizedDescription( String localeString ) {
    return getResourceBundle( localeString ).getString( getName() + ".description" );
  }
}
