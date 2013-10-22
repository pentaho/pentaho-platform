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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class RepositoryAccessVoterManager implements IRepositoryAccessVoterManager {

  private IAuthorizationPolicy authorizationPolicy;
  private String repositoryAdminUsername;
  private List<IRepositoryAccessVoter> voters;

  public RepositoryAccessVoterManager( final IAuthorizationPolicy authorizationPolicy,
      final String repositoryAdminUsername ) {
    super();
    this.authorizationPolicy = authorizationPolicy;
    this.repositoryAdminUsername = repositoryAdminUsername;
  }

  public RepositoryAccessVoterManager( final List<IRepositoryAccessVoter> voters,
      final IAuthorizationPolicy authorizationPolicy, final String repositoryAdminUsername ) {
    this( authorizationPolicy, repositoryAdminUsername );
    Assert.notNull( voters );
    this.voters = new ArrayList<IRepositoryAccessVoter>();
    this.voters.addAll( voters );
  }

  public void registerVoter( IRepositoryAccessVoter voter ) {
    voters.add( voter );
  }

  @Override
  public boolean hasAccess( RepositoryFile file, RepositoryFilePermission operation,
      RepositoryFileAcl repositoryFileAcl, IPentahoSession session ) {
    if ( voters != null && !authorizationPolicy.isAllowed( AdministerSecurityAction.NAME )
        && ( session.getName() != null && !session.getName().equals( repositoryAdminUsername ) ) ) {
      for ( IRepositoryAccessVoter voter : voters ) {
        if ( !voter.hasAccess( file, operation, repositoryFileAcl, session ) ) {
          return false;
        }
      }
    }
    return true;
  }

}
