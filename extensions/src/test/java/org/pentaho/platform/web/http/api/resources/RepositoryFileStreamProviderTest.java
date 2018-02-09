/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
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
