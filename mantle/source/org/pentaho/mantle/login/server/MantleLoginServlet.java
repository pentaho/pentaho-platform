/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.login.server;

import java.util.ArrayList;
import java.util.Collections;

import org.pentaho.mantle.login.client.MantleLoginService;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.AuthenticationTrustResolver;
import org.springframework.security.AuthenticationTrustResolverImpl;
import org.springframework.security.context.SecurityContextHolder;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MantleLoginServlet extends RemoteServiceServlet implements MantleLoginService {

private AuthenticationTrustResolver resolver = new AuthenticationTrustResolverImpl();

  public ArrayList<String> getAllUsers() {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    ArrayList<String> users =  new ArrayList<String>(userRoleListService.getAllUsers());
    Collections.sort(users);
    return users;
  }

  /**
   * Note that this implementation is different from MantleServlet.isAuthenticated. This method must return false if the
   * user is anonymous. That is not the case for MantleServlet.isAuthenticated.
   */
  public boolean isAuthenticated() {
    return !resolver.isAnonymous(SecurityContextHolder.getContext().getAuthentication());
  }

  public boolean isShowUsersList() {
    return "true".equalsIgnoreCase(PentahoSystem.getSystemSetting("login-show-users-list", "true")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
