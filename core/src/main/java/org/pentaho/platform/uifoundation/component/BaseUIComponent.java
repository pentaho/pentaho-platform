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

package org.pentaho.platform.uifoundation.component;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IActionRequestHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.ui.IUIComponent;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public abstract class BaseUIComponent extends PentahoMessenger implements IUIComponent {

  private static final long serialVersionUID = -6768653568134000758L;

  public static final boolean debug = PentahoSystem.debug;

  protected HashMap xslProperties;

  protected HashMap contentTypes;

  private IActionRequestHandler requestHandler;

  private IPentahoSession userSession;

  @Override
  public abstract Log getLogger();

  private HashMap parameterProviders;

  protected IPentahoUrlFactory urlFactory;

  private String sourcePath;

  public void handleRequest( final OutputStream outputStream, final IActionRequestHandler actionRequestHandler,
      final String contentType, final HashMap requestParameterProviders ) throws IOException {

    this.parameterProviders = requestParameterProviders;
    this.requestHandler = actionRequestHandler;
    String content = getContent( contentType );
    if ( content != null ) {
      outputStream.write( content.getBytes( LocaleHelper.getSystemEncoding() ) );
    } else {
      error( Messages.getInstance().getString( "BaseUI.ERROR_0001_NO_CONTENT" ) ); //$NON-NLS-1$
    }
  }

  protected void setSourcePath( final String sourcePath ) {
    this.sourcePath = sourcePath;
  }

  protected String getSourcePath() {
    return sourcePath;
  }

  public void setUrlFactory( final IPentahoUrlFactory urlFactory ) {
    this.urlFactory = urlFactory;
  }

  public void setRequestHandler( final IActionRequestHandler actionRequestHandler ) {
    this.requestHandler = actionRequestHandler;
  }

  public BaseUIComponent( final IPentahoUrlFactory urlFactory, final List messages, final String sourcePath ) {
    super();
    this.urlFactory = urlFactory;
    xslProperties = new HashMap();
    contentTypes = new HashMap();
    setMessages( messages );
    parameterProviders = new HashMap();
    this.sourcePath = sourcePath;
  }

  public void setParameterProvider( final String name, final IParameterProvider parameterProvider ) {
    if ( parameterProviders == null ) {
      parameterProviders = new HashMap();
    }
    parameterProviders.put( name, parameterProvider );
  }

  public void setParameterProviders( final HashMap parameterProviders ) {
    this.parameterProviders = parameterProviders;
  }

  protected IPentahoUrlFactory getUrlFactory() {
    return urlFactory;
  }

  protected IActionRequestHandler getRequestHandler() {
    return requestHandler;
  }

  public HashMap getParameterProviders() {
    return parameterProviders;
  }

  public String getParameter( final String name, final String defaultValue ) {
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get( "options" ); //$NON-NLS-1$
    String value = null;
    if ( parameterProvider != null ) {
      value = parameterProvider.getStringParameter( name, null );
      if ( value != null ) {
        return value;
      }
    }
    parameterProvider = (IParameterProvider) parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
    if ( parameterProvider != null ) {
      value = parameterProvider.getStringParameter( name, null );
      if ( value != null ) {
        return value;
      }
    }
    if ( parameterProvider != null ) {
      value = parameterProvider.getStringParameter( name, null );
      if ( value != null ) {
        return value;
      }
    }
    return defaultValue;
  }

  public Object getObjectParameter( final String name, final Object defaultValue ) {
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get( "options" ); //$NON-NLS-1$
    Object value = null;
    if ( parameterProvider != null ) {
      value = parameterProvider.getParameter( name );
      if ( value != null ) {
        return value;
      }
    }
    parameterProvider = (IParameterProvider) parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
    if ( parameterProvider != null ) {
      value = parameterProvider.getParameter( name );
      if ( value != null ) {
        return value;
      }
    }
    if ( parameterProvider != null ) {
      value = parameterProvider.getParameter( name );
      if ( value != null ) {
        return value;
      }
    }
    return defaultValue;
  }

  public String[] getParameterAsArray( final String name ) {
    IParameterProvider parameterProvider = (IParameterProvider) parameterProviders.get( "options" ); //$NON-NLS-1$
    Object value;
    if ( parameterProvider != null ) {
      value = parameterProvider.getParameter( name );
      if ( value != null ) {
        return toStringArray( value );
      }
    }
    parameterProvider = (IParameterProvider) parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
    if ( parameterProvider != null ) {
      value = parameterProvider.getParameter( name );
      if ( value != null ) {
        return toStringArray( value );
      }
    }
    parameterProvider = (IParameterProvider) parameterProviders.get( IParameterProvider.SCOPE_SESSION );
    if ( parameterProvider != null ) {
      value = parameterProvider.getParameter( name );
      if ( value != null ) {
        return toStringArray( value );
      }
    }
    return ( new String[] {} );
  }

  private String[] toStringArray( final Object value ) {
    if ( value == null ) {
      return ( new String[] {} );
    }

    if ( value instanceof String[] ) {
      return ( (String[]) value );
    }

    return ( new String[] { value.toString() } );
  }

  protected IPentahoSession getSession() {
    return userSession;
  }

  public void setXsl( final String mimeType, final String xslName ) {
    contentTypes.put( mimeType, xslName );
  }

  public String getXsl( final String mimeType ) {
    return (String) contentTypes.get( mimeType );
  }

  public abstract boolean validate();

  /**
   * Set the userSession member, generate a Log Id, set the requestHandler, and validate the component's
   * configuration. NOTE: this method has several side effects not related to validation. could probably use some
   * refactoring
   * 
   * @param session
   * @param actionRequestHandler
   * @return boolean true if component configuration is valid, else false
   */
  public boolean validate( final IPentahoSession session, final IActionRequestHandler actionRequestHandler ) {
    this.userSession = session;
    this.genLogIdFromSession( session );
    this.requestHandler = actionRequestHandler;
    return validate();
  }

  public void setXslProperty( final String name, final String value ) {
    xslProperties.put( name, value );
  }

  public HashMap getXslProperties() {
    return xslProperties;
  }

  public abstract String getContent( String mimeType );

  public void done() {

  }

}
