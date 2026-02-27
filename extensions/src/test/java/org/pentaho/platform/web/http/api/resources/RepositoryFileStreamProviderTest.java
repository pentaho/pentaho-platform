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


package org.pentaho.platform.web.http.api.resources;

import org.junit.Test;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import static org.junit.Assert.*;

public class RepositoryFileStreamProviderTest {

  RepositoryFileStreamProvider repositoryFileStreamProvider;
  RepositoryFileOutputStream outputStream;
  String appendDateFormat;
  DateTimeFormatter formatter;

  @Test
  public void testWriteFile() throws Exception {
    LocalDateTime now = LocalDateTime.now();

    appendDateFormat = "yyyyMMddHHmmss";
    repositoryFileStreamProvider = new RepositoryFileStreamProvider( "/home/admin/one.xanalyzer", "/home/admin/one.*", false, appendDateFormat );
    outputStream = (RepositoryFileOutputStream) repositoryFileStreamProvider.getOutputStream();
    formatter = DateTimeFormatter.ofPattern( appendDateFormat );
    assertEquals( true, outputStream.getFilePath().startsWith( "/home/admin/one" ) );
    assertEquals( 29, outputStream.getFilePath().length() );

    appendDateFormat = "yyyy-MM-dd";
    repositoryFileStreamProvider.setAppendDateFormat( appendDateFormat );
    formatter = DateTimeFormatter.ofPattern( appendDateFormat );
    outputStream = (RepositoryFileOutputStream) repositoryFileStreamProvider.getOutputStream();
    assertEquals( "/home/admin/one" + now.format( formatter ), outputStream.getFilePath() );

    appendDateFormat = "yyyyMMdd";
    repositoryFileStreamProvider.setAppendDateFormat( appendDateFormat );
    formatter = DateTimeFormatter.ofPattern( appendDateFormat );
    outputStream = (RepositoryFileOutputStream) repositoryFileStreamProvider.getOutputStream();
    assertEquals( "/home/admin/one" + now.format( formatter ), outputStream.getFilePath() );

    appendDateFormat = "MM-dd-yyyy";
    repositoryFileStreamProvider.setAppendDateFormat( appendDateFormat );
    formatter = DateTimeFormatter.ofPattern( appendDateFormat );
    outputStream = (RepositoryFileOutputStream) repositoryFileStreamProvider.getOutputStream();
    assertEquals( "/home/admin/one" + now.format( formatter ), outputStream.getFilePath() );

    appendDateFormat = "MM-dd-yy";
    repositoryFileStreamProvider.setAppendDateFormat( "MM-dd-yy" );
    formatter = DateTimeFormatter.ofPattern( appendDateFormat );
    outputStream = (RepositoryFileOutputStream) repositoryFileStreamProvider.getOutputStream();
    assertEquals( "/home/admin/one" + now.format( formatter ), outputStream.getFilePath() );

    appendDateFormat = "dd-MM-yyyy";
    repositoryFileStreamProvider.setAppendDateFormat( "dd-MM-yyyy" );
    formatter = DateTimeFormatter.ofPattern( appendDateFormat );
    outputStream = (RepositoryFileOutputStream) repositoryFileStreamProvider.getOutputStream();
    assertEquals( "/home/admin/one" + now.format( formatter ), outputStream.getFilePath() );
  }
}
