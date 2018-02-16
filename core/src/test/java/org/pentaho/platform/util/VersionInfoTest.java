/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link org.pentaho.platform.util.VersionInfo} class.
 */
public class VersionInfoTest {

  /**
   * Test default values.
   */
  @Test
  public void testDefaults() {
    final VersionInfo info = new VersionInfo();
    doTest( info, false, null, null, null, null, null, null, null, "" );
  }

  /**
   * Tests that the setting of specific attributes occurs as expected.
   */
  @Test
  public void testNonDefaults() {
    final boolean fromManifest = true;
    final String productID = "1"; //$NON-NLS-1$
    final String title = "My Title"; //$NON-NLS-1$
    final String versionMajor = "10"; //$NON-NLS-1$
    String versionMinor = "3a"; //$NON-NLS-1$
    String versionRelease = "123"; //$NON-NLS-1$
    String versionMilestone = "007"; //$NON-NLS-1$
    String versionBuild = "87590"; //$NON-NLS-1$
    StringBuffer versionNumber = new StringBuffer();
    versionNumber.append( versionMajor );
    versionNumber.append( '.' ).append( versionMinor ); //$NON-NLS-1$
    versionNumber.append( '.' ).append( versionRelease ); //$NON-NLS-1$
    versionNumber.append( '.' ).append( versionMilestone ); //$NON-NLS-1$
    versionNumber.append( '.' ).append( versionBuild ); //$NON-NLS-1$

    final VersionInfo info = new VersionInfo();
    info.setFromManifest( fromManifest );
    info.setProductID( productID );
    info.setTitle( title );
    info.setVersionMajor( versionMajor );
    info.setVersionMinor( versionMinor );
    info.setVersionRelease( versionRelease );
    info.setVersionMilestone( versionMilestone );
    info.setVersionBuild( versionBuild );
    doTest( info, fromManifest, productID, title, versionMajor, versionMinor, versionRelease, versionMilestone,
      versionBuild, versionNumber.toString() );

    // The format of versionNumber is: <versionMajor>.<versionMinor>.<versionRelease>.<versionMilestone>.<versionBuild>
    // The method that builds versionNumber checks the individual attribute values in that order and ignores the
    // following values, if an null attribute value is encountered at any point; verify that this is true for each
    // attribute

    // null versionMinor, expecting versionNumber = <versionMajor>
    info.setVersionMinor( null );
    versionNumber = new StringBuffer();
    versionNumber.append( versionMajor );
    doTest( info, fromManifest, productID, title, versionMajor, null, versionRelease, versionMilestone, versionBuild,
      versionNumber.toString() );

    // null versionRelease, expecting versionNumber = <versionMajor>.<versionMinor>
    versionMinor = "3a"; //$NON-NLS-1$
    info.setVersionMinor( versionMinor );
    info.setVersionRelease( null );
    versionNumber = new StringBuffer();
    versionNumber.append( versionMajor );
    versionNumber.append( '.' ).append( versionMinor ); //$NON-NLS-1$
    doTest( info, fromManifest, productID, title, versionMajor, versionMinor, null, versionMilestone, versionBuild,
      versionNumber.toString() );

    // null versionMilestone, expecting versionNumber = <versionMajor>.<versionMinor>.<versionRelease>
    versionRelease = "123"; //$NON-NLS-1$
    info.setVersionRelease( versionRelease );
    info.setVersionMilestone( null );
    versionNumber = new StringBuffer();
    versionNumber.append( versionMajor );
    versionNumber.append( '.' ).append( versionMinor ); //$NON-NLS-1$
    versionNumber.append( '.' ).append( versionRelease ); //$NON-NLS-1$
    doTest( info, fromManifest, productID, title, versionMajor, versionMinor, versionRelease, null, versionBuild,
      versionNumber.toString() );

    // null versionBuild, expecting versionNumber = <versionMajor>.<versionMinor>.<versionRelease>.<versionMilestone>
    versionMilestone = "007"; //$NON-NLS-1$
    info.setVersionMilestone( versionMilestone );
    info.setVersionBuild( null );
    versionNumber = new StringBuffer();
    versionNumber.append( versionMajor );
    versionNumber.append( '.' ).append( versionMinor ); //$NON-NLS-1$
    versionNumber.append( '.' ).append( versionRelease ); //$NON-NLS-1$
    versionNumber.append( '.' ).append( versionMilestone ); //$NON-NLS-1$
    doTest( info, fromManifest, productID, title, versionMajor, versionMinor, versionRelease, versionMilestone, null,
      versionNumber.toString() );
  }

  /**
   * Tests the various version flavors, using the two approved delimiters (. and -) and that the various pieces
   * are set correctly based on the combined version value.
   */
  @Test
  public void testSetVersion() {
    VersionInfo info;
    String version, versionClean;

    // valid, full-length version
    info = new VersionInfo();
    version = "10.3a.123.007.87590";
    versionClean = "10.3a.123.007.87590";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", "123", "007", "87590", versionClean );

    // valid version with no build
    info = new VersionInfo();
    version = "10.3a.123.007";
    versionClean = "10.3a.123.007";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", "123", "007", null, versionClean );

    // valid version with no build or milestone
    info = new VersionInfo();
    version = "10-3a-123";
    versionClean = "10.3a.123";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", "123", null, null, versionClean );

    // valid version with no build, milestone or release
    info = new VersionInfo();
    version = "10-3a";
    versionClean = "10.3a";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", null, null, null, versionClean );

    // valid version with no build, milestone, release or minor value
    info = new VersionInfo();
    version = "10";
    versionClean = "10";
    info.setVersion( version );
    doTest( info, false, null, null, "10", null, null, null, null, versionClean );
  }

