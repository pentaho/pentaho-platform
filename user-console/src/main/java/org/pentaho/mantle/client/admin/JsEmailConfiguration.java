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


package org.pentaho.mantle.client.admin;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONObject;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class JsEmailConfiguration extends JavaScriptObject {
  protected JsEmailConfiguration() {
  }

  public final native boolean isAuthenticate() /*-{ return this.authenticate; }-*/; //

  public final native boolean isDebug() /*-{ return this.debug; }-*/; //

  public final native String getDefaultFrom() /*-{ return this.defaultFrom; }-*/; //

  public final native String getFromName() /*-{ return this.fromName; }-*/; //

  public final native String getSmtpHost() /*-{ return this.smtpHost; }-*/; //

  public final native int getSmtpPort() /*-{ return this.smtpPort; }-*/; //

  public final native String getSmtpProtocol() /*-{ return this.smtpProtocol; }-*/; //

  public final native boolean isSmtpQuitWait() /*-{ return this.smtpQuitWait; }-*/; //

  public final native String getUserId() /*-{ return this.userId; }-*/; //

  public final native String getPassword() /*-{ return this.password; }-*/; //

  public final native boolean isUseSsl() /*-{ return this.useSsl; }-*/; //

  public final native boolean isUseStartTls() /*-{ return this.useStartTls; }-*/; //

  public final native void setAuthenticate( final boolean authenticate ) /*-{ this.authenticate = authenticate; }-*/; //

  public final native void setDebug( final boolean debug ) /*-{ this.debug = debug; }-*/; //

  public final native void setDefaultFrom( final String defaultFrom ) /*-{ this.defaultFrom = defaultFrom; }-*/; //

  public final native void setFromName( final String fromName ) /*-{ this.fromName = fromName; }-*/; //

  public final native void setSmtpHost( final String smtpHost ) /*-{ this.smtpHost = smtpHost; }-*/; //

  public final native void setSmtpPort( final int smtpPort ) /*-{ this.smtpPort = smtpPort; }-*/; //

  public final native void setSmtpProtocol( final String smtpProtocol ) /*-{ this.smtpProtocol = smtpProtocol; }-*/; //

  public final native void setSmtpQuitWait( final boolean smtpQuitWait ) /*-{ this.smtpQuitWait = smtpQuitWait; }-*/; //

  public final native void setUserId( final String userId ) /*-{ this.userId = userId; }-*/; //

  public final native void setPassword( final String password ) /*-{ this.password = password; }-*/; //

  public final native void setUseSsl( final boolean useSsl ) /*-{ this.useSsl = useSsl; }-*/; //

  public final native void setUseStartTls( final boolean useStartTls ) /*-{ this.useStartTls = useStartTls; }-*/; //

  public final native String getAuthMechanism() /*-{ return this.authMechanism; }-*/; //

  public final native void setAuthMechanism( final String authMechanism ) /*-{ this.authMechanism = authMechanism; }-*/; //

  public final native String getClientId() /*-{ return this.clientId; }-*/; //

  public final native void setClientId( final String clientId ) /*-{ this.clientId = clientId; }-*/; //

  public final native String getClientSecret() /*-{ return this.clientSecret; }-*/; //

  public final native void setClientSecret( final String clientSecret ) /*-{ this.clientSecret = clientSecret; }-*/; //

  public final native String getTokenUrl() /*-{ return this.tokenUrl; }-*/; //

  public final native void setTokenUrl( final String tokenUrl ) /*-{ this.tokenUrl = tokenUrl; }-*/; //

  public final native String getScope() /*-{ return this.scope; }-*/; //

  public final native void setScope( final String scope ) /*-{ this.scope = scope; }-*/; //

  public final native String getGrantType() /*-{ return this.grantType; }-*/; //

  public final native void setGrantType( final String grantType ) /*-{ this.grantType = grantType; }-*/; //

  public final native String getRefreshToken() /*-{ return this.refreshToken; }-*/; //

  public final native void setRefreshToken( final String refreshToken ) /*-{ this.refreshToken = refreshToken; }-*/; //

  public final native String getAuthorizationCode() /*-{ return this.authorizationCode; }-*/; //

  public final native void setAuthorizationCode( final String authorizationCode ) /*-{ this.authorizationCode = authorizationCode; }-*/; //

  public final native String getRedirectUri() /*-{ return this.redirectUri; }-*/; //

  public final native void setRedirectUri( final String redirectUri ) /*-{ this.redirectUri = redirectUri; }-*/; //

  public final String getJSONString() {
    return new JSONObject( this ).toString();
  }

  public static final JsEmailConfiguration parseJsonString( final String jsonString ) {
    return (JsEmailConfiguration) parseEmailConfig( JsonUtils.escapeJsonForEval( jsonString ) );
  }

  private static final native JavaScriptObject parseEmailConfig( String json )
    /*-{
      var obj = JSON.parse(json);
      return obj;
    }-*/;
}
