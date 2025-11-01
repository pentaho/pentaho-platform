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


package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionMgr;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Deprecated
public class SpringSecurityPermissionMgr implements IPermissionMgr {

  private static final SpringSecurityPermissionMgr singletonPermMgr = new SpringSecurityPermissionMgr();

  private SpringSecurityPermissionMgr() {
  }

  public static SpringSecurityPermissionMgr instance() {
    return SpringSecurityPermissionMgr.singletonPermMgr;
  }

  public Map<IPermissionRecipient, IPermissionMask> getPermissions( final Object domainInstance ) {
    IAclHolder aclHolder = (IAclHolder) domainInstance;
    List<IPentahoAclEntry> aclList = aclHolder.getAccessControls();
    return transformEntries( aclList );
  }

  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions( Object domainInstance ) {
    IAclHolder aclHolder = (IAclHolder) domainInstance;
    List<IPentahoAclEntry> aclList = aclHolder.getEffectiveAccessControls();
    return transformEntries( aclList );
  }

  /**
   * Converts from List&lt;IPentahoAclEntry&gt; to Map&lt;IPermissionRecipient, IPermissionMask&gt;.
   */
  @SuppressWarnings( "deprecation" )
  protected Map<IPermissionRecipient, IPermissionMask> transformEntries( List<IPentahoAclEntry> entriesFromHolder ) {
    Map<IPermissionRecipient, IPermissionMask> permissionsMap =
      new LinkedHashMap<IPermissionRecipient, IPermissionMask>();
    for ( IPentahoAclEntry pentahoAclEntry : entriesFromHolder ) {
      IPermissionRecipient permissionRecipient = null;
      if ( pentahoAclEntry.getRecipient() instanceof SimpleGrantedAuthority ) {
        SimpleGrantedAuthority grantedAuthorityImpl = (SimpleGrantedAuthority) pentahoAclEntry.getRecipient();
        permissionRecipient = new SimpleRole( grantedAuthorityImpl.toString() );
      } else if ( pentahoAclEntry.getRecipient() instanceof SimpleRole ) {
        permissionRecipient = new SimpleRole( (String) pentahoAclEntry.getRecipient() );
      } else {
        permissionRecipient = new SimpleUser( (String) pentahoAclEntry.getRecipient() );
      }
      IPermissionMask permissionMask = new SimplePermissionMask( pentahoAclEntry.getMask() );
      permissionsMap.put( permissionRecipient, permissionMask );
    }
    return permissionsMap;
  }

  @SuppressWarnings( "deprecation" )
  public void setPermission( final IPermissionRecipient permissionRecipient, final IPermissionMask permission,
                             final Object object ) {
    if ( object == null || !( object instanceof IAclHolder ) ) {
      // i would argue that the "object" parameter should be IAclHolder!
      return;
    }
    IAclHolder aclHolder = (IAclHolder) object;
    PentahoAclEntry entry = new PentahoAclEntry();
    // TODO mlowery instanceof is undesirable as it doesn't allow new concrete classes.
    if ( permissionRecipient instanceof SimpleRole ) {
      entry.setRecipient( new SimpleGrantedAuthority( permissionRecipient.getName() ) );
    } else {
      entry.setRecipient( permissionRecipient.getName() );
    }
    entry.addPermission( permission.getMask() );
    // HibernateUtil.beginTransaction(); - This is now handled by the RepositoryFile
    aclHolder.getAccessControls().add( entry );
    // HibernateUtil.commitTransaction(); - This should be covered by the exitPoint call
  }

  @SuppressWarnings( "deprecation" )
  public void setPermissions( final Map<IPermissionRecipient, IPermissionMask> permissionsMap, final Object object ) {
    if ( object == null || !( object instanceof IAclHolder ) ) {
      // i would argue that the "object" parameter should be IAclHolder!
      return;
    }
    IAclHolder aclHolder = (IAclHolder) object;
    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = permissionsMap.entrySet();
    ArrayList<IPentahoAclEntry> aclList = new ArrayList<IPentahoAclEntry>();
    for ( Entry<IPermissionRecipient, IPermissionMask> mapEntry : mapEntrySet ) {
      PentahoAclEntry pentahoAclEntry = new PentahoAclEntry();
      IPermissionRecipient permissionRecipient = mapEntry.getKey();
      if ( permissionRecipient instanceof SimpleRole ) {
        pentahoAclEntry.setRecipient( new SimpleGrantedAuthority( permissionRecipient.getName() ) );
      } else {
        pentahoAclEntry.setRecipient( permissionRecipient.getName() );
      }
      pentahoAclEntry.addPermission( mapEntry.getValue().getMask() );
      aclList.add( pentahoAclEntry );
    }
    // HibernateUtil.beginTransaction(); - This is now handled in the RepositoryFile
    aclHolder.resetAccessControls( aclList );
    // HibernateUtil.commitTransaction(); - This is covered by the exitPoint
  }

}
