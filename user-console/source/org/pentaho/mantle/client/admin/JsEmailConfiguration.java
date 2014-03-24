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

  public final String getJSONString() {
    return new JSONObject( this ).toString();
  }

  public static final JsEmailConfiguration parseJsonString( final String jsonString ) {
    return (JsEmailConfiguration) parseEmailConfig( JsonUtils.escapeJsonForEval( jsonString ) );
  }

  private static final native JavaScriptObject parseEmailConfig( String json )
  /*-{
    var obj = eval('(' + json + ')');
    return obj;
  }-*/;
}
