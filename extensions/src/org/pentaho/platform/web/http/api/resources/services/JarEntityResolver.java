/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 27, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.http.api.resources.services;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.pentaho.platform.web.http.api.resources.services.messages.Messages;

public class JarEntityResolver implements EntityResolver {

	  private static final Logger logger = Logger.getLogger(JarEntityResolver.class);
	
	private static JarEntityResolver instance;
	
	public static JarEntityResolver getInstance() {
		if( instance == null ) {
			instance = new JarEntityResolver();
		}
		return instance;
	}
	
  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
   *      java.lang.String)
   */
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

    int idx = systemId.lastIndexOf('/');
    String dtdName = systemId.substring(idx + 1);
    
    try {
      InputStream xslIS = getClass().getClassLoader().getResourceAsStream( "system/dtd/" + dtdName ); //$NON-NLS-1$
      if( xslIS != null ) {
          InputSource source = new InputSource(xslIS);
          return source;
      }
  		logger.error( Messages.getErrorString( "JarEntityResoliver.ERROR_0001_COULD_NOT_RESOLVE", dtdName ) ); //$NON-NLS-1$
    } catch (Exception e) {
    	logger.error( Messages.getErrorString( "JarEntityResoliver.ERROR_0001_COULD_NOT_RESOLVE", dtdName ) , e); //$NON-NLS-1$
    }
    return null;
  }

}
