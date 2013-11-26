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

package org.pentaho.platform.plugin.outputs;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentListener;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.BufferedContentItem;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.io.InputStream;
import java.util.Calendar;
import java.util.StringTokenizer;

public abstract class JcrCmsOutputHandler extends BaseOutputHandler {

  public abstract Repository getRepository();

  private static final Log logger = LogFactory.getLog( JcrCmsOutputHandler.class );

  public abstract Session getJcrSession( Repository repository );

  @Override
  public IContentItem getFileOutputContentItem() {

    String contentName = getContentRef();
    try {
      Repository repository = getRepository();

      if ( repository == null ) {
        Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
          "JcrCmsOutputHandler.ERROR_0001_GETTING_CMSREPO" ) ); //$NON-NLS-1$
        return null;
      }

      Session jcrSession = getJcrSession( repository );

      if ( jcrSession == null ) {
        Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
          "JcrCmsOutputHandler.ERROR_0002_GETTING_SESSION" ) ); //$NON-NLS-1$
        return null;
      }

      // Use the root node as a starting point
      Node root = jcrSession.getRootNode();
      if ( root == null ) {
        Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
          "JcrCmsOutputHandler.ERROR_0003_GETTING_ROOT" ) ); //$NON-NLS-1$
        return null;
      }

      Node node = root;

      // parse the path
      StringTokenizer tokenizer = new StringTokenizer( contentName, "/" ); //$NON-NLS-1$
      int levels = tokenizer.countTokens();
      for ( int idx = 0; idx < levels - 1; idx++ ) {
        String folder = tokenizer.nextToken();
        if ( !node.hasNode( folder ) ) {
          // Create an unstructured node under which to import the XML
          node = node.addNode( folder, "nt:folder" ); //$NON-NLS-1$
        } else {
          node = node.getNodes( folder ).nextNode();
        }
      }
      // we should be at the right level now
      String fileName = tokenizer.nextToken();
      Node fileNode = null;
      Node contentNode = null;
      Version version = null;
      if ( node.hasNode( fileName ) ) {
        fileNode = node.getNode( fileName );
        contentNode = fileNode.getNode( "jcr:content" ); //$NON-NLS-1$
        if ( contentNode.isLocked() ) {
          JcrCmsOutputHandler.logger.warn( Messages.getInstance().getString(
            "JcrCmsOutputHandler.ERROR_0004_NODE_LOCKED", contentName ) ); //$NON-NLS-1$
          return null;
        }
        if ( contentNode.isCheckedOut() ) {
          JcrCmsOutputHandler.logger.warn( Messages.getInstance().getString(
              "JcrCmsOutputHandler.ERROR_0005_NODE_CHECKED_OUT", contentName ) ); //$NON-NLS-1$
          return null;
        }
        contentNode.checkout();
        VersionHistory history = contentNode.getVersionHistory();
        VersionIterator iterator = history.getAllVersions();
        while ( iterator.hasNext() ) {
          version = iterator.nextVersion();
          JcrCmsOutputHandler.logger.trace( version.getPath()
              + "," + version.getName() + "," + version.getIndex() + "," + version.getCreated().toString() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

      } else {
        fileNode = node.addNode( fileName, "nt:file" ); //$NON-NLS-1$
        fileNode.addMixin( "mix:versionable" ); //$NON-NLS-1$
        // create the mandatory child node - jcr:content
        contentNode = fileNode.addNode( "jcr:content", "nt:resource" ); //$NON-NLS-1$ //$NON-NLS-2$
        contentNode.addMixin( "mix:versionable" ); //$NON-NLS-1$
        contentNode.addMixin( "mix:filename" ); //$NON-NLS-1$
        contentNode.setProperty( "jcr:mimeType", getMimeType() ); //$NON-NLS-1$
        contentNode.setProperty( "jcr:name", fileName ); //$NON-NLS-1$
        contentNode.setProperty( "jcr:encoding", LocaleHelper.getSystemEncoding() ); //$NON-NLS-1$
      }

      CmsContentListener listener = new CmsContentListener( contentNode, jcrSession );
      BufferedContentItem contentItem = new BufferedContentItem( listener );
      listener.setContentItem( contentItem );
      if ( false ) { // Disable faked search for now
        search( "test", jcrSession ); //$NON-NLS-1$
      }
      return contentItem;
    } catch ( LockException le ) {
      Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
        "JcrCmsOutputHandler.ERROR_0006_GETTING_OUTPUTHANDLER" ) + contentName, le ); //$NON-NLS-1$
    } catch ( NestableRuntimeException nre ) {
      Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
        "JcrCmsOutputHandler.ERROR_0006_GETTING_OUTPUTHANDLER" ) + contentName, nre ); //$NON-NLS-1$
    } catch ( RepositoryException re ) {
      Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
        "JcrCmsOutputHandler.ERROR_0006_GETTING_OUTPUTHANDLER" ) + contentName, re ); //$NON-NLS-1$
    }
    return null;
  }

  private void search( final String searchStr, final Session session ) {
    try {
      Workspace workspace = session.getWorkspace();
      QueryManager queryManager = workspace.getQueryManager();
      Query query = queryManager.createQuery( "//*[jcr:contains(., '" + searchStr + "')]", Query.XPATH ); //$NON-NLS-1$ //$NON-NLS-2$
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      while ( it.hasNext() ) {
        Node n = it.nextNode();
        JcrCmsOutputHandler.logger.trace( n.getName() );
        if ( n.getName().equals( "jcr:content" ) ) { //$NON-NLS-1$
          if ( n.getProperty( "jcr:mimeType" ) != null ) { //$NON-NLS-1$
            JcrCmsOutputHandler.logger.trace( "jcr:mimeType=" + n.getProperty( "jcr:mimeType" ).getString() );
            //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
      }
    } catch ( InvalidQueryException iqe ) {
      Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
        "JcrCmsOutputHandler.ERROR_0008_SEARCH_FAILED" ), iqe ); //$NON-NLS-1$
    } catch ( RepositoryException re ) {
      Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
        "JcrCmsOutputHandler.ERROR_0008_SEARCH_FAILED" ), re ); //$NON-NLS-1$
    }

  }

  private class CmsContentListener implements IContentListener {

    private Node node;

    private Session session;

    private BufferedContentItem contentItem;

    public CmsContentListener() {
    }

    public CmsContentListener( final Node node, final Session session ) {
      this.node = node;
      this.session = session;
    }

    public void close() {
      try {
        InputStream inputStream = contentItem.getInputStream();
        node.setProperty( "jcr:data", inputStream ); //$NON-NLS-1$
        Calendar lastModified = Calendar.getInstance();
        node.setProperty( "jcr:lastModified", lastModified ); //$NON-NLS-1$
        session.save();
        node.checkin();
        session.save();
      } catch ( LockException le ) {
        Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
          "JcrCmsOutputHandler.ERROR_0007_SAVING_CONTENT" ), le ); //$NON-NLS-1$
      } catch ( RepositoryException re ) {
        Logger.error( JcrCmsOutputHandler.class.getName(), Messages.getInstance().getString(
          "JcrCmsOutputHandler.ERROR_0007_SAVING_CONTENT" ), re ); //$NON-NLS-1$
      }
    }

    public void setNode( final Node node ) {
      this.node = node;
    }

    public void setSession( final Session session ) {
      this.session = session;
    }

    public void setContentItem( final BufferedContentItem contentItem ) {
      this.contentItem = contentItem;
    }

    public void setMimeType( final String mimeType ) {

    }

    public void setName( final String Name ) {

    }

  }

}
