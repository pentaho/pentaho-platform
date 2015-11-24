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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.security.policy.rolebased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAggregatingAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.util.Assert;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * An authorization policy based on roles.
 * 
 * @author mlowery
 */
public class RoleAuthorizationPolicy implements IAuthorizationPolicy, ISessionAwareAuthorizationPolicy {

  private static final Log logger = LogFactory.getLog( RoleAuthorizationPolicy.class );

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

  private final ListMultimap<String, String> aggregateActionsMapping;

  // ~ Constructors
  // ====================================================================================================

  public RoleAuthorizationPolicy( final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao ) {
    super();
    Assert.notNull( roleBindingDao );
    this.roleBindingDao = roleBindingDao;
    this.aggregateActionsMapping = buildActionsMapping( PentahoSystem.getAll( IAuthorizationAction.class ) );
  }

  // package-local visibility for testing purposes
  ListMultimap<String, String> buildActionsMapping( List<IAuthorizationAction> actions ) {
    SetMultimap<String, String> multimap = HashMultimap.create( actions.size(), 3 );

    List<IAggregatingAuthorizationAction> unprocessed = new LinkedList<>();
    for ( IAuthorizationAction action : actions ) {
      if ( action instanceof IAggregatingAuthorizationAction ) {
        unprocessed.add( (IAggregatingAuthorizationAction) action );
      } else {
        multimap.put( action.getName(), action.getName() );
      }
    }

    while ( !unprocessed.isEmpty() ) {
      int counter = 0;
      for ( Iterator<IAggregatingAuthorizationAction> iterator = unprocessed.iterator(); iterator.hasNext(); ) {
        IAggregatingAuthorizationAction action = iterator.next();

        List<String> aggregated = action.getAggregatedActions();
        List<String> collector = new ArrayList<>();
        boolean processedAll = true;
        for ( String aggregatedAction : aggregated ) {
          if ( multimap.containsKey( aggregatedAction ) ) {
            collector.addAll( multimap.get( aggregatedAction ) );
          } else {
            processedAll = false;
          }
        }

        if ( processedAll ) {
          iterator.remove();

          multimap.putAll( action.getName(), collector );
          multimap.put( action.getName(), action.getName() );

          counter++;
        }
      }

      if ( counter == 0 ) {
        StringBuilder sb = new StringBuilder();
        for ( IAggregatingAuthorizationAction action : unprocessed ) {
          sb.append( action.getName() ).append( " --> " ).append( action.getAggregatedActions() ).append( '\n' );
        }
        throw new IllegalStateException( "Found circular dependency among:\n" + sb.toString() );
      }
    }

    return ImmutableListMultimap.<String, String>builder().putAll( multimap ).build();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public List<String> getAllowedActions( String actionNamespace ) {
    Set<String> roleNames = includeAggregates( roleBindingDao.getBoundLogicalRoleNames( getRuntimeRoleNames() ) );
    if ( actionNamespace == null ) {
      return new ArrayList<>( roleNames );
    } else {
      if ( !actionNamespace.endsWith( "." ) ) {
        actionNamespace += ".";
      }
      List<String> assignedRolesInNamespace = new ArrayList<String>( roleNames.size() );
      for ( String assignedRole : roleNames ) {
        if ( assignedRole.startsWith( actionNamespace ) ) {
          assignedRolesInNamespace.add( assignedRole );
        }
      }
      return assignedRolesInNamespace;
    }
  }

  private Set<String> includeAggregates( List<String> actions ) {
    if ( actions == null || actions.isEmpty() ) {
      return Collections.emptySet();
    }
    Set<String> result = new HashSet<>( actions );
    for ( String action : actions ) {
      result.addAll( aggregateActionsMapping.get( action ) );
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAllowed( String actionName ) {
    return includeAggregates( roleBindingDao.getBoundLogicalRoleNames( getRuntimeRoleNames() ) )
      .contains( actionName );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAllowed( Session session, String actionName ) {
    List<String> boundLogicalRoleNames;
    try {
      boundLogicalRoleNames = roleBindingDao.getBoundLogicalRoleNames( session, getRuntimeRoleNames() );
    } catch ( RepositoryException e ) {
      logger.error( e );
      return false;
    }
    return includeAggregates( boundLogicalRoleNames ).contains( actionName );
  }

  protected List<String> getRuntimeRoleNames() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Assert.state( authentication != null );
    GrantedAuthority[] authorities = authentication.getAuthorities();
    List<String> runtimeRoles = new ArrayList<String>( authorities.length );
    for ( GrantedAuthority authority : authorities ) {
      runtimeRoles.add( authority.getAuthority() );
    }
    return runtimeRoles;
  }

}
