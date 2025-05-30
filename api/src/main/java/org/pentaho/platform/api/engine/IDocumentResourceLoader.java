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


package org.pentaho.platform.api.engine;

import org.xml.sax.EntityResolver;

import javax.xml.transform.URIResolver;
import java.io.InputStream;

public interface IDocumentResourceLoader extends URIResolver, EntityResolver {

  public InputStream loadXsl( String name );

}
