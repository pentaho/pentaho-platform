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


package org.pentaho.platform.api.email;

public interface IEmailConfiguration {
  public boolean isAuthenticate();

  public void setAuthenticate( final boolean authenticate );

  public boolean isDebug();

  public void setDebug( final boolean debug );

  public String getDefaultFrom();

  public void setDefaultFrom( final String defaultFrom );

  public String getFromName();

  public void setFromName( String fromName );

  public String getSmtpHost();

  public void setSmtpHost( final String smtpHost );

  public Integer getSmtpPort();

  public void setSmtpPort( final Integer smtpPort );

  public String getSmtpProtocol();

  public void setSmtpProtocol( final String smtpProtocol );

  public String getUserId();

  public void setUserId( final String userId );

  public String getPassword();

  public void setPassword( final String password );

  public boolean isUseSsl();

  public void setUseSsl( final boolean useSsl );

  public boolean isUseStartTls();

  public void setUseStartTls( final boolean useStartTls );

  public boolean isSmtpQuitWait();

  public void setSmtpQuitWait( final boolean smtpQuitWait );

  public String getAuthMechanism();

  public void setAuthMechanism( final String authMechanism );

  public String getClientId();

  public void setClientId( final String clientId );

  public String getClientSecret();

  public void setClientSecret( final String clientSecret );

  public String getTokenUrl();

  public void setTokenUrl( final String tokenUrl );

  public String getScope();

  public void setScope( final String scope );

  public String getGrantType();

  public void setGrantType( final String grantType );

  public String getRefreshToken();

  public void setRefreshToken( final String refreshToken );

  public String getAuthorizationCode();

  public void setAuthorizationCode( final String authorizationCode );

  public String getRedirectUri();

  public void setRedirectUri( final String redirectUri );
}
