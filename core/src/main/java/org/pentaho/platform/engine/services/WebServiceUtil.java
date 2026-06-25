/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.services;

import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

public class WebServiceUtil {

  public static Document createErrorDocument( String errorMsg ) {
    Element rootElement = new DefaultElement( "web-service" );
    Document doc = DocumentHelper.createDocument( rootElement );
    rootElement.addElement( "error" ).addAttribute( "msg", StringEscapeUtils.escapeXml11( errorMsg ) );
    return doc;
  }

  public static Document createStatusDocument( String statusMsg ) {
    Element rootElement = new DefaultElement( "web-service" );
    Document doc = DocumentHelper.createDocument( rootElement );
    rootElement.addElement( "status" ).addAttribute( "msg", StringEscapeUtils.escapeXml11( statusMsg ) );
    return doc;
  }
}
