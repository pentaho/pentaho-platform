/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.concurrent.Callable;

/**
 * Interface for a utility class with several methods that are used to either bind the <tt>Authentication</tt> to
 * the <tt>IPentahoSession</tt>, retrieve the <tt>Authentication</tt> from the <tt>IPentahoSession</tt>, and other
 * various helper functions.
 * 
 * @author mbatchel
 */
public interface ISecurityHelper {
  /**
   * Hi-jacks the system for the named user.
   * 
   * <p>
   * This will essentially create a session for this user, make that session the current session, and add the
   * Authentication objects to the session and Spring context holder. WARNING: this method is irreversible!!! If
   * you want execute a block of code as a surrogate user and have the orignal user resume after it is complete,
   * you want {@link #runAsUser(String, java.util.concurrent.Callable)}.
   * </p>
   * 
   * <p>
   * This is for unit tests only.
   * </p>
   * 
   * @param principalName
   *          the user to become in the system
   */
  void becomeUser( String principalName );

  /**
   * Hi-jacks the system for the named user.
   * 
   * <p>
   * This is for unit tests only.
   * </p>
   */
  void becomeUser( String principalName, IParameterProvider paramProvider );

  /**
   * Utility method that allows you to run a block of code as the given user. Regardless of success or exception
   * situation, the original session and authentication will be restored once your block of code is finished
   * executing, i.e. the given user will apply only to your {@link java.util.concurrent.Callable}, then the system
   * environment will return to the user present prior to you calling this method.
   * 
   * @param <T>
   *          the return type of your operation, specify this type as <code>T</code>
   * @param principalName
   *          the user under whom you wish to run a section of code
   * @param callable
   *          {@link java.util.concurrent.Callable#call()} contains the code you wish to run as the given user
   * @return the value returned by your implementation of {@link java.util.concurrent.Callable#call()}
   * @throws Exception
   * @see {@link java.util.concurrent.Callable}
   */
  <T> T runAsUser( String principalName, Callable<T> callable ) throws Exception;

  <T> T runAsUser( String principalName, IParameterProvider paramProvider, Callable<T> callable ) throws Exception;

  /**
   * Utility method that allows you to run a block of code as the given user. Regardless of success or exception
   * situation, the original session and authentication will be restored once your block of code is finished
   * executing, i.e. the given user will apply only to your {@link java.util.concurrent.Callable}, then the system
   * environment will return to the user present prior to you calling this method.
   * 
   * @param <T>
   *          the return type of your operation, specify this type as <code>T</code>
   * @param principalName
   *          the user under whom you wish to run a section of code
   * @param callable
   *          {@link java.util.concurrent.Callable#call()} contains the code you wish to run as the given user
   * @return the value returned by your implementation of {@link java.util.concurrent.Callable#call()}
   * @throws Exception
   * @see {@link java.util.concurrent.Callable}
   */
  <T> T runAsAnonymous( Callable<T> callable ) throws Exception;

  /**
   * Utility method that communicates with the installed ACLVoter to determine administrator status
   * @deprecated use SystemUtils.canAdminister() instead
   * @param session
   *          The users IPentahoSession object
   * @return true if the user is considered a Pentaho administrator
   */
  @Deprecated
  boolean isPentahoAdministrator( IPentahoSession session );

  /**
   * Utility method that communicates with the installed ACLVoter to determine whether a particular role is granted
   * to the specified user.
   *
   * @param session
   *          The users' IPentahoSession
   * @param role
   *          The role to look for
   * @return true if the user is granted the specified role.
   */
  boolean isGranted( IPentahoSession session, GrantedAuthority role );

  @Deprecated
  boolean hasAccess( IAclHolder aHolder, int actionOperation, IPentahoSession session );

  /**
   * Utility method for hydrating a Spring Authentication object (Principal) given just a user name. Note: The
   * {@link org.pentaho.platform.api.engine.IUserRoleListService} will be consulted for the roles associated with
   * this user.
   * 
   * @param principalName
   *          the subject of this Authentication object
   * @return a Spring Authentication for the given user
   */
  Authentication createAuthentication( String principalName );

  Authentication getAuthentication();

  /**
   * Remove this method when data-access is JCR-branched
   * 
   * @param ignoredSession
   * @param ignoredAllowAnonymous
   * @return
   */
  Authentication getAuthentication( IPentahoSession ignoredSession, boolean ignoredAllowAnonymous );

  /**
   * Runs code as system with full privileges.
   */
  <T> T runAsSystem( Callable<T> callable ) throws Exception;
}
