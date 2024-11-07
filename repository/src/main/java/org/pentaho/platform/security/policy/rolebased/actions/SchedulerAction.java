/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.security.policy.rolebased.actions;

import java.util.ResourceBundle;

/**
 * User: nbaker Date: 3/19/13
 */
public class SchedulerAction extends AbstractAuthorizationAction {
  public static final String NAME = "org.pentaho.scheduler.manage";
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
