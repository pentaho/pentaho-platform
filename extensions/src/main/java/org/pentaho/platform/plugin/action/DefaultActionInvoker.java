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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.Serializable;
import java.util.Map;

/**
 * A concrete implementation of the {@link IActionInvoker} interface that invokes the {@link IAction} locally.
 */
public class DefaultActionInvoker implements IActionInvoker {

  private static final Log logger = LogFactory.getLog( DefaultActionInvoker.class );

  /**
   * Gets the stream provider from the {@code INVOKER_STREAMPROVIDER,} or builds it from the input file and output
   * dir {@link Map} values. Returns {@code null} if information needed to build the stream provider is not present in
   * the {@code map}, which is perfectly ok for some {@link org.pentaho.platform.api.action.IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link org.pentaho.platform.api.action.IAction}
   * @return a {@link IBackgroundExecutionStreamProvider} represented in the {@code params} {@link Map}
   */
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
        boolean autoCreateUniqueFilename = params.get( ActionUtil.INVOKER_AUTO_CREATE_UNIQUE_FILENAME ) == null || params.get(
          ActionUtil.INVOKER_AUTO_CREATE_UNIQUE_FILENAME ).toString().equalsIgnoreCase( "true" );
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
   * Invokes the provided {@link IAction} as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  @Override
  public IActionInvokeStatus runInBackground( final IAction actionBean, final String actionUser, final
    Map<String, Serializable> params ) throws Exception {
    ActionUtil.prepareMap( params );
    // call getStreamProvider, in addition to creating the provider, this method also adds values to the map that
    // serialize the stream provider and make it possible to deserialize and recreate it for remote execution.
    getStreamProvider( params );
    return runInBackgroundImpl( actionBean, actionUser, params );
  }

  /**
   * Invokes the provided {@link IAction} locally as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected IActionInvokeStatus runInBackgroundImpl( final IAction actionBean, final String actionUser, final
    Map<String, Serializable> params ) throws Exception {

    if ( actionBean == null || params == null ) {
      throw new ActionInvocationException( Messages.getInstance().getCantInvokeNullAction() );
    }

    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getRunningInBackgroundLocally( actionBean.getClass().getName(), params ) );
    }

    // set the locale, if not already set
    if ( params.get( LocaleHelper.USER_LOCALE_PARAM ) == null || StringUtils.isEmpty(
      params.get( LocaleHelper.USER_LOCALE_PARAM ).toString() ) ) {
      params.put( LocaleHelper.USER_LOCALE_PARAM, LocaleHelper.getLocale() );
    }

    // remove the scheduling infrastructure properties
    ActionUtil.removeFromMap( params, ActionUtil.INVOKER_ACTIONCLASS );
    ActionUtil.removeFromMap( params, ActionUtil.INVOKER_ACTIONID );
    ActionUtil.removeFromMap( params, ActionUtil.INVOKER_ACTIONUSER );
    // build the stream provider
    final IBackgroundExecutionStreamProvider streamProvider = getStreamProvider( params );
    ActionUtil.removeFromMap( params, ActionUtil.INVOKER_STREAMPROVIDER );
    ActionUtil.removeFromMap( params, ActionUtil.INVOKER_UIPASSPARAM );

    final ActionRunner actionBeanRunner = new ActionRunner( actionBean, actionUser, params, streamProvider );
    final ActionInvokeStatus status = new ActionInvokeStatus();

    boolean requiresUpdate = false;
    if ( ( StringUtil.isEmpty( actionUser ) ) || ( actionUser.equals( "system session" ) ) ) { //$NON-NLS-1$
      // For now, don't try to run quartz jobs as authenticated if the user
      // that created the job is a system user. See PPP-2350
      requiresUpdate = SecurityHelper.getInstance().runAsAnonymous( actionBeanRunner );
    } else {
      try {
        requiresUpdate = SecurityHelper.getInstance().runAsUser( actionUser, actionBeanRunner );
      } catch ( final Throwable t ) {
        status.setThrowable( t );
      }
    }
    status.setRequiresUpdate( requiresUpdate );

    return status;
  }
}
