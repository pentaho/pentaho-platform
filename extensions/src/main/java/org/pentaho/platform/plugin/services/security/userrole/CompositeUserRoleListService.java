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

package org.pentaho.platform.plugin.services.security.userrole;

import org.apache.commons.collections.CollectionUtils;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static Logger logger = LoggerFactory.getLogger( CompositeUserRoleListService.class );

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

  @Override
  public List<String> getAllUsers() {
    return collectResultsForOperation( new CompositeOperation() {
      @Override
      public List<String> perform( IUserRoleListService service ) {
        return service.getAllUsers();
      }
    } );
  }

  @Override
  public List<String> getAllUsers( final ITenant tenant ) {
    return collectResultsForOperation( new CompositeOperation() {
      @Override
      public List<String> perform( IUserRoleListService service ) {
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
      try {
        List<String> allFromDelegate = operation.perform( delegate );
        if ( !CollectionUtils.isEmpty( allFromDelegate ) ) {
          returnVal.addAll( allFromDelegate );
          if ( activeStrategy == STRATEGY.FIRST_MATCH ) {
            break;
          }
        }
      } catch ( UnsupportedOperationException ignored ) {
        // next delegate
      } catch ( Exception e ) {
        //Log the exception if the method was supported
        logger.error( "User/Role List could not be obtained.", e );
      }
    }
    return new ArrayList<String>( returnVal );
  }

  private interface CompositeOperation {
    List<String> perform( IUserRoleListService service );
  }
}
