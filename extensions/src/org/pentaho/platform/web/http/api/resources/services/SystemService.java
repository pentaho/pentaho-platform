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

package org.pentaho.platform.web.http.api.resources.services;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import javax.servlet.ServletException;
import java.io.IOException;
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
   * Returns XML for list of Roles for a given User.
   */
  public Document getRolesForUser( String user ) throws ServletException, IOException {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    Element rootElement = new DefaultElement( "roles" ); //$NON-NLS-1$
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( service != null ) {
      List<String> roles = service.getRolesForUser( null, user );
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
   * Returns XML for list of Users for a given Role.
   */
  public Document getUsersInRole( String role ) throws ServletException, IOException {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    Element rootElement = new DefaultElement( "users" ); //$NON-NLS-1$
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( service != null ) {
      List<String> users = service.getUsersInRole( null, role );
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
   * Returns XML for list of Permission.
   */
  public Document getPermissions() throws ServletException, IOException {
    Map<?, ?> validPermissionsNameMap =
        PentahoAclEntry.getValidPermissionsNameMap( IPentahoAclEntry.PERMISSIONS_LIST_ALL );
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
