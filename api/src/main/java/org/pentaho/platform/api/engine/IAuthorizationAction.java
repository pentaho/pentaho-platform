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


package org.pentaho.platform.api.engine;

/**
 * 
 * Represents a Logical Role name used by some IAuthorizationPolicy implementations. Also known as Action-Based Security
 * 
 * User: nbaker Date: 3/19/13
 */
public interface IAuthorizationAction {
  /**
   * Get the name of the action
   * 
   * @return action name
   */
  String getName();

  /**
   * Get the localized display name of action for a specific locale. If null is passed then default locale will be used
   * 
   * @param locale
   * @return localized name
   */
  String getLocalizedDisplayName( String locale );
}
