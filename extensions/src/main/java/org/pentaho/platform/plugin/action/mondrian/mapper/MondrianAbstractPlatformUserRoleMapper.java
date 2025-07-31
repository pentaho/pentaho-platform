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


package org.pentaho.platform.plugin.action.mondrian.mapper;

import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.rolap.RolapConnectionProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author mbatchelor
 */
public abstract class MondrianAbstractPlatformUserRoleMapper implements IConnectionUserRoleMapper {

  private static final Log log = LogFactory.getLog( MondrianAbstractPlatformUserRoleMapper.class );

  public MondrianAbstractPlatformUserRoleMapper() {

  }

  /**
   * Subclasses simply need to implement this one method to do the specific mapping desired.
   * 
   * @param mondrianRoles
   *          Sorted list of roles defined in the catalog
   * @param platformRoles
   *          Sorted list of the roles defined in the catalog
   * @return
   *          The list of roles.
   */
  protected abstract String[] mapRoles( String[] mondrianRoles, String[] platformRoles )
    throws PentahoAccessControlException;

  /**
   * This method returns the role names as found in the Mondrian schema. The returned names must be ordered (sorted) or
   * code down-stream will not work.
   * 
   * @param userSession
   *          Users' session
   * @param catalogName
   *          The name of the catalog
   * @return Array of role names from the schema file
   */
  protected String[] getMondrianRolesFromCatalog( IPentahoSession userSession, String catalogName ) {
    String[] rtn = null;
    // Get the catalog service
    IMondrianCatalogService catalogService = PentahoSystem.get( IMondrianCatalogService.class );
    if ( catalogService != null ) {
      // Get the catalog by name
      MondrianCatalog catalog = catalogService.getCatalog( catalogName, userSession );
      if ( catalog != null ) {
        // The roles are in the schema object
        MondrianSchema schema = catalog.getSchema();
        if ( schema != null ) {
          // Ask the schema for the role names array
          String[] roleNames = schema.getRoleNames();
          if ( ( roleNames != null ) && ( roleNames.length > 0 ) ) {
            // Return the roles from the schema
            Arrays.sort( roleNames );
            return roleNames;
          }
        }
      }
    }

    // Check with the IOlapService and try to get a list of roles there.
    IOlapService olapService = PentahoSystem.get( IOlapService.class );
    if ( olapService != null ) {
      MondrianCatalogRepositoryHelper helper =
          new MondrianCatalogRepositoryHelper( PentahoSystem.get( IUnifiedRepository.class ) );
      String serverName = null;
      for ( String name : helper.getOlap4jServers() ) {
        PropertyList props = Util.parseConnectString( helper.getOlap4jServerInfo( name ).URL );
        if ( props.get( RolapConnectionProperties.Catalog.name(), "" ).equals( catalogName ) ) {
          serverName = name;
        }
      }
      if ( serverName != null ) {
        OlapConnection conn = null;
        try {
          // Use a null session for root access.
          conn = olapService.getConnection( serverName, null );
          List<String> roleList = conn.getAvailableRoleNames();
          String[] roleArray = roleList.toArray( new String[roleList.size()] );
          Arrays.sort( roleArray );
          return roleArray;
        } catch ( OlapException e ) {
          log.error( "Failed to get a list of roles from olap connection " + catalogName, e );
          throw new RuntimeException( e );
        } finally {
          if ( conn != null ) {
            try {
              conn.close();
            } catch ( SQLException e ) {
              // OK to squash this one.
              log.error( "Failed to get a list of roles from olap connection " + catalogName, e );
            }
          }
        }
      }
    }

    // Sort the returned list of roles.
    return rtn;
  }

  /**
   * This method returns the users' roles as specified in the Spring Security authentication object. The role names
   * returned must be sorted for other code downstream to work properly.
   * 
   * @param session
   *          The users' session
   * @return Users' roles as defined in the authentication object
   */
  protected String[] getPlatformRolesFromSession( IPentahoSession session ) {
    // Get the authorities
    Collection<? extends GrantedAuthority> gAuths = (Collection) session.getAttribute( IPentahoSession.SESSION_ROLES );
    if ( gAuths == null ) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      gAuths = authentication.getAuthorities();
      Assert.state( authentication != null, "Authentication object must not be null" );
    }

    List<String> rtn = null;
    if ( ( gAuths != null ) && ( !gAuths.isEmpty() ) ) {
      // Copy role names out of the Authentication
      rtn = new ArrayList<>();
      for ( GrantedAuthority auth : gAuths ) {
        rtn.add( auth.getAuthority() );
      }
      // Sort the returned list of roles
      Collections.sort( rtn );
    }
    return rtn != null ? rtn.toArray( new String[]{} ) : null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IConnectionUserRoleMapper#mapConnectionRoles(org.pentaho.platform.api.engine.
   * IPentahoSession, java.lang.String)
   */
  public String[] mapConnectionRoles( IPentahoSession userSession, String connectionContext )
    throws PentahoAccessControlException {
    // The connectionContextName for this mapper is the Mondrian Catalog.
    String[] mondrianRoleNames = getMondrianRolesFromCatalog( userSession, connectionContext );
    String[] platformRoleNames = getPlatformRolesFromSession( userSession );
    String[] mappedResult = null;
    if ( ( mondrianRoleNames != null ) && ( platformRoleNames != null ) && ( mondrianRoleNames.length > 0 )
        && ( platformRoleNames.length > 0 ) ) {
      mappedResult = mapRoles( mondrianRoleNames, platformRoleNames );
    }
    return mappedResult;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IConnectionUserRoleMapper#mapConnectionUser(org.pentaho.platform.api.engine.
   * IPentahoSession, java.lang.String)
   */
  public Object mapConnectionUser( IPentahoSession userSession, String context ) {
    throw new UnsupportedOperationException();
  }

}
