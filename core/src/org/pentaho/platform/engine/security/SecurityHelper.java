/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.security;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.UserSession;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * A utility class with several static methods that are used to
 * either bind the <tt>Authentication</tt> to the <tt>IPentahoSession</tt>, retrieve
 * the <tt>Authentication</tt> from the <tt>IPentahoSession</tt>, and other various helper
 * functions.
 * @author mbatchel
 *
 */

public class SecurityHelper {

  private static final Log logger = LogFactory.getLog(SecurityHelper.class);

  /**
   * Hi-jacks the system for the named user.
   * 
   * <p>This will essentially create a session for this user, 
   * make that session the current session, and add the Authentication objects to the session
   * and Spring context holder.  WARNING: this method is irreversible!!!  If you want execute
   * a block of code as a surrogate user and have the orignal user resume after it is complete,
   * you want {@link #runAsUser(String, Callable)}.</p>
   * 
   * <p>This is for unit tests only.</p>
   * 
   * @param principalName the user to become in the system
   */
  public static void becomeUser(final String principalName) {
    becomeUser(principalName, null);
  }
  
  /**
   * Hi-jacks the system for the named user.
   * 
   * <p>This is for unit tests only.</p>
   */
  public static void becomeUser(final String principalName, final IParameterProvider paramProvider) {
    UserSession session = new UserSession(principalName, null, false, paramProvider);
    session.setAuthenticated(principalName);
    Authentication auth = SecurityHelper.createAuthentication(principalName);
    PentahoSessionHolder.setSession(session);
    SecurityContextHolder.getContext().setAuthentication(auth);
    PentahoSystem.sessionStartup(PentahoSessionHolder.getSession(), paramProvider);
  }
  
  /**
   * Utility method that allows you to run a block of code as the given user.  
   * Regardless of success or exception situation, the original session and 
   * authentication will be restored once your block of code is finished executing,
   * i.e. the given user will apply only to your {@link Callable}, then the 
   * system environment will return to the user present prior to you calling
   * this method. 
   * @param <T>  the return type of your operation, specify this type as <code>T</code>
   * @param principalName  the user under whom you wish to run a section of code
   * @param callable  {@link Callable#call()} contains the code you wish to run as the given user
   * @return the value returned by your implementation of {@link Callable#call()}
   * @throws Exception
   * @see {@link Callable}
   */
  public static <T> T runAsUser(final String principalName, final Callable<T> callable) throws Exception {
    return runAsUser(principalName, null, callable);
  }
  
  public static <T> T runAsUser(final String principalName, final IParameterProvider paramProvider, final Callable<T> callable) throws Exception {
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      SecurityHelper.becomeUser(principalName);
      return callable.call();
    } finally {
      IPentahoSession sessionToDestroy = PentahoSessionHolder.getSession();
      if (sessionToDestroy != null) {
        try {
          sessionToDestroy.destroy();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      PentahoSessionHolder.setSession(origSession);
      SecurityContextHolder.getContext().setAuthentication(origAuth);
    }
  }

  /**
   * Utility method that allows you to run a block of code as the given user.  
   * Regardless of success or exception situation, the original session and 
   * authentication will be restored once your block of code is finished executing,
   * i.e. the given user will apply only to your {@link Callable}, then the 
   * system environment will return to the user present prior to you calling
   * this method. 
   * @param <T>  the return type of your operation, specify this type as <code>T</code>
   * @param principalName  the user under whom you wish to run a section of code
   * @param callable  {@link Callable#call()} contains the code you wish to run as the given user
   * @return the value returned by your implementation of {@link Callable#call()}
   * @throws Exception
   * @see {@link Callable}
   */
  public static <T> T runAsUnauthenticated(final Callable<T> callable) throws Exception {
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      PentahoSessionHolder.setSession(new StandaloneSession());
      SecurityContextHolder.clearContext();
      return callable.call();
    } finally {
      PentahoSessionHolder.setSession(origSession);
      SecurityContextHolder.getContext().setAuthentication(origAuth);
    }
  }
  
  /**
   * Utility method that communicates with the installed ACLVoter to determine
   * administrator status
   * @param session The users IPentahoSession object
   * @return true if the user is considered a Pentaho administrator
   */
  public static boolean isPentahoAdministrator(final IPentahoSession session) {
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    return voter.isPentahoAdministrator(session);
  }

  /**
   * Utility method that communicates with the installed ACLVoter to determine
   * whether a particular role is granted to the specified user.
   * @param session The users' IPentahoSession
   * @param role The role to look for
   * @return true if the user is granted the specified role.
   */
  public static boolean isGranted(final IPentahoSession session, final GrantedAuthority role) {
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    return voter.isGranted(session, role);
  }

  /**
   * @param aFile
   * @return a boolean that indicates if this file can have ACLS placed on it.
   */
  public static boolean canHaveACLS(final ISolutionFile aFile) {
    if (aFile.isDirectory()) { // All Directories can have ACLS
      return true;
    }

    // Otherwise anything in the PentahoSystem extension list.
    return PentahoSystem.getACLFileExtensionList().contains(aFile.getExtension());
  }

