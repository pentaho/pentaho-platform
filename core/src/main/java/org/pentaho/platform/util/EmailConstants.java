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


package org.pentaho.platform.util;

public final class EmailConstants {

  private EmailConstants() { }

  //Authentication Types
  public static final String AUTH_TYPE_XOAUTH2 = "XOAUTH2";
  public static final String AUTH_TYPE_BASIC = "basic";
  public static final String AUTH_TYPE_NO_AUTH = "no_auth";

  // Grant Types
  public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
  public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  // Protocols
  public static final String PROTOCOL_GRAPH_API = "graph_api";
  public static final String PROTOCOL_SMTP = "SMTP";
  public static final String PROTOCOL_SMTPS = "SMTPS";
}
