/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocaleImportHandlerTest {

  PentahoPlatformImporter importer;

  @Before
  public void setUp() throws Exception {

    MicroPlatform microPlatform = new MicroPlatform();
    Mockery context = new Mockery();

    Map<String, String> mimeMap = new HashMap<String, String>();
    mimeMap.put( "locale", "text/locale" );
    microPlatform.defineInstance( NameBaseMimeResolver.class, new NameBaseMimeResolver( mimeMap ) );

    IPlatformImportMimeResolver nameBaseMimeResolver = context.mock( IPlatformImportMimeResolver.class );
    microPlatform.defineInstance( IPlatformImportMimeResolver.class, nameBaseMimeResolver );

    List<String> allowedArtifacts = new ArrayList<String>();
    allowedArtifacts.add( "xaction" );
    allowedArtifacts.add( "url" );

    List<String> approvedExtensionsList = new ArrayList<String>();
    approvedExtensionsList.add( ".prpt" );
    approvedExtensionsList.add( ".xaction" );

    List<String> hiddenExtensionsList = new ArrayList<String>();
    hiddenExtensionsList.add( ".xml" );
    hiddenExtensionsList.add( ".png" );

    LocaleImportHandler localeImportHandler =
        new LocaleImportHandler( allowedArtifacts, approvedExtensionsList, hiddenExtensionsList );

    Map<String, IPlatformImportHandler> handlers = new HashMap<String, IPlatformImportHandler>();
    handlers.put( "text/locale", localeImportHandler );

    Map<String, String> mimes = new HashMap<String, String>();
    mimes.put( "locale", "text/locale" );
    importer = new PentahoPlatformImporter( handlers, new NameBaseMimeResolver( mimes ) );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );
  }

  @Test
  public void testImportLocaleFiles() throws Exception {

    StringBuffer localeContent = new StringBuffer();
    localeContent.append( "name=Test" );
    localeContent.append( "\n" );
    localeContent.append( "description=Test description" );

    RepositoryFile file = new RepositoryFile.Builder( "test.properties" ).build();
    RepositoryFileBundle repoFileBundle = new RepositoryFileBundle( file, null, "", null, "UTF-8", null );

    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
    localeFilesProcessor.isLocaleFile( repoFileBundle, "/", localeContent.toString().getBytes() );

    localeFilesProcessor.processLocaleFiles( importer );
  }
}
