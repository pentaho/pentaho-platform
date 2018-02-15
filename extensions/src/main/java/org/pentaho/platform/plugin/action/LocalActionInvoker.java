/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.scheduler2.action.DefaultActionInvoker;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.Serializable;
import java.util.Map;

/**
 * A more specific implementation of {@link DefaultActionInvoker} for use within a worker node, that massages the
 * param {@link Map} keys such that they are generic, and not scheduler specific.
 */
public class LocalActionInvoker extends DefaultActionInvoker {

  private static final Log logger = LogFactory.getLog( org.pentaho.platform.plugin.action.LocalActionInvoker.class );

  /**
   * Gets the stream provider from the {@code INVOKER_STREAMPROVIDER} key within the {@code params} {@link Map} or
   * builds it from the input file and output dir {@link Map} values. Returns {@code null} if information needed to
   * build the stream provider is not present in the {@code map}, which is perfectly ok for some
   * {@link org.pentaho.platform.api.action.IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link org.pentaho.platform.api.action.IAction}
   * @return a {@link IBackgroundExecutionStreamProvider} represented in the {@code params} {@link Map}
   */
  @Override
  protected IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

    if ( params == null ) {
      logger.warn( Messages.getInstance().getMapNullCantReturnSp() );
      return null;
    }
    IBackgroundExecutionStreamProvider streamProvider = null;

    final Object objsp = params.get( ActionUtil.INVOKER_STREAMPROVIDER );
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      streamProvider = (IBackgroundExecutionStreamProvider) objsp;
      if ( streamProvider instanceof RepositoryFileStreamProvider ) {
        params.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, ( (RepositoryFileStreamProvider) streamProvider )
          .getInputFilePath() );
        params.put( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, ( (RepositoryFileStreamProvider)
          streamProvider ).getOutputFilePath() );
        params.put( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, ( (RepositoryFileStreamProvider)
          streamProvider ).autoCreateUniqueFilename() );
      }
    } else {
      final String inputFile = params.get( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE ) == null ? null : params.get(
        ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE ).toString();
      final String outputFilePattern = params.get( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ) == null
        ? null : params.get( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ).toString();
      boolean hasInputFile = !StringUtils.isEmpty( inputFile );
      boolean hasOutputPattern = !StringUtils.isEmpty( outputFilePattern );
      if ( hasInputFile && hasOutputPattern ) {
        boolean autoCreateUniqueFilename = params.get( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME ) == null
          || params.get( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME ).toString().equalsIgnoreCase( "true" );
        streamProvider = new RepositoryFileStreamProvider( inputFile, outputFilePattern, autoCreateUniqueFilename );
        // put in the map for future lookup
        params.put( ActionUtil.INVOKER_STREAMPROVIDER, streamProvider );
      } else {
        if ( logger.isWarnEnabled() ) {
          logger.warn( Messages.getInstance().getMissingParamsCantReturnSp( String.format( "%s, %s",
            ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ),
            params ) ); //$NON-NLS-1$
        }
      }
    }
    return streamProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IActionInvokeStatus invokeAction( final IAction actionBean,
                                           final String actionUser,
                                           final Map<String, Serializable> params ) throws Exception {
    ActionUtil.prepareMap( params );
    // call getStreamProvider, in addition to creating the provider, this method also adds values to the map that
    // serialize the stream provider and make it possible to deserialize and recreate it for remote execution.
    getStreamProvider( params );
    return super.invokeAction( actionBean, actionUser, params );
  }
}
