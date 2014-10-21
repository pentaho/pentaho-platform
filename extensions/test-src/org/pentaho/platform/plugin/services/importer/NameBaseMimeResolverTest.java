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
package org.pentaho.platform.plugin.services.importer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;

public class NameBaseMimeResolverTest {

  private static final MimeType[] TEST_MIME_TYPE_LIST = new MimeType[] { new MimeType( "test/test-mime", "tmt" ),
    new MimeType( "othertest/other-test-mime", "smt" ) };

  private NameBaseMimeResolver resolver;

  @Before
  public void prepareResolver() {
    resolver = new NameBaseMimeResolver();
    for ( MimeType type : TEST_MIME_TYPE_LIST ) {
      resolver.addMimeType( type );
    }
  }

  @Test
  public void testResolveMimeForFileName() {
    String actualMime = resolver.resolveMimeForFileName( "testFile.tmt" );
    assertThat( actualMime, equalTo( "test/test-mime" ) );
  }

  @Test
  public void testResolveMimeTypeForFileName() {
    MimeType expectedMimeType = new MimeType( "othertest/other-test-mime", "smt" );
    MimeType actualMimeType = resolver.resolveMimeTypeForFileName( "test.smt" );
    assertThat( actualMimeType, equalTo( expectedMimeType ) );
  }

  @Test
  public void testResolveMimeForBundle_with_defined_mime_type() {
    IPlatformImportBundle bundle = mock( IPlatformImportBundle.class );
    when( bundle.getMimeType() ).thenReturn( "bundle/mime-from-bundle" );
    when( bundle.getName() ).thenReturn( "testFile.smt" );
    String actualMime = resolver.resolveMimeForBundle( bundle );
    assertThat( actualMime, equalTo( "bundle/mime-from-bundle" ) );
  }

  @Test
  public void testResolveMimeForBundle_without_defined_mime_type() {
    IPlatformImportBundle bundle = mock( IPlatformImportBundle.class );
    when( bundle.getName() ).thenReturn( "testFile.smt" );
    String actualMime = resolver.resolveMimeForBundle( bundle );
    assertThat( actualMime, equalTo( "othertest/other-test-mime" ) );
  }

}
