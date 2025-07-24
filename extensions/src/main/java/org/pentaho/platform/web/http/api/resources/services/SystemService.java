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


package org.pentaho.platform.web.http.api.resources.services;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class for System endpoints
 */
public class SystemService {

  private static SystemService systemService;

  //This class does not need to be a singleton but putting a get instance method here removes the need to reproduce
  //getter code in the several resource classes grabbing thing object. Getter code is required in case the class is being
  //set through spring.
  public static SystemService getSystemService() {
    if ( systemService == null ) {
      systemService = PentahoSystem.get( SystemService.class );
      if ( systemService == null ) {
        systemService = new SystemService();
      }
    }
    return systemService;
  }

  /**
   * Returns XML for list of users in the platform.
   */
  public Document getUsers() throws ServletException, IOException, IllegalAccessException {
    if ( !canAdminister() ) {
      throw new IllegalAccessException();
    }

    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    Element rootElement = new DefaultElement( "users" ); //$NON-NLS-1$
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( service != null ) {
      List<String> users = service.getAllUsers();
      for ( Iterator<String> usersIterator = users.iterator(); usersIterator.hasNext(); ) {
        String username = usersIterator.next().toString();
        if ( ( null != username ) && ( username.length() > 0 ) ) {
          rootElement.addElement( "user" ).setText( username ); //$NON-NLS-1$
        }
      }
    }
    return doc;
  }

  /**
   * Returns XML for list of roles.
   */
  public Document getRoles() throws ServletException, IOException {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    Element rootElement = new DefaultElement( "roles" ); //$NON-NLS-1$
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( service != null ) {
      List<String> roles = service.getAllRoles();
      for ( Iterator<String> rolesIterator = roles.iterator(); rolesIterator.hasNext(); ) {
        String roleName = rolesIterator.next().toString();
        if ( ( null != roleName ) && ( roleName.length() > 0 ) ) {
          rootElement.addElement( "role" ).setText( roleName ); //$NON-NLS-1$
        }
      }
    }
    return doc;
  }

  /**
   * Returns a list of Roles for a given User.
   */
  public List<String> getRolesForUser( String user ) {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    return service.getRolesForUser( null, user );
  }

  /**
   * Returns a list of Users for a given Role.
   */
  public List<String> getUsersInRole( String role ) {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    return service.getUsersInRole( null, role );
  }

  /**
   * Returns XML for list of Permission.
   */
  public Document getPermissions() throws ServletException, IOException {
    Map<?, ?> validPermissionsNameMap =
        /* PentahoAclEntry.getValidPermissionsNameMap( IPentahoAclEntry.PERMISSIONS_LIST_ALL ) TODO */ new HashMap<String, String>();
    Element rootElement = new DefaultElement( "acls" ); //$NON-NLS-1$
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( validPermissionsNameMap != null ) {
      Set<?> aclsKeySet = validPermissionsNameMap.keySet();
      for ( Iterator<?> aclsIterator = aclsKeySet.iterator(); aclsIterator.hasNext(); ) {
        String aclName = aclsIterator.next().toString();
        String aclMask =
            null != validPermissionsNameMap.get( aclName ) ? validPermissionsNameMap.get( aclName ).toString() : null;

        if ( ( null != aclName ) && ( aclName.length() > 0 ) && ( null != aclMask ) && ( aclMask.length() > 0 ) ) {
          Element aclElement = rootElement.addElement( "acl" ); //$NON-NLS-1$
          aclElement.addElement( "name" ).setText( aclName ); //$NON-NLS-1$
          aclElement.addElement( "mask" ).setText( aclMask ); //$NON-NLS-1$
        }

      }
    }
    return doc;
  }

  public Document getAll() throws ServletException, IOException, IllegalAccessException {
    Document userDoc = getUsers();
    Document roleDoc = getRoles();
    Document permissionDoc = getPermissions();
    return mergeAllDocument( new Document[] { userDoc, roleDoc, permissionDoc } );
  }

  private Document mergeAllDocument( Document[] documents ) {
    Document document = DocumentHelper.createDocument();
    Element element = new DefaultElement( "content" ); //$NON-NLS-1$
    document.add( element );
    for ( Document contentDocument : documents ) {
      if ( ( contentDocument != null ) && ( contentDocument.getRootElement() != null ) ) {
        element.add( contentDocument.getRootElement() );
      }
    }
    return document;
  }

  protected boolean canAdminister() {
    return SystemUtils.canAdminister();
  }
}
