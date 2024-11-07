/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DecoratedFileContentTest {

  private FileContent fileContent = mock( FileContent.class );
  private DecoratedFileContent decorated = new DecoratedFileContent( fileContent );

  @Test
  public void testGetFile() throws Exception {
    decorated.getFile();
    verify( fileContent ).getFile();
  }

  @Test
  public void testGetSize() throws Exception {
    decorated.getSize();
    verify( fileContent ).getSize();
  }

  @Test
  public void testGetLastModifiedTime() throws Exception {
    decorated.getLastModifiedTime();
    verify( fileContent ).getLastModifiedTime();
  }

  @Test
  public void testSetLastModifiedTime() throws Exception {
    decorated.setLastModifiedTime( 100L );
    verify( fileContent ).setLastModifiedTime( 100L );
  }

  @Test
  public void testHasAttribute() throws Exception {
    decorated.hasAttribute( "asdf" );
    verify( fileContent ).hasAttribute( "asdf" );
  }

  @Test
  public void testGetAttributes() throws Exception {
    decorated.getAttributes();
    verify( fileContent ).getAttributes();
  }

  @Test
  public void testGetAttributeNames() throws Exception {
    decorated.getAttributeNames();
    verify( fileContent ).getAttributeNames();
  }

  @Test
  public void testGetAttribute() throws Exception {
    decorated.getAttribute( "name" );
    verify( fileContent ).getAttribute( "name" );
  }

  @Test
  public void testSetAttribute() throws Exception {
    Object o = new Object();
    decorated.setAttribute( "attr", o );
    verify( fileContent ).setAttribute( "attr", o );
  }

  @Test
  public void testRemoveAttribute() throws Exception {
    decorated.removeAttribute( "toRemove" );
    verify( fileContent ).removeAttribute( "toRemove" );
  }

  @Test
  public void testGetCertificates() throws Exception {
    decorated.getCertificates();
    verify( fileContent ).getCertificates();
  }

  @Test
  public void testGetInputStream() throws Exception {
    decorated.getInputStream();
    verify( fileContent ).getInputStream();
  }

  @Test
  public void testGetOutputStream() throws Exception {
    decorated.getOutputStream();
    verify( fileContent ).getOutputStream();
  }

  @Test
  public void testGetRandomAccessContent() throws Exception {
    decorated.getRandomAccessContent( RandomAccessMode.READ );
    verify( fileContent ).getRandomAccessContent( RandomAccessMode.READ );
  }

  @Test
  public void testGetOutputStream1() throws Exception {
    decorated.getOutputStream( false );
    verify( fileContent ).getOutputStream( false );
  }

  @Test
  public void testClose() throws Exception {
    decorated.close();
    verify( fileContent ).close();
  }

  @Test
  public void testGetContentInfo() throws Exception {
    decorated.getContentInfo();
    verify( fileContent ).getContentInfo();
  }

  @Test
  public void testIsOpen() throws Exception {
    decorated.isOpen();
    verify( fileContent ).isOpen();
  }
}
