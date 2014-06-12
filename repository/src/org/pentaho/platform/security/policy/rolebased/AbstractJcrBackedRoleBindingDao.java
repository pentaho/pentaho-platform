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

package org.pentaho.platform.security.policy.rolebased;

import com.google.common.collect.HashMultimap;
import org.apache.commons.collections.map.LRUMap;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.JcrStringHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.repository2.unified.jcr.NodeHelper;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.springframework.util.Assert;

import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractJcrBackedRoleBindingDao implements IRoleAuthorizationPolicyRoleBindingDao {

  protected ITenantedPrincipleNameResolver tenantedRoleNameUtils;

  protected Map<String, List<IAuthorizationAction>> immutableRoleBindings;
  protected Map<String, List<String>> immutableRoleBindingNames;

  protected Map<String, List<String>> bootstrapRoleBindings;

  protected String superAdminRoleName;

  private List<IAuthorizationAction> authorizationActions = new ArrayList<IAuthorizationAction>();

  public static final String FOLDER_NAME_AUTHZ = ".authz"; //$NON-NLS-1$

  public static final String FOLDER_NAME_ROLEBASED = "roleBased"; //$NON-NLS-1$

  public static final String FOLDER_NAME_RUNTIMEROLES = "runtimeRoles"; //$NON-NLS-1$
  /**
   * Key: runtime role name; value: list of logical role names
   */
  @SuppressWarnings( "unchecked" )
  protected Map boundLogicalRoleNamesCache = Collections.synchronizedMap( new LRUMap() );

  public AbstractJcrBackedRoleBindingDao(final Map<String, List<IAuthorizationAction>> immutableRoleBindings,
      final Map<String, List<String>> bootstrapRoleBindings, final String superAdminRoleName,
      final ITenantedPrincipleNameResolver tenantedRoleNameUtils, final List<IAuthorizationAction> authorizationActions ) {
    super();
    // TODO: replace with IllegalArgumentException
    Assert.notNull( immutableRoleBindings );
    Assert.notNull( bootstrapRoleBindings );
    Assert.notNull( superAdminRoleName );
    Assert.notNull( authorizationActions );

    this.authorizationActions.addAll(authorizationActions);  

    this.immutableRoleBindings = immutableRoleBindings;
    this.bootstrapRoleBindings = bootstrapRoleBindings;
    this.superAdminRoleName = superAdminRoleName;
    this.tenantedRoleNameUtils = tenantedRoleNameUtils;

    immutableRoleBindingNames = new HashMap<String, List<String>>();
    for ( final Entry<String, List<IAuthorizationAction>> entry : immutableRoleBindings.entrySet() ) {
      final List<String> roles = new ArrayList<String>();
      for ( final IAuthorizationAction a : entry.getValue() ) {
        roles.add( a.getName() );
      }
      immutableRoleBindingNames.put( entry.getKey(), roles );
    }

  }

  public List<String> getBoundLogicalRoleNames( Session session, List<String> runtimeRoleNames )
    throws NamespaceException, RepositoryException {
    Set<String> boundRoleNames = new HashSet<String>();
    HashMap<ITenant, List<String>> tenantMap = new HashMap<ITenant, List<String>>();
    boolean includeSuperAdminLogicalRoles = false;
    for ( String runtimeRoleName : runtimeRoleNames ) {
      if ( !superAdminRoleName.equals( runtimeRoleName ) ) {
        ITenant tenant = JcrTenantUtils.getTenant( runtimeRoleName, false );
        List<String> runtimeRoles = tenantMap.get( tenant );
        if ( runtimeRoles == null ) {
          runtimeRoles = new ArrayList<String>();
          tenantMap.put( tenant, runtimeRoles );
        }
        runtimeRoles.add( tenantedRoleNameUtils.getPrincipleName( runtimeRoleName ) );
      } else {
        includeSuperAdminLogicalRoles = true;
      }
    }
    for ( Map.Entry<ITenant, List<String>> mapEntry : tenantMap.entrySet() ) {
      boundRoleNames.addAll( getBoundLogicalRoleNames( session, mapEntry.getKey(), mapEntry.getValue() ) );
    }
    if ( includeSuperAdminLogicalRoles ) {
      boundRoleNames.addAll( immutableRoleBindingNames.get( superAdminRoleName ) );
    }
    return new ArrayList<String>( boundRoleNames );
  }

  public List<String> getBoundLogicalRoleNames( Session session, ITenant tenant, List<String> runtimeRoleNames )
    throws NamespaceException, RepositoryException {
    if ( ( tenant == null ) || ( tenant.getId() == null ) ) {
      return getBoundLogicalRoleNames( session, runtimeRoleNames );
    }

    if ( !TenantUtils.isAccessibleTenant( tenant ) ) {
      return new ArrayList<String>();
    }

    final List<String> uncachedRuntimeRoleNames = new ArrayList<String>();
    final Set<String> cachedBoundLogicalRoleNames = new HashSet<String>();
    for ( String runtimeRoleName : runtimeRoleNames ) {
      String roleName = tenantedRoleNameUtils.getPrincipleName( runtimeRoleName );
      String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, runtimeRoleName );
      if ( boundLogicalRoleNamesCache.containsKey( roleId ) ) {
        cachedBoundLogicalRoleNames.addAll( (Collection<String>) boundLogicalRoleNamesCache.get( roleId ) );
      } else {
        uncachedRuntimeRoleNames.add( roleName );
      }
    }
    if ( uncachedRuntimeRoleNames.isEmpty() ) {
      // no need to hit the repo
      return new ArrayList<String>( cachedBoundLogicalRoleNames );
    }

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    final String phoNsPrefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":"; //$NON-NLS-1$
    final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
    HashMultimap<String, String> boundLogicalRoleNames = HashMultimap.create();
    Node runtimeRolesFolderNode = getRuntimeRolesFolderNode( session, tenant );
    NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes( onlyPentahoPattern );
    if ( !runtimeRoleNodes.hasNext() ) {
      // no bindings setup yet; fall back on bootstrap bindings
      for ( String runtimeRoleName : uncachedRuntimeRoleNames ) {
        String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, runtimeRoleName );
        if ( bootstrapRoleBindings.containsKey( runtimeRoleName ) ) {
          boundLogicalRoleNames.putAll( roleId, bootstrapRoleBindings.get( runtimeRoleName ) );
        }
      }
    } else {
      for ( String runtimeRoleName : uncachedRuntimeRoleNames ) {
        if ( NodeHelper.hasNode( runtimeRolesFolderNode, phoNsPrefix , runtimeRoleName ) ) {
          Node runtimeRoleFolderNode = NodeHelper.getNode( runtimeRolesFolderNode, phoNsPrefix , runtimeRoleName );
          if ( runtimeRoleFolderNode.hasProperty( pentahoJcrConstants.getPHO_BOUNDROLES() ) ) {
            Value[] values = runtimeRoleFolderNode.getProperty( pentahoJcrConstants.getPHO_BOUNDROLES() ).getValues();
            String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, runtimeRoleName );
            for ( Value value : values ) {
              boundLogicalRoleNames.put( roleId, value.getString() );
            }
          }
        }
      }
    }
    // now add in immutable bound logical role names
    for ( String runtimeRoleName : uncachedRuntimeRoleNames ) {
      if ( immutableRoleBindings.containsKey( runtimeRoleName ) ) {
        String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, runtimeRoleName );
        boundLogicalRoleNames.putAll( roleId, immutableRoleBindingNames.get( runtimeRoleName ) );
      }
    }

    // update cache
    boundLogicalRoleNamesCache.putAll( boundLogicalRoleNames.asMap() );
    // now add in those runtime roles that have no bindings to the cache
    for ( String runtimeRoleName : uncachedRuntimeRoleNames ) {
      String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, runtimeRoleName );
      if ( !boundLogicalRoleNamesCache.containsKey( roleId ) ) {
        boundLogicalRoleNamesCache.put( roleId, Collections.emptyList() );
      }
    }

    // combine cached findings plus ones from repo
    Set<String> res = new HashSet<String>();
    res.addAll( cachedBoundLogicalRoleNames );
    res.addAll( boundLogicalRoleNames.values() );
    return new ArrayList<String>( res );
  }

  public void setRoleBindings( Session session, ITenant tenant, String runtimeRoleName, List<String> logicalRoleNames )
    throws NamespaceException, RepositoryException {
    if ( tenant == null ) {
      tenant = JcrTenantUtils.getTenant( runtimeRoleName, false );
      runtimeRoleName = getPrincipalName( runtimeRoleName );
    }

    if ( !TenantUtils.isAccessibleTenant( tenant ) ) {
      throw new NotFoundException( "Tenant " + tenant.getId() + " not found" );
    }

    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    final String phoNsPrefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":"; //$NON-NLS-1$
    final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
    Node runtimeRolesFolderNode = getRuntimeRolesFolderNode( session, tenant );
    NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes( onlyPentahoPattern );
    int i = 0;
    while ( runtimeRoleNodes.hasNext() ) {
      runtimeRoleNodes.nextNode();
      i++;
    }
    if ( i == 0 ) {
      // no bindings setup yet; install bootstrap bindings; bootstrapRoleBindings will now no longer be
      // consulted
      for ( Map.Entry<String, List<String>> entry : bootstrapRoleBindings.entrySet() ) {
        JcrRoleAuthorizationPolicyUtils.internalSetBindings( pentahoJcrConstants, runtimeRolesFolderNode, entry.getKey(),
            entry.getValue(), phoNsPrefix );
      }
    }
    if ( !isImmutable( runtimeRoleName ) ) {
      JcrRoleAuthorizationPolicyUtils.internalSetBindings( pentahoJcrConstants, runtimeRolesFolderNode, runtimeRoleName,
          logicalRoleNames, phoNsPrefix );
    } else {
      throw new RuntimeException( Messages.getInstance().getString(
          "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0001_ATTEMPT_MOD_IMMUTABLE", runtimeRoleName ) ); //$NON-NLS-1$
    }
    session.save();
    Assert.isTrue( NodeHelper.hasNode( runtimeRolesFolderNode, phoNsPrefix, runtimeRoleName ) );

    // update cache
    String roleId = tenantedRoleNameUtils.getPrincipleId( tenant, runtimeRoleName );
    boundLogicalRoleNamesCache.put( roleId, logicalRoleNames );
  }

  private String getPrincipalName( String principalId ) {
    String principalName = null;
    if ( tenantedRoleNameUtils != null ) {
      principalName = tenantedRoleNameUtils.getPrincipleName( principalId );
    }
    return principalName;
  }

  protected boolean isImmutable( final String runtimeRoleName ) {
    return immutableRoleBindings.containsKey( runtimeRoleName );
  }

  protected Map<String, List<String>> getRoleBindings( Session session, ITenant tenant ) throws RepositoryException {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    if ( tenant == null ) {
      tenant = JcrTenantUtils.getTenant();
    }
    if ( !TenantUtils.isAccessibleTenant( tenant ) ) {
      return map;
    }
    PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
    final String phoNsPrefix = session.getNamespacePrefix( PentahoJcrConstants.PHO_NS ) + ":"; //$NON-NLS-1$
    final String onlyPentahoPattern = phoNsPrefix + "*"; //$NON-NLS-1$
    Node runtimeRolesFolderNode = getRuntimeRolesFolderNode( session, tenant );
    NodeIterator runtimeRoleNodes = runtimeRolesFolderNode.getNodes( onlyPentahoPattern );
    if ( !runtimeRoleNodes.hasNext() ) {
      // no bindings setup yet; fall back on bootstrap bindings
      map.putAll( bootstrapRoleBindings );
    } else {
      while ( runtimeRoleNodes.hasNext() ) {
        Node runtimeRoleNode = runtimeRoleNodes.nextNode();
        if ( runtimeRoleNode.hasProperty( pentahoJcrConstants.getPHO_BOUNDROLES() ) ) {
          // get clean runtime role name
          String runtimeRoleName = JcrStringHelper.fileNameDecode(
                  runtimeRoleNode.getName().substring(phoNsPrefix.length())
          );
          // get logical role names
          List<String> logicalRoleNames = new ArrayList<String>();
          Value[] values = runtimeRoleNode.getProperty( pentahoJcrConstants.getPHO_BOUNDROLES() ).getValues();
          for ( Value value : values ) {
            logicalRoleNames.add( value.getString() );
          }
          map.put( runtimeRoleName, logicalRoleNames );
        }
      }
    }
    // add all immutable bindings
    map.putAll( immutableRoleBindingNames );
    return map;
  }

  public RoleBindingStruct getRoleBindingStruct( Session session, ITenant tenant, String locale )
    throws RepositoryException {
    return new RoleBindingStruct( getMapForLocale( locale ), getRoleBindings( session, tenant ), new HashSet<String>( immutableRoleBindingNames.keySet() ) );
  }

  protected Map<String, String> getMapForLocale( final String localeString ) {
    Map<String, String> map = new HashMap<String, String>();
    for ( IAuthorizationAction authorizationAction : authorizationActions ) {
      map.put( authorizationAction.getName(), authorizationAction.getLocalizedDisplayName(localeString) );
    }
    return map;
  }

  public Node getRuntimeRolesFolderNode( final Session session, ITenant tenant ) throws RepositoryException {
    Node tenantRootFolderNode = null;
    try {
      tenantRootFolderNode = (Node) session.getItem( ServerRepositoryPaths.getTenantRootFolderPath( tenant ) );
    } catch ( PathNotFoundException e ) {
      throw new RepositoryException( "Error retrieving RuntimeRoles for folder, folder not found", e );
      // Assert.state(false, Messages.getInstance().getString(
      // "JcrRoleAuthorizationPolicyRoleBindingDao.ERROR_0002_REPO_NOT_INITIALIZED")); //$NON-NLS-1$
    }
    Node authzFolderNode = tenantRootFolderNode.getNode( FOLDER_NAME_AUTHZ );
    Node roleBasedFolderNode = authzFolderNode.getNode( FOLDER_NAME_ROLEBASED );
    return roleBasedFolderNode.getNode( FOLDER_NAME_RUNTIMEROLES );
  }
}
