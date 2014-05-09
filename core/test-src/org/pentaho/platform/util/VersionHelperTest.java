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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Aug 18, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.util;

import org.pentaho.platform.api.engine.IPentahoObjectFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

public class VersionHelperTest extends TestCase {

  public void testVersionHelper() {

    VersionHelper vh = new VersionHelper();
    
    // Should read server-assembly.properties file from root.
    String verInfo = vh.getVersionInformation();
    VersionInfo vi1 = VersionHelper.getVersionInfo();
    Assert.assertFalse( vi1.isFromManifest() );
    Assert.assertEquals( "TVH", vi1.getProductID() );
    Assert.assertEquals( "Test Version Helper", vi1.getTitle() );
    Assert.assertEquals( "5", vi1.getVersionMajor() );
    Assert.assertEquals( "0", vi1.getVersionMinor() );
    Assert.assertEquals( "SNAPSHOT", vi1.getVersionRelease() );

    // Should Read from this class - no manifest
    String verInfo2 = vh.getVersionInformation(this.getClass());
    VersionInfo vi2 = VersionHelper.getVersionInfo(this.getClass());
    Assert.assertFalse( vi2.isFromManifest() );
    Assert.assertEquals( "PCONN", vi2.getProductID() );
    Assert.assertEquals( "Pentaho Connections API", vi2.getTitle() );
    Assert.assertEquals( "2", vi2.getVersionMajor() );
    Assert.assertEquals( "0", vi2.getVersionMinor() );
    Assert.assertEquals( "0", vi2.getVersionRelease() );
    
    Assert.assertEquals("Test Version Helper 5.0-SNAPSHOT", verInfo);
    Assert.assertEquals("Pentaho Connections API 2.0.0.25 (class)", verInfo2);

    // Gets version information from API Jar Manifest
    VersionInfo vi3 = VersionHelper.getVersionInfo(IPentahoObjectFactory.class);
    Assert.assertTrue( vi3.isFromManifest() );
    Assert.assertEquals("POBS", vi3.getProductID());
    Assert.assertEquals( "Pentaho Platform API", vi3.getTitle() );
    Assert.assertNotNull( vi3.getVersionRelease() );
    String verInfo3 = vh.getVersionInformation(IPentahoObjectFactory.class);
    
    Assert.assertNotNull( verInfo3 );
    boolean startsWith = verInfo3.startsWith( "Pentaho Platform API" );
    assertTrue( startsWith );
    
  }

  public static void main(final String[] args) {
    VersionHelperTest test = new VersionHelperTest();
    try {
      test.testVersionHelper();
    } finally {
    }
  }

}
