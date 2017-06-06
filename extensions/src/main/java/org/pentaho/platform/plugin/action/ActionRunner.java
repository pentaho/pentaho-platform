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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IPostProcessingAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.ISourcesStreamEvents;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.output.FileContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ActionSequenceCompatibilityFormatter;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.scheduler2.quartz.SchedulerOutputPathResolver;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.beans.ActionHarness;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

public class ActionRunner implements Callable<Boolean> {

  private static final Log logger = LogFactory.getLog( ActionRunner.class );

  private Map<String, Serializable> params;
  private IAction actionBean;
  private IBackgroundExecutionStreamProvider streamProvider;
  private String actionUser;

  private String outputFilePath = null;
  private Object lock = new Object();

  public ActionRunner( final IAction actionBean, final String actionUser, final Map<String, Serializable> params, final
    IBackgroundExecutionStreamProvider streamProvider ) {
    this.actionBean = actionBean;
    this.actionUser = actionUser;
    this.params = params;
    this.streamProvider = streamProvider;
  }

  public Boolean call() throws ActionInvocationException {
    try {
      return callImpl();
    } catch ( final Throwable t ) {
      // ensure that the main thread isn't blocked on lock
      synchronized ( lock ) {
        lock.notifyAll();
      }

      // We should not distinguish between checked and unchecked exceptions here. All job execution failures
      // should result in a rethrow of the exception
      throw new ActionInvocationException( Messages.getInstance().getActionFailedToExecute( actionBean //$NON-NLS-1$
        .getClass().getName() ), t );
    }
  }

  private Boolean callImpl() throws Exception {
    final Object locale = params.get( LocaleHelper.USER_LOCALE_PARAM );
    if ( locale instanceof Locale ) {
      LocaleHelper.setLocaleOverride( (Locale) locale );
    } else {
      LocaleHelper.setLocaleOverride( new Locale( (String) locale ) );
    }
    // sync job params to the action bean
    ActionHarness actionHarness = new ActionHarness( actionBean );
    boolean updateJob = false;

    final Map<String, Object> actionParams = new HashMap<String, Object>();
    actionParams.putAll( params );
    if ( streamProvider != null ) {
      actionParams.put( "inputStream", streamProvider.getInputStream() );
    }
    actionHarness.setValues( actionParams, new ActionSequenceCompatibilityFormatter() );

    if ( actionBean instanceof IVarArgsAction ) {
      actionParams.remove( "inputStream" );
      actionParams.remove( "outputStream" );
      ( (IVarArgsAction) actionBean ).setVarArgs( actionParams );
    }

    boolean waitForFileCreated = false;
    OutputStream stream = null;

    if ( streamProvider != null ) {
      actionParams.remove( "inputStream" );
      if ( actionBean instanceof IStreamingAction ) {
        streamProvider.setStreamingAction( (IStreamingAction) actionBean );
      }

      // BISERVER-9414 - validate that output path still exist
      SchedulerOutputPathResolver resolver =
        new SchedulerOutputPathResolver( streamProvider.getOutputPath(), actionUser );
      String outputPath = resolver.resolveOutputFilePath();
      actionParams.put( "useJcr", Boolean.TRUE );
      actionParams.put( "jcrOutputPath", outputPath.substring( 0, outputPath.lastIndexOf( "/" ) ) );

      if ( !outputPath.equals( streamProvider.getOutputPath() ) ) {
        streamProvider.setOutputFilePath( outputPath ); // set fallback path
        updateJob = true; // job needs to be deleted and recreated with the new output path
      }

      stream = streamProvider.getOutputStream();
      if ( stream instanceof ISourcesStreamEvents ) {
        ( (ISourcesStreamEvents) stream ).addListener( new IStreamListener() {
          public void fileCreated( final String filePath ) {
            synchronized ( lock ) {
              outputFilePath = filePath;
              lock.notifyAll();
            }
          }
        } );
        waitForFileCreated = true;
      }
      actionParams.put( "outputStream", stream );
      // The lineage_id is only useful for the metadata and not needed at this level see PDI-10171
      actionParams.remove( ActionUtil.QUARTZ_LINEAGE_ID );
      actionHarness.setValues( actionParams );
    }

    actionBean.execute();

    if ( stream != null ) {
      IOUtils.closeQuietly( stream );
    }

    if ( waitForFileCreated ) {
      synchronized ( lock ) {
        if ( outputFilePath == null ) {
          lock.wait();
        }
      }
      ActionUtil.sendEmail( actionParams, params, outputFilePath );
    }
    if ( actionBean instanceof IPostProcessingAction ) {
      closeContentOutputStreams( (IPostProcessingAction) actionBean );
      markContentAsGenerated( (IPostProcessingAction) actionBean );
    }
    return updateJob;
  }

  private void closeContentOutputStreams( IPostProcessingAction actionBean ) {
    for ( IContentItem contentItem : actionBean.getActionOutputContents() ) {
      contentItem.closeOutputStream();
    }
  }

  private void markContentAsGenerated( IPostProcessingAction actionBean ) {
    IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
    String lineageId = (String) params.get( ActionUtil.QUARTZ_LINEAGE_ID );
    for ( IContentItem contentItem : actionBean.getActionOutputContents() ) {
      RepositoryFile sourceFile = getRepositoryFileSafe( repo, contentItem.getPath() );
      // add metadata if we have access and we have file
      if ( sourceFile != null ) {
        Map<String, Serializable> metadata = repo.getFileMetadata( sourceFile.getId() );
        metadata.put( ActionUtil.QUARTZ_LINEAGE_ID, lineageId );
        repo.setFileMetadata( sourceFile.getId(), metadata );
      } else {
        String fileName = getFSFileNameSafe( contentItem );
        logger.warn( Messages.getInstance().getSkipRemovingOutputFile( fileName ) );
      }
    }
  }

  private RepositoryFile getRepositoryFileSafe( IUnifiedRepository repo, String path ) {
    try {
      return repo.getFile( path );
    } catch ( Exception e ) {
      logger.debug( Messages.getInstance().getCannotGetRepoFile( path, e.getMessage() ) );
      return null;
    }
  }

  private String getFSFileNameSafe( IContentItem contentItem ) {
    if ( contentItem instanceof FileContentItem ) {
      return ( (FileContentItem) contentItem ).getFile().getName();
    }
    return null;
  }
}
