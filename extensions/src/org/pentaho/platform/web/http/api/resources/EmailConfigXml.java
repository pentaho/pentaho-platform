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
package org.pentaho.platform.web.http.api.resources;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.config.DtdEntityResolver;
import org.pentaho.platform.config.IEmailConfig;
import org.pentaho.platform.engine.security.userroledao.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class EmailConfigXml implements IEmailConfig {
  
  private static final String ROOT_ELEMENT = "email-smtp";  //$NON-NLS-1$
  private static final String PASSWORD_XPATH = ROOT_ELEMENT +"/mail.password";  //$NON-NLS-1$
  private static final String AUTHENTICATE_XPATH = ROOT_ELEMENT +"/properties/mail.smtp.auth";  //$NON-NLS-1$
  private static final String DEBUG_XPATH = ROOT_ELEMENT +"/properties/mail.debug";  //$NON-NLS-1$
  private static final String DEFAULT_FROM_XPATH = ROOT_ELEMENT +"/mail.from.default";  //$NON-NLS-1$
  private static final String SMTP_HOST_XPATH = ROOT_ELEMENT +"/properties/mail.smtp.host";  //$NON-NLS-1$
  private static final String SMTP_PORT_XPATH = ROOT_ELEMENT +"/properties/mail.smtp.port";  //$NON-NLS-1$
  private static final String SMTP_PROTOCOL_XPATH = ROOT_ELEMENT +"/properties/mail.transport.protocol";  //$NON-NLS-1$
  private static final String SMTP_QUIT_WAIT_XPATH = ROOT_ELEMENT +"/properties/mail.smtp.quitwait";  //$NON-NLS-1$
  private static final String USER_ID_XPATH = ROOT_ELEMENT +"/mail.userid";  //$NON-NLS-1$
  private static final String USE_SSL_XPATH = ROOT_ELEMENT +"/properties/mail.smtp.ssl";  //$NON-NLS-1$
  private static final String USE_START_TLS_XPATH = ROOT_ELEMENT +"/properties/mail.smtp.starttls.enable";  //$NON-NLS-1$
  private static final String POP3_SERVER_XPATH = ROOT_ELEMENT +"/mail.pop3";  //$NON-NLS-1$
  
  Document document;
  
  public EmailConfigXml(File pentahoXmlFile) throws IOException, DocumentException{
    this(XmlDom4JHelper.getDocFromFile(pentahoXmlFile, new DtdEntityResolver()));    
  }
  
  public EmailConfigXml(String xml) throws DocumentException, XmlParseException {
    this(XmlDom4JHelper.getDocFromString(xml, new DtdEntityResolver()));
  }
  
  public EmailConfigXml(Document doc) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ((rootElement != null) &&  !doc.getRootElement().getName().equals(ROOT_ELEMENT)) {
      throw new DocumentException(Messages.getInstance().getErrorString("GoogleMapsConfig.ERROR_0001_INVALID_ROOT_ELEMENT")); //$NON-NLS-1$
    }
    document = doc;
  }
  
  public EmailConfigXml(IEmailConfig config) {
	this();
	setAuthenticate(config.getAuthenticate());
	setDebug(config.getDebug());
	setDefaultFrom(config.getDefaultFrom());
	setPassword(config.getPassword());
	setPop3Server(config.getPop3Server());
	setSmtpHost(config.getSmtpHost());
	setSmtpPort(config.getSmtpPort());
	setSmtpProtocol(config.getSmtpProtocol());
	setUserId(config.getUserId());
	setUseSsl(config.getUseSsl());
	setUseStartTls(config.getUseStartTls());
  }
  
  public EmailConfigXml() {
    document = DocumentHelper.createDocument();
    document.addElement(ROOT_ELEMENT);
  }
  
  
  private void setValue(String xPath, String value) {
    Element element = (Element) document.selectSingleNode( xPath );
    if (element == null) {
      element = DocumentHelper.makeElement(document, xPath);
    }
    element.setText(value);
  }

  private String getValue(String xpath) {
    Element element = (Element)document.selectSingleNode(xpath);
    return element != null ? element.getText() : null;
  }
  
  public Document getDocument() {
    return document;
  }
  public String getPassword() {
    return getValue(PASSWORD_XPATH);
  }
  public void setPassword(String password) {
    setValue(PASSWORD_XPATH, password);
  }
  public boolean getAuthenticate() {
    return Boolean.parseBoolean(getValue(AUTHENTICATE_XPATH));
  }
  public void setAuthenticate(boolean authenticate) {
    setValue(AUTHENTICATE_XPATH, Boolean.toString(authenticate));
  }
  public boolean getDebug() {
    return Boolean.parseBoolean(getValue(DEBUG_XPATH));
  }
  public void setDebug(boolean debug) {
    setValue(DEBUG_XPATH, Boolean.toString(debug));
  }
  public String getDefaultFrom() {
    return getValue(DEFAULT_FROM_XPATH);
  }
  public void setDefaultFrom(String defaultFrom) {
    setValue(DEFAULT_FROM_XPATH, defaultFrom);
  }
  public String getSmtpHost() {
    return getValue(SMTP_HOST_XPATH);
  }
  public void setSmtpHost(String smtpHost) {
    setValue(SMTP_HOST_XPATH, smtpHost);
  }
  public Integer getSmtpPort() {
    Integer port = null;
    try {
      port = new Integer(getValue(SMTP_PORT_XPATH));
    } catch (Exception ex) {
      // Do nothing..
    }
    return port;
  }
  public void setSmtpPort(Integer smtpPort) {
    setValue(SMTP_PORT_XPATH, smtpPort != null ? smtpPort.toString() : "");
  }
  public String getSmtpProtocol() {
    return getValue(SMTP_PROTOCOL_XPATH);
  }
  public void setSmtpProtocol(String smtpProtocol) {
    setValue(SMTP_PROTOCOL_XPATH, smtpProtocol);
  }
  public boolean getUseSsl() {
    return Boolean.parseBoolean(getValue(USE_SSL_XPATH));
  }
  public void setUseSsl(boolean useSsl) {
    setValue(USE_SSL_XPATH, Boolean.toString(useSsl));
  }
  public boolean getQuitWait() {
    return Boolean.parseBoolean(getValue(SMTP_QUIT_WAIT_XPATH));
  }
  public void setQuitWait(boolean useSsl) {
    setValue(SMTP_QUIT_WAIT_XPATH, Boolean.toString(useSsl));
  }
  public boolean getUseStartTls() {
    return Boolean.parseBoolean(getValue(USE_START_TLS_XPATH));
  }
  public void setUseStartTls(boolean useStartTls) {
    setValue(USE_START_TLS_XPATH, Boolean.toString(useStartTls));
  }
  public String getUserId() {
    return getValue(USER_ID_XPATH);
  }
  public void setUserId(String userId) {
    setValue(USER_ID_XPATH, userId);
  }
  public String getPop3Server() {
    return getValue(POP3_SERVER_XPATH);
  }
  public void setPop3Server(String pop3Server) {
    setValue(POP3_SERVER_XPATH, pop3Server);
  }
}
