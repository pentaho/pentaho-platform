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

import java.util.ResourceBundle;

/**
 * User: nbaker Date: 3/30/13
 */
public class PublishAction extends AbstractAuthorizationAction {
  public static final String NAME = "org.pentaho.security.publish";
  ResourceBundle resourceBundle;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getLocalizedDisplayName( String localeString ) {
    resourceBundle = getResourceBundle( localeString );
    return resourceBundle.getString( NAME );
  }
}
