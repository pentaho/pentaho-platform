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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import java.util.List;

public class CsrfToken {

  private String header;
  private String token;
  private String parameter;

  private List<String> cookies;

  CsrfToken( String header, String parameter, String token, List<String> cookies ) {
    this.header = header;
    this.token = token;
    this.parameter = parameter;
    this.cookies = cookies;
  }

  public String getHeader() {
    return this.header;
  }

  public String getParameter() {
    return this.parameter;
  }

  public String getToken() {
    return this.token;
  }

  public List<String> getCookies() {
    return this.cookies;
  }
}
