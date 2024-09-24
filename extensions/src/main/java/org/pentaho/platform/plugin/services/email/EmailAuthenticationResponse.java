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

package org.pentaho.platform.plugin.services.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.pentaho.platform.api.email.IEmailAuthenticationResponse;

public class EmailAuthenticationResponse implements IEmailAuthenticationResponse {
  @JsonProperty( "access_token" )
  private String accessToken;

  @JsonProperty( "token_type" )
  private String tokenType;

  @JsonProperty( "expires_in" )
  private Integer expiresIn;

  @JsonProperty( "ext_expires_in" )
  private Integer extExpiresIn;

  @JsonProperty( "refresh_token" )
  private String refreshToken;

  @JsonProperty( "scope" )
  private String scope;

  @JsonProperty( "id_token" )
  private String idToken;

  public String getAccessToken() {
    return accessToken == null ? "" : accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

}
