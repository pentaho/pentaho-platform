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

package org.pentaho.platform.api.engine;

/**
 * Represents a Logical Role name used by some IAuthorizationPolicy implementations. Also known as Action-Based Security
 */
public interface IAuthorizationAction {
  /**
   * Get the name of the action
   *
   * @return action name
   */
  String getName();

  /**
   * Get the localized display name of the action for the default locale.
   *
   * @return The localized name
   */
  default String getLocalizedDisplayName() {
    return getLocalizedDisplayName( null );
  }

  /**
   * Get the localized display name of the action for a specific locale.
   * If `locale` is invalid, then the default locale will be used.
   *
   * @param locale The locale to use for localization
   * @return The localized name
   */
  String getLocalizedDisplayName( String locale );

  /**
   * Get the localized description of the action for the default locale.
   *
   * @return The localized description
   */
  default String getLocalizedDescription() {
    return getLocalizedDescription( null );
  }

  /**
   * Get the localized description of the action for a specific locale.
   * The key used is composed by the action name appended with ".description".
   * For example, if the action name is "org.pentaho.action" then
   * the description key will be "org.pentaho.action.description".
   * If `locale` is invalid, then the default locale will be used.
   *
   * @param locale The locale to use for localization
   * @return The localized description
   */
  String getLocalizedDescription( String locale );
}
