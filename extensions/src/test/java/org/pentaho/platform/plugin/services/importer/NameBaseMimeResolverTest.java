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


package org.pentaho.platform.plugin.services.importer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.core.mimetype.MimeType;

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
    assertEquals( actualMime, "test/test-mime" );
  }

  @Test
  public void testResolveMimeTypeForFileName() {
    MimeType expectedMimeType = new MimeType( "othertest/other-test-mime", "smt" );
    IMimeType actualMimeType = resolver.resolveMimeTypeForFileName( "test.smt" );
    assertEquals( actualMimeType, expectedMimeType );
  }

  @Test
  public void testResolveMimeForBundle_with_defined_mime_type() {
    IPlatformImportBundle bundle = mock( IPlatformImportBundle.class );
    when( bundle.getMimeType() ).thenReturn( "bundle/mime-from-bundle" );
    when( bundle.getName() ).thenReturn( "testFile.smt" );
    String actualMime = resolver.resolveMimeForBundle( bundle );
    assertEquals( actualMime, "bundle/mime-from-bundle" );
  }

  @Test
  public void testResolveMimeForBundle_without_defined_mime_type() {
    IPlatformImportBundle bundle = mock( IPlatformImportBundle.class );
    when( bundle.getName() ).thenReturn( "testFile.smt" );
    String actualMime = resolver.resolveMimeForBundle( bundle );
    assertEquals( actualMime, "othertest/other-test-mime" );
  }

}