  /**
   * Tests the various ways to set an empty version.
   */
  @Test
  public void testSetVersionEmpty() {
    // empty version value - versionMajor will be an empty string and all other attributes should be null
    VersionInfo info = new VersionInfo();
    info.setVersion( "" );
    doTest( info, false, null, null, "", null, null, null, null, "" );

    // null, null string, hyphen and period should all be treated the same - as if the value was empty,
    final String[] emptyVersions = new String[] { null, "-", "." };
    for ( final String emptyVersion : emptyVersions ) {
      info = new VersionInfo();
      info.setVersion( emptyVersion );
      doTest( info, false, null, null, null, null, null, null, null, "" );
    }
  }

  /**
   * Verifies that additional fields present in the combined version value are ignored.
   */
  @Test
  public void testSetVersionAdditionalFields() {
    VersionInfo info;
    String version, versionClean;

    info = new VersionInfo();
    version = "10.3a.123.007.87590.foe";
    versionClean = "10.3a.123.007.87590";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", "123", "007", "87590", versionClean );

    info = new VersionInfo();
    version = "10.3a.123.007.87590.foe.foo-abc.xyz";
    versionClean = "10.3a.123.007.87590";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", "123", "007", "87590", versionClean );
  }

  /**
   * Tests that when partial version values are provided (with missing pieces in the beginning, middle and end),
   * the individual pieces are set as expected.
   */
  @Test
  public void testSetVersionMissingFields() {
    VersionInfo info;
    String version, versionClean;

    // when the first character is the delimiter (.) the major version should be empty
    info = new VersionInfo();
    version = ".3a.123.007.";
    versionClean = ".3a.123.007";
    info.setVersion( version );
    doTest( info, false, null, null, "",  "3a", "123", "007", null, versionClean );

    // when the first character is the delimiter (-) the major version should be empty
    info = new VersionInfo();
    version = "-3a.123.007-";
    versionClean = ".3a.123.007";
    info.setVersion( version );
    doTest( info, false, null, null, "", "3a", "123", "007", null, versionClean );

    // consecutive delimiter characters yield empty values where appropriate
    info = new VersionInfo();
    version = "10..123..87590";
    versionClean = "10..123..87590";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "", "123", "", "87590", versionClean );

    info = new VersionInfo();
    version = "10.3a.-.87590";
    versionClean = "10.3a...87590";
    info.setVersion( version );
    doTest( info, false, null, null, "10", "3a", "", "", "87590", versionClean );
  }

  private static void doTest( final VersionInfo info, final boolean expectedFromManifest, final String
    expectedProductID, final String expectedTitle, final String expectedVersionMajor, final String
    expectedVersionMinor, final String expectedVersionRelease, final String expectedVersionMilestone, final String
    expectedVersionBuild, final String expectedVersionNumber ) {

    Assert.assertEquals( expectedFromManifest, info.isFromManifest() );
    Assert.assertEquals( expectedProductID, info.getProductID() );
    Assert.assertEquals( expectedTitle, info.getTitle() );
    Assert.assertEquals( expectedVersionMajor, info.getVersionMajor() );
    Assert.assertEquals( expectedVersionMinor, info.getVersionMinor() );
    Assert.assertEquals( expectedVersionRelease, info.getVersionRelease() );
    Assert.assertEquals( expectedVersionMilestone, info.getVersionMilestone() );
    Assert.assertEquals( expectedVersionBuild, info.getVersionBuild() );
    Assert.assertEquals( expectedVersionNumber, info.getVersionNumber() );

    final StringBuffer buffer = new StringBuffer();
    buffer.append( "fromManifest       = [" ).append( expectedFromManifest ).append( "]\n" ); //$NON-NLS-1$  //$NON-NLS-2$
    buffer.append( "productID          = [" ).append( expectedProductID ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    buffer.append( "title              = [" ).append( expectedTitle ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    buffer.append( "versionMajor       = [" ).append( expectedVersionMajor ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    buffer.append( "versionMinor       = [" ).append( expectedVersionMinor ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    buffer.append( "versionRelease     = [" ).append( expectedVersionRelease ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    buffer.append( "versionMilestone   = [" ).append( expectedVersionMilestone ).append( "]\n" ); //$NON-NLS-1$ $NON-NLS-2$
    buffer.append( "versionBuild       = [" ).append( expectedVersionBuild ).append( "]\n" ); //$NON-NLS-1$ $NON-NLS-2$
    buffer.append( "getVersionNumber() = [" ).append( expectedVersionNumber ).append( "]\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    Assert.assertEquals( buffer.toString(), info.toString() );
  }
}
