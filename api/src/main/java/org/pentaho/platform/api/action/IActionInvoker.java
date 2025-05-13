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


package org.pentaho.platform.api.action;

import java.io.Serializable;
import java.util.Map;

/**
 * The purpose of this interface is to provide functionality needed to invoke an {@link IAction} instance in a
 * generic fashion.
 */
public interface IActionInvoker {

  /**
   * Invokes the {@link IAction} {@code action}.
   *
   * @param action The {@link IAction} to be invoked
   * @param user   The user invoking the action
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception if the action cannot be run for some reason
   */
  IActionInvokeStatus invokeAction( IAction action, final String user, final Map<String, Object> params )
        throws Exception;

  /**
   * Predicate that tells whether an {@link IActionInvoker} can handle a given {@link IAction}
   *
   * @param action The {@link IAction} to be handled
   * @return true if the {@link IActionInvoker} can handle a given {@link IAction}
   */
  boolean isSupportedAction( IAction action );
}
