/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
}
