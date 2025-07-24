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


package org.pentaho.platform.repository2.unified.jcr;

import org.apache.jackrabbit.core.SessionImpl;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.util.Assert;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.util.Calendar;
import java.util.Date;

public class SimpleJcrTestUtils {

  public static void deleteItem( final JcrTemplate jcrTemplate, final String absPath ) {
    jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item;
        try {
          item = session.getItem( absPath );
        } catch ( PathNotFoundException e ) {
          return null;
        }
        item.remove();
        session.save();
        return null;
      }
    } );
  }

  public static String addNode( final JcrTemplate jcrTemplate, final String parentAbsPath, final String name,
      final String primaryNodeTypeName ) {
    return (String) jcrTemplate.execute( new JcrCallback() {
      public String doInJcr( final Session session ) throws RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Node newNode;
        try {
          Item item = session.getItem( parentAbsPath );
          Assert.isTrue( item.isNode(), "The item at the specified path must be a node. Ensure the path points to a valid node." );
          Node parentNode = (Node) item;
          newNode = parentNode.addNode( name, primaryNodeTypeName );
          newNode.addMixin( pentahoJcrConstants.getMIX_REFERENCEABLE() );
        } catch ( PathNotFoundException e ) {
          return null;
        }
        session.save();
        return newNode.getUUID();
      }
    } );
  }

  public static Item getItem( final JcrTemplate jcrTemplate, final String absPath ) {
    return (Item) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item;
        try {
          item = session.getItem( absPath );
        } catch ( PathNotFoundException e ) {
          return null;
        }
        return item;
      }
    } );
  }

  public static String getNodeId( final JcrTemplate jcrTemplate, final String absPath ) {
    return (String) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item;
        try {
          item = session.getItem( absPath );
        } catch ( PathNotFoundException e ) {
          return null;
        }
        Assert.isTrue( item.isNode(), "The item must be a node. Ensure the provided path corresponds to a valid node." );
        return ( (Node) item ).getUUID();
      }
    } );
  }

  public static int getVersionCount( final JcrTemplate jcrTemplate, final String absPath ) {
    return (Integer) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Node fileNode = (Node) session.getItem( absPath );
        VersionHistory versionHistory = fileNode.getVersionHistory();
        VersionIterator versionIterator = versionHistory.getAllVersions();
        int versionCount = 0;
        while ( versionIterator.hasNext() ) {
          versionIterator.nextVersion();
          versionCount++;
        }
        return versionCount;
      }
    } );
  }

  public static void printAccess( final JcrTemplate jcrTemplate, final String absPath ) {
    jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {

        SessionImpl jrSession = (SessionImpl) session;
        AccessControlPolicy[] epols = jrSession.getAccessControlManager().getEffectivePolicies( absPath );
        AccessControlPolicy[] pols = jrSession.getAccessControlManager().getPolicies( absPath );
        AccessControlPolicyIterator apols = jrSession.getAccessControlManager().getApplicablePolicies( absPath );
        return null;
      }
    } );
  }

  public static boolean hasPrivileges( final JcrTemplate jcrTemplate, final String absPath,
                                       final String... privNames ) {
    return (Boolean) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Assert.notEmpty( privNames, "The privilege names array must not be empty. Ensure at least one privilege name is provided." );
        Privilege[] privs = new Privilege[privNames.length];
        for ( int i = 0; i < privs.length; i++ ) {
          privs[i] = session.getAccessControlManager().privilegeFromName( privNames[i] );
        }
        Privilege[] privileges = session.getAccessControlManager().getPrivileges( absPath );
        return session.getAccessControlManager().hasPrivileges( absPath, privs );
      }
    } );

  }

  public static boolean isLocked( final JcrTemplate jcrTemplate, final String absPath ) {
    return (Boolean) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item = session.getItem( absPath );
        Assert.isTrue( item.isNode(), "The item at the specified path must be a node. Ensure the path points to a valid node." );
        return ( (Node) item ).isLocked();
      }
    } );
  }

  public static String getString( final JcrTemplate jcrTemplate, final String absPath ) {
    return (String) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item = session.getItem( absPath );
        Assert.isTrue( !item.isNode(), "The item at the specified path must be a property. Ensure the path points to a valid property." );
        return ( (Property) item ).getString();
      }
    } );
  }

  public static Date getDate( final JcrTemplate jcrTemplate, final String absPath ) {
    return (Date) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item = session.getItem( absPath );
        Assert.isTrue( !item.isNode(), "The item at the specified path must be a property. Ensure the path points to a valid property." );
        return ( (Property) item ).getDate().getTime();
      }
    } );
  }

  public static void setDate( final JcrTemplate jcrTemplate, final String absPath, final Date date ) {
    jcrTemplate.execute( new JcrCallback() {
      public Void doInJcr( final Session session ) throws RepositoryException {
        if ( !session.itemExists( absPath ) ) {
          int lastSlashIdx = absPath.lastIndexOf( '/' );
          String parentPath = absPath.substring( 0, lastSlashIdx );
          Node parentNode = (Node) session.getItem( parentPath );
          Calendar cal = Calendar.getInstance();
          cal.setTime( date );
          parentNode.setProperty( absPath.substring( lastSlashIdx + 1 ), cal );
        } else {
          Item item = session.getItem( absPath );
          Assert.isTrue( !item.isNode(), "The item at the specified path must be a node. Ensure the path points to a valid node." );
          Calendar cal = Calendar.getInstance();
          cal.setTime( date );
          ( (Property) item ).setValue( cal );
        }
        session.save();
        return null;
      }
    } );
  }

  public static void move( final JcrTemplate jcrTemplate, final String src, final String dest ) {
    jcrTemplate.execute( new JcrCallback() {
      public Void doInJcr( final Session session ) throws RepositoryException {
        session.move( src, dest );
        session.save();
        return null;
      }
    } );
  }

  public static boolean isCheckedOut( final JcrTemplate jcrTemplate, final String absPath ) {
    return (Boolean) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item = session.getItem( absPath );
        Assert.isTrue( item.isNode(), "The item at the specified path must be a node. Ensure the path points to a valid node" );
        return ( (Node) item ).isCheckedOut();
      }
    } );
  }

  public static String getVersionHistoryNodePath( final JcrTemplate jcrTemplate, final String absPath ) {
    return (String) jcrTemplate.execute( new JcrCallback() {
      public Object doInJcr( final Session session ) throws RepositoryException {
        Item item = session.getItem( absPath );
        Assert.isTrue( item.isNode(), "The item at the specified path must be a node. Ensure the path points to a valid node" );
        Node node = ( (Node) item );
        return node.getVersionHistory().getPath();
      }
    } );
  }

}
