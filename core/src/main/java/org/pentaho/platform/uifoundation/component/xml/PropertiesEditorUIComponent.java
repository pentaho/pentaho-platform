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


package org.pentaho.platform.uifoundation.component.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;

import java.util.List;

public class PropertiesEditorUIComponent extends XmlComponent {

  private static final long serialVersionUID = 1L;

  private static final Log logger = LogFactory.getLog( PropertiesEditorUIComponent.class );

  private Document document = null;

  protected IPentahoSession session = null;

  protected String baseUrl = null;

  public PropertiesEditorUIComponent( final IPentahoUrlFactory urlFactory, final List messages,
      final IPentahoSession session ) {
    super( urlFactory, messages, null );
    this.session = session;
    setXsl( "text/html", "PropertiesEditor.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
    setXslProperty( "baseUrl", urlFactory.getDisplayUrlBuilder().getUrl() ); //$NON-NLS-1$ 
  }

  @Override
  public Document getXmlContent() {

    if ( document == null ) {
      document = DocumentHelper.createDocument();
      document.addElement( "root" ); //$NON-NLS-1$
    }
    return document;
  }

  @Override
  public Log getLogger() {
    return PropertiesEditorUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

}
