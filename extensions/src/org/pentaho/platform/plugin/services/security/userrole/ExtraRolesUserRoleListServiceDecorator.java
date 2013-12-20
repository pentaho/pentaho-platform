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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Decorates another {@link IUserRoleListService} and returns a merged list consisting of the original roles from
 * {@link IUserRoleListService#getAllRoles()} plus the extra roles. Roles are added to the end of the list and only if
 * they don't already exist.
 * 
 * Use with {@code DefaultRoleUserDetailsServiceDecorator}.
 * 
 * @author mlowery
 */
public class ExtraRolesUserRoleListServiceDecorator implements IUserRoleListService {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog( ExtraRolesUserRoleListServiceDecorator.class );

  // ~ Instance fields =================================================================================================

  private IUserRoleListService userRoleListService;

  private List<String> extraRoles;

  // ~ Constructors ====================================================================================================

  public ExtraRolesUserRoleListServiceDecorator() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Override
  public List<String> getAllRoles() {
    return userRoleListService.getAllRoles();
  }

  protected List<String> getNewRoles() {
    List<String> origRoles = userRoleListService.getAllRoles();
    List<String> newRoles1 = new ArrayList<String>( origRoles );
    for ( String extraRole : extraRoles ) {
      if ( !origRoles.contains( extraRole ) ) {
        newRoles1.add( extraRole );
      }
    }
    if ( logger.isDebugEnabled() ) {
      logger.debug( String.format( "original roles: %s, new roles: %s", origRoles, newRoles1 ) ); //$NON-NLS-1$
    }
    return newRoles1;
  }

  @Override
  public List<String> getAllUsers() {
    return userRoleListService.getAllUsers();
  }

  public void setUserRoleListService( final IUserRoleListService userRoleListService ) {
    this.userRoleListService = userRoleListService;
  }

  public void setExtraRoles( final List<String> extraRoles ) {
    Assert.notNull( extraRoles );
    this.extraRoles = new ArrayList<String>( extraRoles );
  }

  public void setSystemRoles( final Set<String> systemRoles ) {
    Assert.notNull( systemRoles );
  }

  @Override
  public List<String> getAllRoles( ITenant tenant ) {
    return  userRoleListService.getAllRoles( tenant );
  }

  @Override
  public List<String> getAllUsers( ITenant tenant ) {
    return userRoleListService.getAllUsers();
  }

  @Override
  public List<String> getUsersInRole( ITenant tenant, String role ) {
    return userRoleListService.getUsersInRole( tenant, role );
  }

  @Override
  public List<String> getRolesForUser( ITenant tenant, String username ) {
    return userRoleListService.getRolesForUser( tenant, username );
  }

  @Override
  public List<String> getSystemRoles() {
    return userRoleListService.getSystemRoles();
  }
}
