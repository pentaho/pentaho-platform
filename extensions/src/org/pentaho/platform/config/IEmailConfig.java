/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
*/
package org.pentaho.platform.config;

public interface IEmailConfig {
  public String getPassword();
  public void setPassword(String password);
  public boolean getAuthenticate();
  public void setAuthenticate(boolean authenticate);
  public boolean getDebug();
  public void setDebug(boolean debug);
  public String getDefaultFrom();
  public void setDefaultFrom(String defaultFrom);
  public String getSmtpHost();
  public void setSmtpHost(String smtpHost);
  public Integer getSmtpPort();
  public void setSmtpPort(Integer smtpPort);
  public String getSmtpProtocol();
  public void setSmtpProtocol(String smtpProtocol);
  public boolean getUseSsl();
  public void setUseSsl(boolean useSsl);
  public boolean getUseStartTls();
  public void setUseStartTls(boolean useStartTls);
  public String getUserId();
  public void setUserId(String userId);
  public String getPop3Server();
  public void setPop3Server(String pop3Server);
  public boolean getQuitWait();
  public void setQuitWait(boolean quitWait);
}
