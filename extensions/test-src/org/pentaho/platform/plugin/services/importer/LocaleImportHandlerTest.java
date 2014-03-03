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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class LocaleImportHandlerTest {

  PentahoPlatformImporter importer;
  LocaleFilesProcessor localeFilesProcessor;

  @Before
  public void setUp() throws Exception {

    MicroPlatform microPlatform = new MicroPlatform();
    Mockery context = new Mockery();

    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    microPlatform.defineInstance( IPlatformImportMimeResolver.class, nameResolver );
    
    IRepositoryContentConverterHandler converterHandler =
        new DefaultRepositoryContentConverterHandler( new HashMap<String, Converter>() );
    
    List<MimeType> localeMimeList = new ArrayList<MimeType>();
    localeMimeList.add( new MimeType( "text/locale", "locale" ) );
    
    List<MimeType> mimeList = new ArrayList<MimeType>();
    nameResolver.addMimeType( new MimeType( "text/prptMimeType", "prpt" ) );
    nameResolver.addMimeType( new MimeType( "text/xactionMimeType", "xaction" ) );

    MimeType mimeType = new MimeType( "text/xml", "xml");
    mimeType.setHidden(true);
    nameResolver.addMimeType(  mimeType );

    mimeType = new MimeType( "image/png", "png");
    mimeType.setHidden(true);
    nameResolver.addMimeType(  mimeType );
    
    
    List<String> allowedArtifacts = new ArrayList<String>();
    allowedArtifacts.add( "xaction" );
    allowedArtifacts.add( "url" );

    LocaleImportHandler localeImportHandler = new LocaleImportHandler( localeMimeList, allowedArtifacts );

    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( localeImportHandler );

    importer = new PentahoPlatformImporter( handlers, converterHandler );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );
    
    localeFilesProcessor = new LocaleFilesProcessor();
  }

  @Test
  public void testImportLocaleFiles() throws Exception {

    StringBuffer localeContent = new StringBuffer();
    localeContent.append( "name=Test" );
    localeContent.append( "\n" );
    localeContent.append( "description=Test description" );
    
    Assert.assertTrue( processIsLocalFile( "test.properties", localeContent ) );
    Assert.assertFalse( processIsLocalFile( "test.bla", localeContent ) );
    
    localeContent = new StringBuffer("bla bla");
    Assert.assertFalse( processIsLocalFile( "test.properties", localeContent ) );

    localeFilesProcessor.processLocaleFiles( importer );
  }
  
  private boolean processIsLocalFile( String fileName, StringBuffer localeContent ) throws Exception {
    RepositoryFile file = new RepositoryFile.Builder( fileName ).build();
    RepositoryFileBundle repoFileBundle = new RepositoryFileBundle( file, null, "", null, "UTF-8", null );
    return localeFilesProcessor.isLocaleFile( repoFileBundle, "/", localeContent.toString().getBytes() );
  }
}
