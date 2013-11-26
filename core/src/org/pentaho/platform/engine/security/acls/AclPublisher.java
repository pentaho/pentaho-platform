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

package org.pentaho.platform.engine.security.acls;

import org.dom4j.Element;
import org.pentaho.platform.api.engine.IAclPublisher;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.SpringSecurityPermissionMgr;
import org.pentaho.platform.engine.security.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AclPublisher implements IAclPublisher {

  private static final String NOTHING = "NOTHING"; //$NON-NLS-1$

  private static final String ADMINISTRATION = "ADMINISTRATION"; //$NON-NLS-1$

  private static final String EXECUTE = "EXECUTE"; //$NON-NLS-1$

  private static final String EXECUTE_ADMINISTRATION = "EXECUTE_ADMINISTRATION"; //$NON-NLS-1$

  private static final String SUBSCRIBE = "SUBSCRIBE"; //$NON-NLS-1$

  private static final String CREATE = "CREATE"; //$NON-NLS-1$

  private static final String UPDATE = "UPDATE"; //$NON-NLS-1$

  private static final String DELETE = "DELETE"; //$NON-NLS-1$

  private static final String SUBSCRIBE_ADMINISTRATION = "SUBSCRIBE_ADMINISTRATION"; //$NON-NLS-1$

  private static final String EXECUTE_SUBSCRIBE = "EXECUTE_SUBSCRIBE"; //$NON-NLS-1$

  /**
   * @deprecated Do not use this constant; instead use FULL_CONTROL Previously referenced a static list of access
   *             controls
   */
  @Deprecated
  private static final String ADMIN_ALL = "ADMIN_ALL"; //$NON-NLS-1$

  private static final String FULL_CONTROL = "FULL_CONTROL"; //$NON-NLS-1$ 

  private Map<IPermissionRecipient, IPermissionMask> defaultAcls = Collections.EMPTY_MAP;

  /**
   * Constructor that allows overriding the source of the default access control list. This constructor is mainly
   * used from test cases.
   * 
   * @param defAcls
   */
  public AclPublisher( final Map<IPermissionRecipient, IPermissionMask> defAcls ) {
    this.defaultAcls = new LinkedHashMap<IPermissionRecipient, IPermissionMask>( defAcls );
  }

  /**
   * Default constructor. This constructor reads the default access controls from the pentaho.xml. The pentaho.xml
   * needs to have a section similar to the following: <br>
   * <br>
   * &nbsp;&nbsp;&lt;acl-publisher&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;!--<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;These acls are used when publishing from the file system. Every
   * folder<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gets these ACLS. Authenticated is a "default" role that everyone<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;gets when they're authenticated (be sure to setup your bean xml
   * properly<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;for this to work).<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;--&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;default-acls&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;acl-entry role="Admin" acl="7" /&gt; &lt;!-- Admin users get all
   * authorities --&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;acl-entry role="cto" acl="7" /&gt; &lt;!-- CTO gets everything
   * --&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;acl-entry role="dev" acl="6" /&gt; &lt;!-- Dev gets
   * execute/subscribe --&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;acl-entry role="Authenticated" acl="2" /&gt; &lt;!--
   * Authenticated users get execute only --&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/default-acls&gt;<br>
   * &nbsp;&nbsp;&lt;/acl-publisher&gt;<br>
   * 
   * 
   */
  public AclPublisher() {
    // Read the default ACLs from the pentaho.xml.
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    List sysAcls = settings.getSystemSettings( "default-acls/*" ); //$NON-NLS-1$
    defaultAcls = aclFromNodeList( sysAcls );
  }

  @SuppressWarnings( "deprecation" )
  private Map<IPermissionRecipient, IPermissionMask> aclFromNodeList( final List sysAcls ) {
    Map<IPermissionRecipient, IPermissionMask> pentahoAclEntries =
        new LinkedHashMap<IPermissionRecipient, IPermissionMask>();

    for ( int i = 0; i < sysAcls.size(); i++ ) {
      Object obj = sysAcls.get( i );
      Element defAcl = (Element) obj;
      String aclRole = XmlDom4JHelper.getNodeText( "@role", defAcl, null ); //$NON-NLS-1$
      String aclUser = XmlDom4JHelper.getNodeText( "@user", defAcl, null ); //$NON-NLS-1$
      String aclStr = XmlDom4JHelper.getNodeText( "@acl", defAcl, null ); //$NON-NLS-1$

      if ( ( aclRole == null ) && ( aclUser == null ) ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
            "AclPublisher.ERROR_0001_DEFAULT_ACL_REQUIRES_USER_OR_ROLE" ) ); //$NON-NLS-1$
      }
      if ( ( aclRole != null ) && ( aclUser != null ) ) {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
            "AclPublisher.ERROR_0002_DEFAULT_ACL_HAS_BOTH" ) ); //$NON-NLS-1$
      }

      int aclValue = -1; // Default to undefined
      if ( aclStr != null ) {
        if ( AclPublisher.NOTHING.equalsIgnoreCase( aclStr ) ) {
          aclValue = 0;
        } else if ( AclPublisher.EXECUTE.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_EXECUTE;
        } else if ( AclPublisher.SUBSCRIBE.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_SUBSCRIBE;
        } else if ( AclPublisher.EXECUTE_SUBSCRIBE.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_EXECUTE_SUBSCRIBE;
        } else if ( AclPublisher.CREATE.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_CREATE;
        } else if ( AclPublisher.UPDATE.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_UPDATE;
        } else if ( AclPublisher.DELETE.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_DELETE;
        } else if ( AclPublisher.ADMINISTRATION.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_ADMINISTRATION;
        } else if ( AclPublisher.EXECUTE_ADMINISTRATION.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_EXECUTE_ADMINISTRATION;
        } else if ( AclPublisher.SUBSCRIBE_ADMINISTRATION.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_SUBSCRIBE_ADMINISTRATION;
        } else if ( AclPublisher.ADMIN_ALL.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_ADMIN_ALL;
        } else if ( AclPublisher.FULL_CONTROL.equalsIgnoreCase( aclStr ) ) {
          aclValue = IPentahoAclEntry.PERM_FULL_CONTROL;
        } else {
          // Parse it as an integer then - BUSERVER-4651
          try {
            aclValue = Integer.parseInt( aclStr );
          } catch ( Exception ignored ) {
            continue;
          }
        }
      }

      if ( aclUser != null ) {
        pentahoAclEntries.put( new SimpleUser( aclUser ), new SimplePermissionMask( aclValue ) );
      } else {
        pentahoAclEntries.put( new SimpleRole( aclRole ), new SimplePermissionMask( aclValue ) );
      }
    }

    return pentahoAclEntries;
  }

  /**
   * This method is called from the RDBMS repository publish method when publishing a file-based solution to the
   * RDBMS repository. This implementation recurses through all the children of the specified
   * <tt>IAclSolutionFile</tt>, and applies the default access controls only to the
   * 
   * @param rootFile
   * 
   * @see IAclSolutionFile
   */
  public void publishDefaultAcls( final IAclSolutionFile rootFile ) {
    publishDefaultFolderAcls( rootFile );
    publishOverrideAcls( rootFile );
  }

  private void publishDefaultFolderAcls( final IAclSolutionFile rootFile ) {
    if ( ( rootFile != null ) && ( rootFile.isDirectory() ) ) {
      // publish acl for folder if it doesn't already exist...
      if ( rootFile.getAccessControls().size() == 0 ) {
        SpringSecurityPermissionMgr.instance().setPermissions( defaultAcls, rootFile );
      }
      // Now, recurse through kids looking for folders...
      Set kids = rootFile.getChildrenFiles();
      if ( kids != null ) { // Doesn't have to have kids in it...
        Iterator it = kids.iterator();
        IAclSolutionFile aChild = null;
        while ( it.hasNext() ) {
          // Recursively publish ACLs for all child folders
          aChild = (IAclSolutionFile) it.next();
          if ( aChild.isDirectory() ) {
            publishDefaultFolderAcls( aChild );
          }
        }
      }
    }
  }

  private void publishOverrideAcls( final IAclSolutionFile rootFile ) {
    Map<IPermissionRecipient, IPermissionMask> overridePerms = getOverrideAclList( rootFile.getFullPath() );
    if ( overridePerms.size() > 0 ) {
      Map<IPermissionRecipient, IPermissionMask> currentPerms =
          SpringSecurityPermissionMgr.instance().getPermissions( rootFile );
      if ( ( currentPerms.size() == 0 ) || ( currentPerms.size() == defaultAcls.size() )
          && ( currentPerms.entrySet().containsAll( defaultAcls.entrySet() ) ) ) {
        // We've got overridden acls and the file contains ONLY the default acls or NO acls at all
        SpringSecurityPermissionMgr.instance().setPermissions( overridePerms, rootFile );
      }
    }

    // Recurse through this files children
    if ( rootFile.isDirectory() ) {
      Iterator iter = rootFile.getChildrenFiles().iterator();
      while ( iter.hasNext() ) {
        publishOverrideAcls( (IAclSolutionFile) iter.next() );
      }
    }
  }

  private Map<IPermissionRecipient, IPermissionMask> getOverrideAclList( final String fullPath ) {
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    return aclFromNodeList( settings.getSystemSettings( "overrides/file[@path=\"" + fullPath + "\"]/*" ) ); //$NON-NLS-1$//$NON-NLS-2$;
  }

  /**
   * Returns an unmodifiable map of default access controls.
   * 
   * @return An unmodifiable map containing all the default access controls.
   */
  public Map<IPermissionRecipient, IPermissionMask> getDefaultAclList() {
    return Collections.unmodifiableMap( defaultAcls );
  }

}
