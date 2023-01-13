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
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

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
