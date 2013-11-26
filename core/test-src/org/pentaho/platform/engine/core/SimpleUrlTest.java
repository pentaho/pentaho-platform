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
