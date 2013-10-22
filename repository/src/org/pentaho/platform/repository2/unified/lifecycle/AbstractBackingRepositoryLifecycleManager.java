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

package org.pentaho.platform.repository2.unified.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains some common functionality.
 */
public abstract class AbstractBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers
  // ======================================================================================

  protected static final Log logger = LogFactory.getLog( DefaultBackingRepositoryLifecycleManager.class );

  // ~ Instance fields
  // =================================================================================================

  protected TransactionTemplate txnTemplate;

  protected JcrTemplate adminJcrTemplate;

  protected IPathConversionHelper pathConversionHelper;

  // ~ Constructors
  // ====================================================================================================

  public AbstractBackingRepositoryLifecycleManager( final TransactionTemplate txnTemplate,
      final JcrTemplate adminJcrTemplate, final IPathConversionHelper pathConversionHelper ) {
    Assert.notNull( txnTemplate );
    this.txnTemplate = txnTemplate;
    this.adminJcrTemplate = adminJcrTemplate;
    this.pathConversionHelper = pathConversionHelper;
    initTransactionTemplate();
  }

  protected void initTransactionTemplate() {
    // a new transaction must be created (in order to run with the correct user privileges)
    txnTemplate.setPropagationBehavior( TransactionDefinition.PROPAGATION_REQUIRES_NEW );
  }

  public void addMetadataToRepository( final String metadataProperty ) {
    txnTemplate.execute( new TransactionCallbackWithoutResult() {
      public void doInTransactionWithoutResult( final TransactionStatus status ) {
        adminJcrTemplate.execute( new JcrCallback() {
          @Override
          public Object doInJcr( Session session ) throws IOException, RepositoryException {
            new PentahoJcrConstants( session );
            String absPath = ServerRepositoryPaths.getPentahoRootFolderPath();
            RepositoryFile rootFolder =
                JcrRepositoryFileUtils
                    .getFileByAbsolutePath( session, absPath, pathConversionHelper, null, false, null );
            if ( rootFolder != null ) {
              Map<String, Serializable> metadataMap =
                  JcrRepositoryFileUtils.getFileMetadata( session, rootFolder.getId() );
              if ( metadataMap == null ) {
                metadataMap = new HashMap<String, Serializable>();
              }
              metadataMap.put( metadataProperty, Boolean.TRUE );
              JcrRepositoryFileUtils.setFileMetadata( session, rootFolder.getId(), metadataMap );
            } else {
              throw new IllegalStateException( "Repository has not been initialized properly" );
            }
            session.save();
            return null;
          }
        } );
      }
    } );
  }

  public Boolean doesMetadataExists( final String metadataProperty ) {
    try {
      return (Boolean) txnTemplate.execute( new TransactionCallback() {
        @Override
        public Object doInTransaction( TransactionStatus status ) {
          return adminJcrTemplate.execute( new JcrCallback() {
            @Override
            public Object doInJcr( Session session ) throws IOException, RepositoryException {
              PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
              String absPath = ServerRepositoryPaths.getPentahoRootFolderPath();
              RepositoryFile rootFolder =
                  JcrRepositoryFileUtils.getFileByAbsolutePath( session, absPath, pathConversionHelper, null, false,
                      null );
              if ( rootFolder != null ) {
                Map<String, Serializable> metadataMap =
                    JcrRepositoryFileUtils.getFileMetadata( session, rootFolder.getId() );
                for ( Entry<String, Serializable> metadataEntry : metadataMap.entrySet() ) {
                  if ( metadataEntry.getKey().equals( metadataProperty ) ) {
                    return (Boolean) metadataEntry.getValue();
                  }
                }
              }
              return false;
            }
          } );
        }
      } );
    } catch ( Throwable th ) {
      th.printStackTrace();
      return false;
    }
  }

}
