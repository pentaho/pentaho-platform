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

package org.pentaho.platform.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.Messages;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ActionUtil {

  private static final Log logger = LogFactory.getLog( ActionUtil.class );

  public static final String QUARTZ_ACTIONCLASS = "ActionAdapterQuartzJob-ActionClass"; //$NON-NLS-1$
  public static final String QUARTZ_ACTIONUSER = "ActionAdapterQuartzJob-ActionUser"; //$NON-NLS-1$
  public static final String QUARTZ_ACTIONID = "ActionAdapterQuartzJob-ActionId"; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER = "ActionAdapterQuartzJob-StreamProvider"; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER_INPUT_FILE =
    "ActionAdapterQuartzJob-StreamProvider-InputFile"; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER_INLINE_INPUT_FILE = "input file ="; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER_INLINE_OUTPUT_FILE = ":output file="; //$NON-NLS-1$
  public static final String QUARTZ_UIPASSPARAM = "uiPassParam"; //$NON-NLS-1$
  public static final String QUARTZ_LINEAGE_ID = "lineage-id"; //$NON-NLS-1$
  public static final String QUARTZ_RESTART_FLAG = "ActionAdapterQuartzJob-Restart"; //$NON-NLS-1$
  public static final String QUARTZ_AUTO_CREATE_UNIQUE_FILENAME = "autoCreateUniqueFilename"; //$NON-NLS-1$
  public static final String QUARTZ_APPEND_DATE_FORMAT = "appendDateFormat"; //$NON-NLS-1$

  public static final String INVOKER_ACTIONPARAMS = "actionParams";
  public static final String INVOKER_ACTIONCLASS = "actionClass"; //$NON-NLS-1$
  public static final String INVOKER_ACTIONUSER = "actionUser"; //$NON-NLS-1$
  public static final String INVOKER_ACTIONID = "actionId"; //$NON-NLS-1$
  public static final String INVOKER_UUID = "UUID"; //$NON-NLS-1$
  public static final String INVOKER_CONTENT_TYPE = "contentType"; //$NON-NLS-1$
  public static final String INVOKER_STATUS = "status"; //$NON-NLS-1$
  public static final String INVOKER_STREAMPROVIDER = "streamProvider"; //$NON-NLS-1$
  public static final String INVOKER_STREAMPROVIDER_INPUT_FILE = "inputFile"; //$NON-NLS-
  public static final String INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN = "outputFilePattern"; //$
  public static final String INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME = QUARTZ_AUTO_CREATE_UNIQUE_FILENAME;
  public static final String INVOKER_UIPASSPARAM = QUARTZ_UIPASSPARAM;
  public static final String INVOKER_RESTART_FLAG = "restart"; //$NON-NLS-1$
  public static final String INVOKER_SESSION = "::session"; //$NON-NLS-1$
  public static final String INVOKER_ASYNC_EXEC = "async"; //$NON-NLS-1$
  public static final String INVOKER_DEFAULT_ASYNC_EXEC_VALUE = "true"; //$NON-NLS-1$
  public static final String INVOKER_SYNC_VALUE = "false";

  public static final String WORK_ITEM_UID = "workItemUid"; //$NON-NLS-1$
  public static final String WORK_ITEM_NAME = "workItemName"; //$NON-NLS-1$

  /**
   * Regex representing characters that are allowed in the work item uid (in compliance with chronos job names).
   */
  private static final String WORK_ITEM_UID_INVALID_CHARS = "[^\\w\\\\-]+";

  private static final int WORK_ITEM_LENGTH_LIMIT = 1000;

  private static final Map<String, String> KEY_MAP;

  static {
    KEY_MAP = new HashMap<>();
    KEY_MAP.put( ActionUtil.QUARTZ_ACTIONCLASS, ActionUtil.INVOKER_ACTIONCLASS );
    KEY_MAP.put( ActionUtil.QUARTZ_ACTIONUSER, ActionUtil.INVOKER_ACTIONUSER );
    KEY_MAP.put( ActionUtil.QUARTZ_ACTIONID, ActionUtil.INVOKER_ACTIONID );
    KEY_MAP.put( ActionUtil.QUARTZ_STREAMPROVIDER, ActionUtil.INVOKER_STREAMPROVIDER );
    KEY_MAP.put( ActionUtil.QUARTZ_STREAMPROVIDER_INPUT_FILE, ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE );
    KEY_MAP.put( ActionUtil.QUARTZ_RESTART_FLAG, ActionUtil.INVOKER_RESTART_FLAG );
  }

  /**
   * Removes the provided {@code key} from the {@code params} {@link Map} , if it exists, as well as its
   * "equivalent", as defined within the {@code KEY_MAP}. If the provided {@code key} is a key within the {@code
   * KEY_MAP}, its "equivalent" is the value. If the provided {@code key} is a value within the {@code KEY_MAP}, its
   * "equivalent" is the key.<br><br>
   * Example: Given a {@code KEY_MAP} that looks like this:<br>
   *   <ul><li>firstName=first.name</li>
   *   <li>lastName=last.name</li>
   *   <li>addr=address</li>
   *   <li>person.age=age</li></ul>
   * a call to {@code removeKeyFromMap( map, "firstName")} will attempt to remove the "firstName" and "first.name" keys
   * from the provided map. Likewise, a call to  {@code removeKeyFromMap( map, "last.name")} will attempt to remove the
   * "last.name" and "lastName" keys from the provided map.
   *
   * @param params the {@link Map} from which keys are being removed
   * @param key the {@code String} key being removed from the map
   */
  public static void removeKeyFromMap( final Map<String, ?> params, final String key ) {
    if ( params == null || key == null ) {
      logger.debug( "Map or key are null, cannot remove." );
      return;
    }
    // remove the key itself
    params.remove( key );

    // also remove the keys equivalent in the KEY_MAP, whether its a key or a value itself
    if ( KEY_MAP.containsKey( key ) ) {
      params.remove( KEY_MAP.get( key ) );
    } else if ( KEY_MAP.containsValue( key ) ) {
      final Iterator<Map.Entry<String, String>> keyMapIter = KEY_MAP.entrySet().iterator();
      while ( keyMapIter.hasNext() ) {
        final Map.Entry<String, String> entry = keyMapIter.next();
        final String keyMapKey = entry.getKey();
        final String keyMapValue = entry.getValue();
        if ( key.equals( keyMapValue ) ) {
          params.remove( keyMapKey );
          // the KEY_MAP is built by us in a controlled fashion, therefore we know that once we've found one matching
          // value, we are done
          break;
        }
      }
    }
  }

  /**
   * Prepares the {@code params} {@link Map} for action invocation, adding appropriate keys that are not scheduler
   * specific, so that the action invocation code can operate on these mapped non-scheduler specific keys, rather than
   * any keys defined within the scheduler. The corresponding entries that use the scheduler related keys are removed.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   */
  public static void prepareMap( final Map<String, Serializable> params ) {
    if ( params == null ) {
      return;
    }

    final Map<String, Serializable> replaced = new HashMap<>();

    for ( final Map.Entry<String, Serializable> ent : params.entrySet() ) {
      final String key = ent.getKey();
      final Serializable value = params.get( key );
      final String altKey = KEY_MAP.get( key );

      replaced.put( altKey == null ? key : altKey, value );
    }

    params.clear();
    params.putAll( replaced );
  }

  /**
   * Looks up the work item name within the {@link Map}. If available, the value is returned,
   * otherwise a new name is generated and returned.
   *
   * @param params a {@link Map} that may contain the name
   * @return work item name from the {@link Map} or a new one, or {@code null} in the absence of
   * the {@link IWorkItemLifecycleEventPublisher} bean
   */
  public static String extractName( final Map<String, Serializable> params ) {
    final IWorkItemLifecycleEventPublisher publisher = PentahoSystem.get( IWorkItemLifecycleEventPublisher.class );
    // if the published bean is null, we do not want to generate the work item name, as this is a worker nodes
    // concept, and we do not want any worker nodes related information in the logs. Given that
    // IWorkItemLifecycleEventPublisher only exists when worker nodes are enabled, we can use the existence of this
    // bean to determine whether to generate the work item UID or not
    if ( publisher == null ) {
      return null;
    }
    if ( params == null ) {
      return generateWorkItemName( params );
    }

    String name = (String) params.get( WORK_ITEM_NAME );
    if ( name == null ) {
      name = generateWorkItemName( params );
      params.put( WORK_ITEM_NAME, name );
    }

    return name;
  }

  /**
   * @param params a {@link Map} containing action/work item related attributes, in particular {@code inputFile} and
   *               {@code actionUser}, which are both used for the purpose of generating the uid.
   * @return a name for the work item
   * @see {@link #generateWorkItemName(String, String)}
   */
  public static String generateWorkItemName( final Map<String, Serializable> params ) {
    if ( params == null ) {
      return generateWorkItemName( null, null );
    }
    // at the time this method is called, the quartz specific map keys may not have been mapped to their
    // corresponding action keys (see KEY_MAP), fall back on the quartz key, if the action key cannot be found
    final String userName = Optional.ofNullable( (String) params.get( INVOKER_ACTIONUSER ) ).orElse( (String) params
      .get( QUARTZ_ACTIONUSER ) );
    String inputFilePath = Optional.ofNullable( (String) params.get( INVOKER_STREAMPROVIDER_INPUT_FILE ) )
      .orElse( (String) params.get( QUARTZ_STREAMPROVIDER_INPUT_FILE ) );

    // if we still cannot find the inputFile, we have one last attempt: check for inline passing of input and output files,
    // represented by 'ActionAdapterQuartzJob-StreamProvider -> input file = <path><colon>output file=<path>
    if ( StringUtil.isEmpty( inputFilePath ) && isInlinePassingOfInputOnStreamProvider( params ) ) {
      inputFilePath = getInlineInputFileOnStreamProvider( params );
    }

    return generateWorkItemName( inputFilePath, userName );
  }

  public static boolean isInlinePassingOfInputOnStreamProvider( final Map<String, Serializable> params ) {
    return params != null && getStreamProviderContent( params ).contains( QUARTZ_STREAMPROVIDER_INLINE_INPUT_FILE );
  }

  public static String getInlineInputFileOnStreamProvider( final Map<String, Serializable> params ) {

    try {

      String streamProviderContent = getStreamProviderContent( params );

      int startIdx = streamProviderContent.indexOf( QUARTZ_STREAMPROVIDER_INLINE_INPUT_FILE ) + QUARTZ_STREAMPROVIDER_INLINE_INPUT_FILE.length();
      int endIdx = streamProviderContent.indexOf( QUARTZ_STREAMPROVIDER_INLINE_OUTPUT_FILE );

      if ( startIdx >= 0 && endIdx >= 0 && endIdx > startIdx ) {
        return streamProviderContent.substring( startIdx, endIdx ).trim();
      }

    } catch ( Throwable t ) {
      logger.error( t );
    }

    return null;
  }

  public static String getStreamProviderContent( final Map<String, Serializable> params ) {
    return ( params.containsKey( QUARTZ_STREAMPROVIDER )
            ? params.get( QUARTZ_STREAMPROVIDER ) : params.containsKey( INVOKER_STREAMPROVIDER )
            ? params.get( INVOKER_STREAMPROVIDER ) : StringUtils.EMPTY ).toString().trim();
  }

  /**
   * Returns a name for a work item which includes the input file name (derived from {@code inputFilePath}) and
   * {@code user} that executed it, in the following format: %input file name%-%user%, stripping any
   * invalid characters.
   *
   * This workItemName will be used for logging only
   *
   * @param inputFilePath the path of the input file of the action being invoked - optional
   * @param userName      the user executing the action
   * @return a name for the work item
   */
  public static String generateWorkItemName( final String inputFilePath,
                                            final String userName ) {

    if ( StringUtil.isEmpty( inputFilePath ) ) {
      logger.info( "Input file path is not provided." );
    }
    if ( StringUtil.isEmpty( userName ) ) {
      logger.info( "User name is not provided." );
    }

    String workItemName = String.format( "%s[%s]",
            Optional.ofNullable( inputFilePath ).orElse( StringUtils.EMPTY ),
            Optional.ofNullable( userName ).orElse( StringUtils.EMPTY ) );

    if ( workItemName.length() > WORK_ITEM_LENGTH_LIMIT ) {
      logger.info( String.format( "Work item name exceeds max character limit of %d: %d", WORK_ITEM_LENGTH_LIMIT,
              workItemName.length() ) );
    }
    return workItemName;
  }

  private static final long RETRY_COUNT = 6;
  private static final long RETRY_SLEEP_AMOUNT = 10000;

  /**
   * Returns the {@link Class} that corresponds to the provides {@code actionClassName} and {@code beanId}.
   *
   * @param actionClassName the name of the class being resolved
   * @param beanId          the beanId of the class being resolved
   * @return the {@link Class} that corresponds to the provides {@code actionClassName} and {@code beanId}
   * @throws PluginBeanException when the plugin required to resolve the bean class from the {@code beanId} cannot be
   *                             created
   * @throws Exception           when the required parameters are not provided
   */
  static Class<?> resolveActionClass( final String actionClassName, final String beanId ) throws
          PluginBeanException, ActionInvocationException {
    return resolveActionClass( actionClassName, beanId, true /* default retryBeanInstantiationIfFailed */ );
  }

  /**
   * Returns the {@link Class} that corresponds to the provides {@code actionClassName} and {@code beanId}.
   *
   * @param actionClassName the name of the class being resolved
   * @param beanId          the beanId of the class being resolved
   * @param retryBeanInstantiationIfFailed re-trigger bean instantiation attempt, should it fail for some reason
   * @return the {@link Class} that corresponds to the provides {@code actionClassName} and {@code beanId}
   * @throws PluginBeanException when the plugin required to resolve the bean class from the {@code beanId} cannot be
   *                             created
   * @throws Exception           when the required parameters are not provided
   */
  static Class<?> resolveActionClass( final String actionClassName, final String beanId, final boolean retryBeanInstantiationIfFailed ) throws
      PluginBeanException, ActionInvocationException {

    Class<?> clazz = null;

    if ( StringUtils.isEmpty( beanId ) && StringUtils.isEmpty( actionClassName ) ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
          "ActionUtil.ERROR_0001_REQUIRED_PARAM_MISSING", INVOKER_ACTIONCLASS, INVOKER_ACTIONID ) );
    }

    final long retryCount = ( retryBeanInstantiationIfFailed ? RETRY_COUNT : 1 );
    final long retrySleepCount = ( retryBeanInstantiationIfFailed ? RETRY_SLEEP_AMOUNT : 1 ); // millis

    for ( int i = 0; i < retryCount; i++ ) {
      try {
        if ( !StringUtils.isEmpty( beanId ) ) {
          IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
          clazz = pluginManager.loadClass( beanId );
          if ( clazz != null ) {
            return clazz;
          }
        }
        // we will execute this only if the beanId is not provided, or if the beanId cannot be resolved
        if ( !StringUtils.isEmpty( actionClassName ) ) {
          clazz = Class.forName( actionClassName );
          return clazz;
        }
      } catch ( Throwable t ) {
        try {
          Thread.sleep( retrySleepCount );
        } catch ( InterruptedException ie ) {
          logger.info( ie.getMessage(), ie );
        }
      }
    }

    // we have failed to locate the class for the actionClass
    // and we're giving up waiting for it to become available/registered
    // which can typically happen at system startup
    throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionUtil.ERROR_0002_FAILED_TO_CREATE_ACTION", StringUtils.isEmpty( beanId ) ? actionClassName : beanId ) );
  }

  /**
   * Returns an instance of {@link IAction} created from the provided parameters.
   *
   * @param actionClassName the name of the class being resolved
   * @param actionId        the is of the action which corresponds to some bean id
   * @return {@link IAction} created from the provided parameters.
   * @throws Exception when the {@link IAction} cannot be created for some reason
   */
  public static IAction createActionBean( final String actionClassName, final String actionId ) throws
          ActionInvocationException {
    return createActionBean( actionClassName, actionId, true /* default retryBeanInstantiationIfFailed */ );
  }

  /**
   * Returns an instance of {@link IAction} created from the provided parameters.
   *
   * @param actionClassName the name of the class being resolved
   * @param actionId        the is of the action which corresponds to some bean id
   * @return {@link IAction} created from the provided parameters.
   * @throws Exception when the {@link IAction} cannot be created for some reason
   */
  public static IAction createActionBean( final String actionClassName, final String actionId, final boolean retryBeanInstantiationIfFailed ) throws
      ActionInvocationException {
    Object actionBean = null;
    Class<?> actionClass = null;
    try {
      actionClass = resolveActionClass( actionClassName, actionId, retryBeanInstantiationIfFailed );
      actionBean = actionClass.newInstance();
    } catch ( final Exception e ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
          "ActionUtil.ERROR_0002_FAILED_TO_CREATE_ACTION", StringUtils.isEmpty( actionId ) ? ( actionClass == null
              ? "?" : actionClass.getName() ) : actionId, e ) );
    }

    if ( !( actionBean instanceof IAction ) ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
          "ActionUtil.ERROR_0003_ACTION_WRONG_TYPE", actionClass.getName(), IAction.class.getName() ) );
    }
    return (IAction) actionBean;
  }

  /**
   * Sends an email with the file representing the provided {@code filePath}  as an attachment. All information
   * needed to send the email (to, from, cc, bcc etc) is expected to be proviced in the {@code actionParams}
   * {@link Map}.
   *
   * @param actionParams a {@link Map} of parameters needed to send the email
   * @param params       a {@link Map} of parameter used to invoke the action
   * @param filePath     the path of the repository file that was generated when the action was invoked
   */
  public static void sendEmail( Map<String, Object> actionParams, Map<String, Serializable> params, String filePath ) {
    try {
      // if email is setup and we have tos, then do it
      Emailer emailer = new Emailer();
      if ( !emailer.setup() ) {
        // email not configured
        return;
      }

      if ( filePath != null ) {
        addAttachment( actionParams, params, filePath, emailer );
      }

      String to = (String) actionParams.get( "_SCH_EMAIL_TO" );
      String cc = (String) actionParams.get( "_SCH_EMAIL_CC" );
      String bcc = (String) actionParams.get( "_SCH_EMAIL_BCC" );
      if ( ( to == null || "".equals( to ) ) && ( cc == null || "".equals( cc ) )
          && ( bcc == null || "".equals( bcc ) ) ) {
        // no destination
        return;
      }
      emailer.setTo( to );
      emailer.setCc( cc );
      emailer.setBcc( bcc );

      String subject = (String) actionParams.get( "_SCH_EMAIL_SUBJECT" );
      if ( subject != null && !"".equals( subject ) ) {
        emailer.setSubject( subject );
      } else {
        emailer.setSubject( "Pentaho Scheduler" + ( emailer.getAttachmentName() != null ? " : " + emailer.getAttachmentName() : "" ) );
      }
      String message = (String) actionParams.get( "_SCH_EMAIL_MESSAGE" );
      if ( subject != null && !"".equals( subject ) ) {
        emailer.setBody( message );
      }
      emailer.send();
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
  }

  private static void addAttachment( Map<String, Object> actionParams, Map<String, Serializable> params,
                                     String filePath, Emailer emailer ) {
    IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
    RepositoryFile sourceFile = repo.getFile( filePath );
    // add metadata
    Map<String, Serializable> metadata = repo.getFileMetadata( sourceFile.getId() );
    String lineageId = (String) params.get( ActionUtil.QUARTZ_LINEAGE_ID );
    metadata.put( ActionUtil.QUARTZ_LINEAGE_ID, lineageId );
    repo.setFileMetadata( sourceFile.getId(), metadata );
    // send email
    SimpleRepositoryFileData data = repo.getDataForRead( sourceFile.getId(), SimpleRepositoryFileData.class );
    emailer.setAttachment( data.getInputStream() );
    emailer.setAttachmentName( "attachment" );
    String attachmentName = (String) actionParams.get( "_SCH_EMAIL_ATTACHMENT_NAME" );
    if ( !StringUtils.isEmpty( attachmentName ) ) {
      String extension = MimeHelper.getExtension( data.getMimeType(), ".bin" );
      emailer.setAttachmentName( attachmentName.endsWith( extension ) ?  attachmentName : attachmentName + extension );
    } else if ( data != null ) {
      String path = filePath;
      if ( path.endsWith( ".*" ) ) {
        path = path.replace( ".*", "" );
      }
      String extension = MimeHelper.getExtension( data.getMimeType(), ".bin" );
      path = path.substring( path.lastIndexOf( "/" ) + 1, path.length() );
      emailer.setAttachmentName( attachmentName.endsWith( extension ) ?  path : path + extension );
    }
    if ( data == null || data.getMimeType() == null || "".equals( data.getMimeType() ) ) {
      emailer.setAttachmentMimeType( "binary/octet-stream" );
    } else {
      emailer.setAttachmentMimeType( data.getMimeType() );
    }
  }

}
