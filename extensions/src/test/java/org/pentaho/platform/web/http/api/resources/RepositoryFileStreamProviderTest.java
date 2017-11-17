  /*
 * Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * This software was developed by Hitachi Vantara and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
