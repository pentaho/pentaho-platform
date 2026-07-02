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


package org.pentaho.platform.uifoundation.component;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.HttpUtil;

import java.util.List;

public class HtmlComponent extends BaseUIComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -7404173000559758744L;

  // TODO sbarkdull convert these 2 TYPE_* to enumerated type
  public static final int TYPE_URL = 1;

  public static final int TYPE_SOLUTION_FILE = 2;

  private String location;

  private String errorMessage;

  private int type;

  private static final Log logger = LogFactory.getLog( HtmlComponent.class );

  @Override
  public Log getLogger() {
    return HtmlComponent.logger;
  }

  public HtmlComponent( final int type, final String location, final String errorMessage,
      final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, null );
    this.type = type;
    this.location = location;
    this.errorMessage = errorMessage;
  }

  @Override
  public boolean validate() {
    return true;
  }

  @Override
  public String getContent( final String mimeType ) {
    if ( "text/html".equals( mimeType ) ) { //$NON-NLS-1$
      if ( type == HtmlComponent.TYPE_URL ) {
        return getUrl( location );
      } else if ( type == HtmlComponent.TYPE_SOLUTION_FILE ) {
        return getFile( location );
      }
    }
    return null;
  }

  private String getFile( final String solutionPath ) {
    IActionSequenceResource resource =
        new ActionSequenceResource( "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/html", solutionPath ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      byte[] bytes =
          IOUtils.toByteArray( resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() ) );
      return new String( bytes, LocaleHelper.getSystemEncoding() );
    } catch ( Exception e ) {
      if ( errorMessage != null ) {
        return errorMessage;
      } else {
        error( Messages.getInstance().getErrorString( "Html.ERROR_0001_COULD_NOT_GET_CONTENT", solutionPath ) ); //$NON-NLS-1$
        return Messages.getInstance().getErrorString( "Html.ERROR_0001_COULD_NOT_GET_CONTENT", solutionPath ); //$NON-NLS-1$
      }
    }
  }

  private String getUrl( final String url ) {
    StringBuffer content = new StringBuffer();
    try {
      // check to see if this URL failed before thia session
      if ( ( getSession() != null )
          && ( getSession().getAttribute( "pentaho-HtmlComponent-failed-url-" + url ) != null ) ) { //$NON-NLS-1$
        return errorMessage;
      }
      if ( BaseUIComponent.debug ) {
        debug( Messages.getInstance().getString( "Html.DEBUG_GETTING_CONTENT", url ) ); //$NON-NLS-1$
      }
      if ( HttpUtil.getURLContent( url, content ) ) {
        return content.toString();
      } else {
        getSession().setAttribute( "pentaho-HtmlComponent-failed-url-" + url, "" ); //$NON-NLS-1$ //$NON-NLS-2$
        return errorMessage;
      }
    } catch ( Exception e ) {

      if ( errorMessage != null ) {
        return errorMessage;
      } else {
        error( Messages.getInstance().getErrorString( "Html.ERROR_0001_COULD_NOT_GET_CONTENT", url ) ); //$NON-NLS-1$
        return Messages.getInstance().getErrorString( "Html.ERROR_0001_COULD_NOT_GET_CONTENT", url ); //$NON-NLS-1$
      }
    }

  }

}
