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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class delgates calls to a configured list of IUserRoleListService delegates. The results are determined by the
 * STRATEGY in effect, either returning the results from the first delegate returning a non-empty value
 * (STRATEGY.FIRST_MATCH), or combining the results from all delegates together in an unique list (STRATEGY.ADDITIVE)
 * <p/>
 * Created by nbaker on 5/13/14.
 */
public class CompositeUserRoleListService implements IUserRoleListService {

  private List<IUserRoleListService> delegates = new ArrayList<IUserRoleListService>();

  public static enum STRATEGY {
    ADDITIVE,
    FIRST_MATCH
  }

  private STRATEGY activeStrategy = STRATEGY.FIRST_MATCH;

  public CompositeUserRoleListService( List<IUserRoleListService> delegates ) {
    this.delegates.addAll( delegates );
  }

  public void setStrategy( STRATEGY strategy ) {
    this.activeStrategy = strategy;
  }

  public void setStrategy( String strategy ) {
    setStrategy( STRATEGY.valueOf( strategy ) );
  }

  public STRATEGY getActiveStrategy() {
    return activeStrategy;
  }

  @Override public List<String> getAllRoles() {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getAllRoles();
      }
    } );
  }

  @Override public List<String> getSystemRoles() {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getSystemRoles();
      }
    } );
  }

  @Override public List<String> getAllRoles( final ITenant tenant ) {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getAllRoles( tenant );
      }
    } );
  }

  @Override public List<String> getAllUsers() {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getAllUsers();
      }
    } );
  }

  @Override public List<String> getAllUsers( final ITenant tenant ) {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getAllUsers( tenant );
      }
    } );
  }

  @Override public List<String> getUsersInRole( final ITenant tenant, final String role ) {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getUsersInRole( tenant, role );
      }
    } );
  }

  @Override public List<String> getRolesForUser( final ITenant tenant, final String username ) {
    return collectResultsForOperation( new CompositeOperation() {
      @Override public List<String> perform( IUserRoleListService service ) {
        return service.getRolesForUser( tenant, username );
      }
    } );
  }

  private List<String> collectResultsForOperation( CompositeOperation operation ) {
    Set<String> returnVal = new HashSet<String>();
    for ( IUserRoleListService delegate : delegates ) {
      List<String> allFromDelegate;
      try {
        allFromDelegate = operation.perform( delegate );
      } catch ( UnsupportedOperationException ignored ) {
        continue;
      }
      if ( allFromDelegate != null && allFromDelegate.size() > 0 ) {
        returnVal.addAll( allFromDelegate );
        if ( activeStrategy == STRATEGY.FIRST_MATCH ) {
          return new ArrayList<String>( returnVal );
        }
      }
    }
    return new ArrayList<String>( returnVal );
  }

  private interface CompositeOperation {
    List<String> perform( IUserRoleListService service );
  }
}