  public static boolean hasAccess(final IAclHolder aHolder, final int actionOperation, final IPentahoSession session) {
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    int aclMask = -1;

    switch (actionOperation) {
      case (IAclHolder.ACCESS_TYPE_READ): {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_WRITE:
      case IAclHolder.ACCESS_TYPE_UPDATE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_DELETE: {
        aclMask = IPentahoAclEntry.PERM_DELETE;
        break;
      }
      case IAclHolder.ACCESS_TYPE_ADMIN: {
        aclMask = IPentahoAclEntry.PERM_ADMINISTRATION;
        break;
      }
      default: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }

    }
    return voter.hasAccess(session, aHolder, aclMask);
  }

  /**
   * Utility method for access negotiation. For performance, not all files will
   * be checked against the supplied voter.
   * @param aFile
   * @param actionOperation
   * @param session
   * @return
   */
  public static boolean hasAccess(final IAclSolutionFile aFile, final int actionOperation, final IPentahoSession session) {
    if (aFile == null) {
      return false;
    }
    if (!aFile.isDirectory()) {
      List extensionList = PentahoSystem.getACLFileExtensionList();
      String fName = aFile.getFileName();
      int posn = fName.lastIndexOf('.');
      if (posn >= 0) {
        if (extensionList.indexOf(fName.substring(posn)) < 0) {
          // Non-acl'd file. Return true.
          return true;
        }
      } else {
        // Untyped file. Allow access.
        return true;
      }
    }
    IAclVoter voter = PentahoSystem.get(IAclVoter.class, session);
    int aclMask = -1;
    switch (actionOperation) {
      case ISolutionRepository.ACTION_EXECUTE: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
      case ISolutionRepository.ACTION_ADMIN: {
        // aclMask = PentahoAclEntry.ADMINISTRATION;
        // break;
        return SecurityHelper.isPentahoAdministrator(session);
      }
      case ISolutionRepository.ACTION_SUBSCRIBE: {
        aclMask = IPentahoAclEntry.PERM_SUBSCRIBE;
        break;
      }
      case ISolutionRepository.ACTION_CREATE: {
        aclMask = IPentahoAclEntry.PERM_CREATE;
        break;
      }
      case ISolutionRepository.ACTION_UPDATE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE;
        break;
      }
      case ISolutionRepository.ACTION_DELETE: {
        aclMask = IPentahoAclEntry.PERM_DELETE;
        break;
      }
      case ISolutionRepository.ACTION_SHARE: {
        aclMask = IPentahoAclEntry.PERM_UPDATE_PERMS;
        break;
      }
      default: {
        aclMask = IPentahoAclEntry.PERM_EXECUTE;
        break;
      }
    }
    return voter.hasAccess(session, aFile, aclMask);
  }

  /**
   * Utility method for hydrating a Spring Authentication object (Principal) given just a user name.
   * Note: The {@link IUserRoleListService} will be consulted for the roles associated with this 
   * user.
   * @param principalName the subject of this Authentication object
   * @return a Spring Authentication for the given user
   */
  public static Authentication createAuthentication(String principalName) {
    IUserRoleListService roleListService = PentahoSystem.get(IUserRoleListService.class);
    List<String> roles = roleListService.getRolesForUser(principalName);
    if (SecurityHelper.logger.isDebugEnabled()) {
      SecurityHelper.logger.debug("rolesForUser from roleListService:" + roles); //$NON-NLS-1$
    }
    GrantedAuthority[] grantedAuthorities = new GrantedAuthority[roles.size()];
    if (!roles.isEmpty()) {
      for (int i = 0; i < roles.size(); i++) {
        grantedAuthorities[i] = new GrantedAuthorityImpl(roles.get(i));
      }
    }

    Authentication auth = new UsernamePasswordAuthenticationToken(principalName, null, grantedAuthorities);
    return auth;
  }
  
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
  
  /**
   * Remove this method when data-access is JCR-branched
   * @param ignoredSession
   * @param ignoredAllowAnonymous
   * @return
   */
  public static Authentication getAuthentication(IPentahoSession ignoredSession, boolean ignoredAllowAnonymous) {
    return getAuthentication();
  }
  
  /**
   * Runs code as system with full privileges.
   */
  public static <T> T runAsSystem(final Callable<T> callable) throws Exception {
    // TODO Substitute the tennant admin user name using the pattern {0}_adminUser
    // final String name = MessageFormat.format("{0}_adminUser", TenantUtils.getTenantId());
    
    final String name = "joe"; //$NON-NLS-1$
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
    // create pentaho session
    StandaloneSession session = new StandaloneSession(name);
    session.setAuthenticated(name);
    // create authentication
    
    GrantedAuthority[] roles;
    IAclVoter aclVoter = PentahoSystem.get(IAclVoter.class, null);
    if (aclVoter != null) {
      roles = new GrantedAuthority[1];
      roles[0] = aclVoter.getAdminRole();
    } else {
      // silently ignore a missing IAclVoter (access will be denied for lack of roles)
      roles = new GrantedAuthority[0];
    }
    Authentication auth = new UsernamePasswordAuthenticationToken(name, "", roles); //$NON-NLS-1$

    // set holders
    PentahoSessionHolder.setSession(session);
    SecurityContextHolder.getContext().setAuthentication(auth);
      return callable.call();
    } finally {
      IPentahoSession sessionToDestroy = PentahoSessionHolder.getSession();
      if (sessionToDestroy != null) {
        try {
          sessionToDestroy.destroy();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      PentahoSessionHolder.setSession(origSession);
      SecurityContextHolder.getContext().setAuthentication(origAuth);
    }
  }
  
}
