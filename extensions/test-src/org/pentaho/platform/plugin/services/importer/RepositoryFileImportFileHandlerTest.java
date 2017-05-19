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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.plugin.services.importer;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.core.mimetype.MimeType;

import java.util.Arrays;
import java.util.List;

public class RepositoryFileImportFileHandlerTest {
  private static final String MIMENAME = "mimeName";
  private static final String MIME_EXTENSION = "dum";

  RepositoryFileImportFileHandler fileHandler;

  @Test
  public void testExtensionNotTruncated() {
    String name = "file.csv";
    setup( MIMENAME, MIME_EXTENSION );
    String title = fileHandler.getTitle( name );
    Assert.assertEquals( name, title );
  }

  @Test
  public void testExtensionTruncated() {
    String name = "file.prpt";
    setup( MIMENAME, MIME_EXTENSION );
    String title = fileHandler.getTitle( name );
    Assert.assertEquals( "file", title );
  }

  @Test
  public void testFileWithoutExtension() {
    String name = "file";
    setup( MIMENAME, MIME_EXTENSION );
    String title = fileHandler.getTitle( name );
    Assert.assertEquals( name, title );
  }

  private void setup( String mimeTypeName, String extension ) {
    List<String> extensions = Arrays.asList( extension );
    IMimeType mimeType = new MimeType( mimeTypeName, extensions );
    mimeType.setConverter( Mockito.mock( Converter.class ) );
    List<IMimeType> mimeTypeList = Arrays.asList( mimeType );

    fileHandler = new RepositoryFileImportFileHandler( mimeTypeList );
    fileHandler.setKnownExtensions( Arrays.asList( "prpt" ) );
  }

}
