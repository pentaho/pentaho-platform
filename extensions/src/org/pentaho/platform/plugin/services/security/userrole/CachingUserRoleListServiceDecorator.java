package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Collections;
import java.util.List;

/**
 * Caching Decorator for an IUserRoleListService. It will use the configured ICacheManager in the PentahoSystem to cache
 * all calls being made to the decorated instance.
 * <p/>
 * Created by nbaker on 5/20/14.
 */
public class CachingUserRoleListServiceDecorator implements IUserRoleListService {
  private IUserRoleListService delegate;
  private ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
  private static final String REGION = "userRoleListCache";
  private static final String ALL_USERS = "all users";
  private static final String ALL_ROLES = "all roles";
  private static final String SYSTEM_ROLES = "system roles";
  private static final String ROLES_BY_USER = "roles by user";

  private static interface DelegateOperation {
    List<String> perform();
  }

  private final DelegateOperation ALL_ROLES_OPERATION = new DelegateOperation() {
    @Override public List<String> perform() {
      return delegate.getAllRoles();
    }
  };

  private final DelegateOperation SYSTEM_ROLES_OPERATION = new DelegateOperation() {
    @Override public List<String> perform() {
      return delegate.getSystemRoles();
    }
  };
  private final DelegateOperation ALL_USERS_OPERATION = new DelegateOperation() {
    @Override public List<String> perform() {
      return delegate.getAllUsers();
    }
  };


  public CachingUserRoleListServiceDecorator( IUserRoleListService delegate ) {
    if ( delegate == null ) {
      throw new IllegalArgumentException( "Decorated IUserRoleListService cannot be null" );
    }
    this.delegate = delegate;
    if ( !this.cacheManager.cacheEnabled( REGION ) ) {
      this.cacheManager.addCacheRegion( REGION );
    }
  }

  @SuppressWarnings( "unchecked" )
  private List<String> performOperation( String cacheEntry, DelegateOperation operation ) {
    Object fromRegionCache = cacheManager.getFromRegionCache( REGION, cacheEntry );
    if ( fromRegionCache != null && fromRegionCache instanceof List ) {
      return (List<String>) fromRegionCache;
    }
    List<String> results = operation.perform();
    cacheManager.putInRegionCache( REGION, cacheEntry, results );
    return Collections.unmodifiableList( results );
  }


  @Override
  public List<String> getAllRoles() {
    return performOperation( ALL_ROLES, ALL_ROLES_OPERATION );
  }

  @Override
  public List<String> getSystemRoles() {
    return performOperation( SYSTEM_ROLES, SYSTEM_ROLES_OPERATION );
  }

  @Override
  public List<String> getRolesForUser( final ITenant tenant, final String username ) {
    return performOperation( ROLES_BY_USER + getTenantKey( tenant ) + username,
      new DelegateOperation() {
        @Override public List<String> perform() {
          return delegate.getRolesForUser( tenant, username );
        }
      }
    );
  }

  private String getTenantKey( ITenant tenant ) {
    return ( ( tenant != null ) ? tenant.getId() : "_" );
  }

  @Override
  public List<String> getAllUsers( final ITenant tenant ) {
    return performOperation( ALL_USERS + getTenantKey( tenant ), new DelegateOperation() {
      @Override public List<String> perform() {
        return delegate.getAllUsers( tenant );
      }
    } );
  }

  @Override
  public List<String> getAllRoles( final ITenant tenant ) {
    return performOperation( ALL_ROLES + getTenantKey( tenant ), new DelegateOperation() {
      @Override public List<String> perform() {
        return delegate.getAllRoles( tenant );
      }
    } );
  }

  @Override
  public List<String> getUsersInRole( final ITenant tenant, final String role ) {
    return performOperation( ALL_USERS + getTenantKey( tenant ) + role, new DelegateOperation() {
      @Override public List<String> perform() {
        return delegate.getUsersInRole( tenant, role );
      }
    } );
  }

  @Override
  public List<String> getAllUsers() {

    return performOperation( ALL_USERS, ALL_USERS_OPERATION );
  }
}
