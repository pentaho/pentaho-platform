package org.pentaho.platform.api.engine;

/**
 *
 * Represents a Logical Role name used by some IAuthorizationPolicy implementations. Also known as Action-Based
 * Security
 *
 * User: nbaker
 * Date: 3/19/13
 */
public interface IAuthorizationAction {
  /**
   * Get the name of the action
   * @return action name
   */
  String getName();
}
