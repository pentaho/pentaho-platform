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


package org.pentaho.platform.engine.core;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.util.web.SimpleUrl;
import org.pentaho.platform.util.web.SimpleUrlFactory;

public class SimpleUrlTest extends TestCase {

  public void testSimpleUrl() {

    String baseUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "/testurl?" ); //$NON-NLS-1$
    urlFactory.getActionUrlBuilder();
    SimpleUrl url = new SimpleUrl( baseUrl );
    Assert.assertEquals( url.getUrl(), baseUrl );
    url.setParameter( "action", "doSomething" ); //$NON-NLS-1$  //$NON-NLS-2$
    Assert.assertEquals( url.getUrl(), baseUrl + "&action=doSomething" ); //$NON-NLS-1$

  }

}
