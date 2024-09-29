/*!
 *
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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.management.DataStoreGarbageCollector;
import org.apache.jackrabbit.core.IPentahoSystemSessionFactory;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.VersionHistory;

/**
 * This class provides a static method {@linkplain #gc()} for running JCR's GC routine.
 *
 * @author Andrey Khayrutdinov
 */
public class RepositoryCleaner {

  private final Log logger = LogFactory.getLog( RepositoryCleaner.class );
  private static final String JCR_FROZEN_NODE = "jcr:frozenNode";
  private static final String JCR_FROZEN_UUID = "jcr:frozenUuid";
  private static final String JCR_ROOT_VERSION = "jcr:rootVersion";
  private IPentahoSystemSessionFactory systemSessionFactory = new IPentahoSystemSessionFactory.DefaultImpl();

  /**
   * Exists primary for testing
   * @param systemSessionFactory
   */
  public void setSystemSessionFactory( IPentahoSystemSessionFactory systemSessionFactory ) {
    this.systemSessionFactory = systemSessionFactory;
  }

  public synchronized void gc() {
    Repository jcrRepository = PentahoSystem.get( Repository.class, "jcrRepository", null );
    if ( jcrRepository == null ) {
      logger.error( "Cannot obtain JCR repository. Exiting" );
      return;
    }

    if ( !( jcrRepository instanceof RepositoryImpl ) ) {
      logger.error(
          String.format( "Expected RepositoryImpl, but got: [%s]. Exiting", jcrRepository.getClass().getName() ) );
      return;
    }

    final RepositoryImpl repository = (RepositoryImpl) jcrRepository;

    try {
      logger.debug( "Starting Orphaned Version Purge" );
      Session systemSession = systemSessionFactory.create( repository );
      Node node = systemSession.getNode( "/jcr:system/jcr:versionStorage" );
      findVersionNodesAndPurge( node, systemSession );
      systemSession.save();
      logger.debug( "Finished Orphaned Version Purge" );
    } catch ( RepositoryException e ) {
      logger.error( "Error running Orphaned Version purge", e );
    }

    try {
      logger.info( "Creating garbage collector" );
      // JCR's documentation recommends not to use RepositoryImpl.createDataStoreGarbageCollector() and
      // instead invoke RepositoryManager.createDataStoreGarbageCollector()
      // (see it here: http://wiki.apache.org/jackrabbit/DataStore#Data_Store_Garbage_Collection)

      // However, the example from the wiki cannot be applied directly, because
      // RepositoryFactoryImpl accepts only TransientRepository's instances that were created by itself;
      // it creates such instance in "not started" state, and when the instance tries to start, it fails,
      // because Pentaho's JCR repository is already running.

      DataStoreGarbageCollector gc = repository.createDataStoreGarbageCollector();
      try {
        logger.debug( "Starting marking stage" );
        gc.setPersistenceManagerScan( false );
        gc.mark();
        logger.debug( "Starting sweeping stage" );
        int deleted = gc.sweep();
        logger.info( String.format( "Garbage collecting completed. %d items were deleted", deleted ) );
      } finally {
        gc.close();
      }
    } catch ( RepositoryException e ) {
      logger.error( "Error during garbage collecting", e );
    }

  }

  private void findVersionNodesAndPurge( Node node, Session session ) {
    if ( node == null || session == null ) {
      return;
    }
    try {
      if ( node.getName().equals( JCR_FROZEN_NODE ) && node.hasProperty( JCR_FROZEN_UUID ) && !node.getParent()
          .getName().equals( JCR_ROOT_VERSION ) ) {
        // Version Node
        Property property = node.getProperty( JCR_FROZEN_UUID );
        Value uuid = property.getValue();
        Node nodeByIdentifier = null;
        try {
          nodeByIdentifier = session.getNodeByIdentifier( uuid.getString() );
          nodeByIdentifier = session.getNode( nodeByIdentifier.getPath() );
        } catch ( RepositoryException ex ) {
          // ignored this means the node is gone.
        }
        if ( nodeByIdentifier == null ) {
          // node is gone
          logger.info( "Removed orphan version: " + node.getPath() );
          ( (VersionHistory) node.getParent().getParent() ).removeVersion( node.getParent().getName() );
        }
      }
    } catch ( RepositoryException e ) {
      logger.error( "Error purging version nodes. Routine will continue", e );
    }

    NodeIterator nodes = null;
    try {
      nodes = node.getNodes();
    } catch ( RepositoryException e ) {
      logger.error( "Error purging version nodes. Routine will continue", e );
    }

    if ( nodes == null ) {
      return;
    }

    while ( nodes.hasNext() ) {
      findVersionNodesAndPurge( nodes.nextNode(), session );
    }
  }
}
