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


package org.pentaho.platform.plugin.services.importexport;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

/**
 * @author TKafalas
 * 
 */
public class RepositoryFileBundleTest {
  static final  String FILE_NAME = "dummy.ext";
  static final String PATH = "public/folder";
  static final String USER_NAME = "user name";
  static final String MIME_NAME = "mimeName";
  static final String CHAR_SET = "UTF-8";
  static final RepositoryFile repositoryFile = new RepositoryFile.Builder( FILE_NAME ).path( PATH ).build();
  static final RepositoryFileSid sid = new RepositoryFileSid( USER_NAME );
  static final RepositoryFileAce ace1 = new RepositoryFileAce( sid, RepositoryFilePermission.READ,
      RepositoryFilePermission.WRITE, RepositoryFilePermission.DELETE );
  static final RepositoryFileAce ace2 = new RepositoryFileAce( new RepositoryFileSid( USER_NAME ),
      RepositoryFilePermission.READ, RepositoryFilePermission.WRITE, RepositoryFilePermission.DELETE );
  static final List<RepositoryFileAce> aces = Arrays.asList( ace1, ace2 );
  static final RepositoryFileAcl acl = new RepositoryFileAcl( "", sid, false, aces );

  static RepositoryFileBundle repositoryFileBundle;

  @BeforeClass
  public static void setup() throws Exception {
    File tmpFile = File.createTempFile( "tempfile", ".tmp" );
    repositoryFileBundle = new RepositoryFileBundle( repositoryFile, acl, PATH, tmpFile, CHAR_SET, MIME_NAME );
  }

  @Test
  public void testGetAcl() throws Exception {
    assertEquals( repositoryFileBundle.getAcl(), acl );
  }

  @Test
  public void testGetCharSet() throws Exception {
    assertEquals( repositoryFileBundle.getCharset(), CHAR_SET );
  }

  @Test
  public void testGetInputStream() throws Exception {
    assertTrue( "Expected an instance of InputStream", repositoryFileBundle.getInputStream() instanceof InputStream );
  }

  @Test
  public void testGetFile() throws Exception {
    assertEquals( repositoryFileBundle.getFile(), repositoryFile );
  }

  @Test
  public void testGetMimeType() throws Exception {
    assertEquals( repositoryFileBundle.getMimeType(), MIME_NAME );
  }

  @Test
  public void testGetPath() throws Exception {
    assertEquals( repositoryFileBundle.getPath(), PATH );
  }

  @Test
  public void testSetPath() throws Exception {
    final String newPath = "/newpath";
    repositoryFileBundle.setPath( newPath );
    assertEquals( repositoryFileBundle.getPath(), newPath );
  }

  @Test
  public void testEquals() throws Exception {
    RepositoryFileBundle equalBundle = new RepositoryFileBundle( repositoryFile, acl, PATH, null, CHAR_SET, MIME_NAME );
    assertEquals( repositoryFileBundle, equalBundle );

    equalBundle = new RepositoryFileBundle( repositoryFile, acl, "diffPath", null, CHAR_SET, MIME_NAME );
    assertNotEquals( repositoryFileBundle, equalBundle );

    RepositoryFile repoFile = new RepositoryFile.Builder( "diffName" ).path( PATH ).build();
    equalBundle = new RepositoryFileBundle( repoFile, acl, PATH, null, CHAR_SET, MIME_NAME );
    assertNotEquals( repositoryFileBundle, equalBundle );
  }

}
