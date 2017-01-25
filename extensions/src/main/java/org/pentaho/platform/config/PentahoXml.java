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

package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.config.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PentahoXml {

  Document document;

  private static final String ROOT_ELEMENT = "pentaho-system"; //$NON-NLS-1$
  private static final String AUDIT_DATE_FORMAT_XPATH = ROOT_ELEMENT + "/audit/auditDateFormat"; //$NON-NLS-1$
  private static final String AUDIT_LOG_FILE_XPATH = ROOT_ELEMENT + "/audit/auditLogFile"; //$NON-NLS-1$
  private static final String AUDIT_LOG_SEPARATOR_XPATH = ROOT_ELEMENT + "/audit/id_separator"; //$NON-NLS-1$ 
  private static final String DEFAULT_PARAMETER_FORM = ROOT_ELEMENT + "/default-parameter-xsl"; //$NON-NLS-1$
  private static final String LOG_FILE_XPATH = ROOT_ELEMENT + "/log-file"; //$NON-NLS-1$
  private static final String LOG_LEVEL_XPATH = ROOT_ELEMENT + "/log-level"; //$NON-NLS-1$
  private static final String SOLUTION_REPOSITORY_CACHE_SIZE_XPATH = ROOT_ELEMENT + "/solution-repository/cache-size"; //$NON-NLS-1$
  private static final String ACL_FILES_XPATH = ROOT_ELEMENT + "/acl-files"; //$NON-NLS-1$
  private static final String ADMIN_ROLE_XPATH = ROOT_ELEMENT + "/acl-voter/admin-role"; //$NON-NLS-1$
  private static final String ANONYMOUS_USER_XPATH = ROOT_ELEMENT + "/anonymous-authentication/anonymous-user"; //$NON-NLS-1$
  private static final String ANONYMOUS_ROLE_XPATH = ROOT_ELEMENT + "/anonymous-authentication/anonymous-role"; //$NON-NLS-1$
  private static final String ACL_ENTRY_XPATH = ROOT_ELEMENT + "/acl-publisher/default-acls/acl-entry"; //$NON-NLS-1$

  public PentahoXml( File pentahoXmlFile ) throws IOException, DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromFile( pentahoXmlFile, new DtdEntityResolver() ) );
  }

  public PentahoXml( String xml ) throws DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromString( xml, new DtdEntityResolver() ) );
  }

  public PentahoXml( Document doc ) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT_ELEMENT ) ) {
      throw new DocumentException( Messages.getInstance().getErrorString( "PentahoXml.ERROR_0001_INVALID_ROOT_ELEMENT" ) ); //$NON-NLS-1$ 
    }
    document = doc;
  }

  public PentahoXml() {
    document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
  }

  public String getAuditDateFormat() {
    return getValue( AUDIT_DATE_FORMAT_XPATH );
  }

  public void setAuditDateFormat( String auditDateFormat ) {
    setValue( AUDIT_DATE_FORMAT_XPATH, auditDateFormat );
  }

  public String getAuditLogFile() {
    return getValue( AUDIT_LOG_FILE_XPATH );
  }

  public void setAuditLogFile( String auditLogFile ) {
    setValue( AUDIT_LOG_FILE_XPATH, auditLogFile );
  }

  public String getAuditLogSeparator() {
    return getValue( AUDIT_LOG_SEPARATOR_XPATH );
  }

  public void setAuditLogSeparator( String auditLogSeparator ) {
    setValue( AUDIT_LOG_SEPARATOR_XPATH, auditLogSeparator, true );
  }

  public String getDefaultParameterForm() {
    return getValue( DEFAULT_PARAMETER_FORM );
  }

  public void setDefaultParameterForm( String defaultParameterForm ) {
    setValue( DEFAULT_PARAMETER_FORM, defaultParameterForm );
  }

  public String getLogFile() {
    return getValue( LOG_FILE_XPATH );
  }

  public void setLogFile( String logFile ) {
    setValue( LOG_FILE_XPATH, logFile );
  }

  public String getLogLevel() {
    return getValue( LOG_LEVEL_XPATH );
  }

  public void setLogLevel( String logLevel ) {
    setValue( LOG_LEVEL_XPATH, logLevel );
  }

  public List<AclEntry> getDefaultAcls() {
    List<AclEntry> aclEntries = new ArrayList<AclEntry>();
    List<Element> elements = document.selectNodes( ACL_ENTRY_XPATH ); //$NON-NLS-1$ //$NON-NLS-2$
    for ( Element element : elements ) {
      AclEntry aclEntry = new AclEntry();
      aclEntry.setPrincipalName( element.attributeValue( "role" ) );
      aclEntry.setPermission( element.attributeValue( "acl" ) );
      aclEntries.add( aclEntry );
    }
    return aclEntries;
  }

  public void setDefaultAcls( List<AclEntry> defaultAcls ) {

  }

  public Integer getSolutionRepositoryCacheSize() {
    Integer cacheSize = null;
    String tempValue = getValue( SOLUTION_REPOSITORY_CACHE_SIZE_XPATH );
    if ( tempValue != null ) {
      try {
        cacheSize = Integer.parseInt( tempValue );
      } catch ( Exception ex ) {
        // do nothing we'll return null
      }
    }
    return cacheSize;
  }

  public void setSolutionRepositoryCacheSize( Integer solutionReporitoryCacheSize ) {
    setValue( SOLUTION_REPOSITORY_CACHE_SIZE_XPATH, solutionReporitoryCacheSize == null
        ? "" : solutionReporitoryCacheSize.toString() ); //$NON-NLS-1$
  }

  public String getAclFiles() {
    return getValue( ACL_FILES_XPATH );
  }

  public void setAclFiles( String fileExtensions ) {
    setValue( ACL_FILES_XPATH, fileExtensions != null ? fileExtensions : "" ); //$NON-NLS-1$
  }

  public String getAdminRole() {
    return getValue( ADMIN_ROLE_XPATH );
  }

  public void setAdminRole( String role ) {
    setValue( ADMIN_ROLE_XPATH, role != null ? role : "" ); //$NON-NLS-1$
  }

  public String getAnonymousUser() {
    return getValue( ANONYMOUS_USER_XPATH );
  }

  public void setAnonymousUser( String user ) {
    setValue( ANONYMOUS_USER_XPATH, user != null ? user : "" ); //$NON-NLS-1$
  }

  public String getAnonymousRole() {
    return getValue( ANONYMOUS_ROLE_XPATH );
  }

  public void setAnonymousRole( String role ) {
    setValue( ANONYMOUS_ROLE_XPATH, role != null ? role : "" ); //$NON-NLS-1$
  }

  private void setValue( String xPath, String value ) {
    setValue( xPath, value, false );
  }

  private void setValue( String xPath, String value, boolean useCData ) {
    Element element = (Element) document.selectSingleNode( xPath );
    if ( element == null ) {
      element = DocumentHelper.makeElement( document, xPath );
    }
    if ( useCData ) {
      element.clearContent();
      element.addCDATA( value );
    } else {
      element.setText( value );
    }
  }

  private String getValue( String xpath ) {
    Element element = (Element) document.selectSingleNode( xpath );
    return element != null ? element.getText() : null;
  }

  public Document getDocument() {
    return document;
  }

}
