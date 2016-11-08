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

package org.pentaho.platform.plugin.services.email;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.email.IEmailConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Properties;

/**
 * Bean which contains all the information for the email configuration
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
@XmlRootElement
public class EmailConfiguration implements Serializable, IEmailConfiguration {

  private static final long serialVersionUID = -7765357320116118032L;

  private boolean authenticate;
  private boolean debug;
  private String defaultFrom;
  private String fromName;
  private String smtpHost;
  private Integer smtpPort;
  private String smtpProtocol;
  private boolean smtpQuitWait;
  private String userId;
  private String password;
  private boolean useSsl;
  private boolean useStartTls;

  public EmailConfiguration() {
  }

  public EmailConfiguration( final boolean authenticate, final boolean debug, final String defaultFrom,
      final String fromName, final String smtpHost, final Integer smtpPort, final String smtpProtocol,
      final boolean smtpQuitWait, final String userId, final String password, final boolean useSsl,
      final boolean useStartTls ) {
    this.authenticate = authenticate;
    this.debug = debug;
    this.defaultFrom = defaultFrom;
    this.fromName = fromName;
    this.smtpHost = smtpHost;
    this.smtpPort = smtpPort;
    this.smtpProtocol = smtpProtocol;
    this.smtpQuitWait = smtpQuitWait;
    this.userId = userId;
    this.password = password;
    this.useSsl = useSsl;
    this.useStartTls = useStartTls;
  }

  public boolean isAuthenticate() {
    return authenticate;
  }

  public void setAuthenticate( final boolean authenticate ) {
    this.authenticate = authenticate;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug( final boolean debug ) {
    this.debug = debug;
  }

  public String getDefaultFrom() {
    return defaultFrom == null ? "" : defaultFrom;
  }

  public void setDefaultFrom( final String defaultFrom ) {
    this.defaultFrom = defaultFrom;
  }

  public String getFromName() {
    return fromName == null ? "" : fromName;
  }

  public void setFromName( final String fromName ) {
    this.fromName = fromName;
  }

  public String getSmtpHost() {
    return smtpHost == null ? "" : smtpHost;
  }

  public void setSmtpHost( final String smtpHost ) {
    this.smtpHost = smtpHost;
  }

  public Integer getSmtpPort() {
    return smtpPort == null ? Integer.MIN_VALUE : smtpPort;
  }

  public void setSmtpPort( final Integer smtpPort ) {
    this.smtpPort = smtpPort;
  }

  public String getSmtpProtocol() {
    return smtpProtocol == null ? "" : smtpProtocol;
  }

  public void setSmtpProtocol( final String smtpProtocol ) {
    this.smtpProtocol = smtpProtocol;
  }

  public String getUserId() {
    return userId == null ? "" : userId;
  }

  public void setUserId( final String userId ) {
    this.userId = userId;
  }

  public String getPassword() {
    return password == null ? "" : password;
  }

  public void setPassword( final String password ) {
    this.password = password;
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  public void setUseSsl( final boolean useSsl ) {
    this.useSsl = useSsl;
  }

  public boolean isUseStartTls() {
    return useStartTls;
  }

  public void setUseStartTls( final boolean useStartTls ) {
    this.useStartTls = useStartTls;
  }

  public boolean isSmtpQuitWait() {
    return smtpQuitWait;
  }

  public void setSmtpQuitWait( final boolean smtpQuitWait ) {
    this.smtpQuitWait = smtpQuitWait;
  }

  public Properties asProperties() {
    final Properties properties = new Properties();
    properties.setProperty( "mail.transport.protocol", getSmtpProtocol() );
    properties.setProperty( "mail.smtp.host", getSmtpHost() );
    properties.setProperty( "mail.smtp.port", getSmtpPort().toString() );
    properties.setProperty( "mail.smtp.host", getSmtpHost() );
    properties.setProperty( "mail.smtp.host", getSmtpHost() );
    properties.setProperty( "mail.smtp.host", getSmtpHost() );
    properties.setProperty( "mail.smtp.host", getSmtpHost() );
    return properties;
  }

  @Override
  public String toString() {
    return "authenticate='" + authenticate + '\'' + ", debug='" + debug + '\'' + ", defaultFrom='" + defaultFrom + '\''
        + ", fromName='" + fromName + '\'' + ", smtpHost='" + smtpHost + '\'' + ", smtpPort=" + smtpPort
        + ", smtpProtocol='" + smtpProtocol + '\'' + ", smtpQuitWait=" + smtpQuitWait + ", userId='" + userId + '\''
        + ", password='" + password + '\'' + ", useSsl=" + useSsl + ", useStartTls=" + useStartTls;
  }

  @Override
  public boolean equals( final Object obj ) {
    if ( obj == null || !( obj instanceof EmailConfiguration ) ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }
    final EmailConfiguration that = (EmailConfiguration) obj;
    return ( this.authenticate == that.authenticate && this.debug == that.debug
        && this.smtpQuitWait == that.smtpQuitWait && this.useSsl == that.useSsl && this.useStartTls == that.useStartTls
        && ObjectUtils.equals( this.getSmtpPort(), that.getSmtpPort() )
        && isEquals( this.defaultFrom, that.defaultFrom ) && isEquals( this.fromName, that.fromName )
        && isEquals( this.smtpHost, that.smtpHost ) && isEquals( this.smtpProtocol, that.smtpProtocol )
        && isEquals( this.userId, that.userId ) && isEquals( this.password, that.password ) );
  }

  private boolean isEquals( final String a, final String b ) {
    return StringUtils.equals( a, b ) || ( StringUtils.isBlank( a ) && StringUtils.isBlank( b ) );
  }
}
