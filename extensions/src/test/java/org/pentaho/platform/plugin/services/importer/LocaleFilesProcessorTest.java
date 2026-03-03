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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Created by Yury_Bakhmutski on 1/20/2018.
 */
public class LocaleFilesProcessorTest {

  private static final String DEFAULT_ENCODING = "UTF-8";

  PentahoPlatformImporter importer;
  LocaleFilesProcessor localeFilesProcessor;

  @Before
  public void setUp() throws Exception {
    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    PentahoSystem.registerObject( nameResolver );
  }

  @Test
  public void testProcessLocaleFilesIgnoreProperties() throws Exception {
    IRepositoryContentConverterHandler converterHandler = new DefaultRepositoryContentConverterHandler( new HashMap<String, Converter>() );
    List<IMimeType> localeMimeList = new ArrayList<IMimeType>();
    localeMimeList.add( new MimeType( "text/locale", "locale" ) );
    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    LocaleImportHandler localeImportHandler = new LocaleImportHandler( localeMimeList, null );
    LocaleImportHandler localeImportHandlerSpy = spy( localeImportHandler );
    handlers.add( localeImportHandlerSpy );
    importer = new PentahoPlatformImporter( handlers, converterHandler );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );

    ByteArrayInputStream localePropertiesContent = new ByteArrayInputStream(
        ( "description=This runs a simple Kettle transformation filtering records from the Quandrant_Actuals sample data "
            + "table, and sending a Hello message to each position.\nname=1. Hello ETL" )
            .getBytes() );

    ByteArrayInputStream localeContent =
        new ByteArrayInputStream( "file.title=fileTitle\ntitle=SampleTransformation\nfile.description=".getBytes() );

    localeFilesProcessor = spy( new LocaleFilesProcessor() );
    localeFilesProcessor.createLocaleEntry( "/", "file1.properties", null, "description", null, localePropertiesContent, 0 );
    localeFilesProcessor.createLocaleEntry( "/", "file1.locale", null, "description", null, localeContent, 0 );
    int count = localeFilesProcessor.processLocaleFiles( importer );
    System.out.println( count );

    Mockito.verify( localeFilesProcessor, times( 1 ) )
        .proceed( any( IPlatformImporter.class ), any( RepositoryFileImportBundle.Builder.class ), nullable( String.class ),
            any( LocaleFileDescriptor.class ) );
  }

  @Test
  public void testProcessLocaleFilesDontIgnoreProperties() throws Exception {
    IRepositoryContentConverterHandler converterHandler = new DefaultRepositoryContentConverterHandler( new HashMap<String, Converter>() );
    List<IMimeType> localeMimeList = new ArrayList<IMimeType>();
    localeMimeList.add( new MimeType( "text/locale", "locale" ) );
    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    LocaleImportHandler localeImportHandler = new LocaleImportHandler( localeMimeList, null );
    LocaleImportHandler localeImportHandlerSpy = spy( localeImportHandler );
    handlers.add( localeImportHandlerSpy );
    importer = new PentahoPlatformImporter( handlers, converterHandler );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );

    ByteArrayInputStream localePropertiesContent = new ByteArrayInputStream(
        ( "description=This runs a simple Kettle transformation filtering records from the Quandrant_Actuals sample data "
            + "table, and sending a Hello message to each position.\nname=1. Hello ETL" )
            .getBytes() );

    localeFilesProcessor = spy( new LocaleFilesProcessor() );
    localeFilesProcessor.createLocaleEntry( "/", "file1.properties", null, "description", null, localePropertiesContent, 0 );

    int count = localeFilesProcessor.processLocaleFiles( importer );
    System.out.println( count );
    Mockito.verify( localeFilesProcessor, times( 1 ) )
        .proceed( any( IPlatformImporter.class ), any( RepositoryFileImportBundle.Builder.class ), nullable( String.class ),
            any( LocaleFileDescriptor.class ) );
  }

  @Test
  public void testProcessLocaleFilesTwoLocaleFiles() throws Exception {
    IRepositoryContentConverterHandler converterHandler =
        new DefaultRepositoryContentConverterHandler( new HashMap<String, Converter>() );

    List<IMimeType> localeMimeList = new ArrayList<IMimeType>();
    localeMimeList.add( new MimeType( "text/locale", "locale" ) );

    LocaleImportHandler localeImportHandler = new LocaleImportHandler( localeMimeList, null );
    LocaleImportHandler localeImportHandlerSpy = spy( localeImportHandler );
    Log log = mock( Log.class );
    doReturn( log ).when( localeImportHandlerSpy ).getLogger();

    String localeFileName = "SampleTransformationWithParameters";
    List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();
    handlers.add( localeImportHandlerSpy );

    importer = new PentahoPlatformImporter( handlers, converterHandler );
    importer.setRepositoryImportLogger( new Log4JRepositoryImportLogger() );

    localeFilesProcessor = new LocaleFilesProcessor();

    StringBuffer localePropertiesContent = new StringBuffer();
    localePropertiesContent.append(
        "description=This runs a simple Kettle transformation filtering records from the Quandrant_Actuals sample data "
            + "table, and sending a Hello message to each position.\n" );
    localePropertiesContent.append( "name=1. Hello ETL" );

    assertTrue( processIsLocalFile( "SampleTransformation.properties", localePropertiesContent ) );

    StringBuffer localeContent = new StringBuffer();
    localeContent.append( "file.title=" + localeFileName + "\n" );
    localeContent.append( "title=SampleTransformation\n" );
    localeContent.append( "file.description=" );

    assertTrue( processIsLocalFile( "SampleTransformation.xaction.locale", localeContent ) );

    int count = localeFilesProcessor.processLocaleFiles( importer );
    System.out.println( count );
    //verify that in case of both .properties and .locale files are at input the only one proceeded is .locale
    Mockito.verify( localeImportHandlerSpy, times( 1 ) ).importFile( any( IPlatformImportBundle.class ) );
    ArgumentCaptor<IPlatformImportBundle> argument = ArgumentCaptor.forClass( IPlatformImportBundle.class );
    Mockito.verify( localeImportHandlerSpy ).importFile( argument.capture() );
    assertEquals( localeFileName, argument.getValue().getName() );
  }

  @Test
  public void isXMLLocaleTest() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<index>"
        + "<name>the name</name>"
        + "<description>the description</description>"
        + "</index>";
    localeFilesProcessor = new LocaleFilesProcessor();
    assertTrue( localeFilesProcessor.isXMLlocale( new ByteArrayInputStream( xml.getBytes() ) ) );
  }

  @Test
  public void isXMLLocaleWrongFormatTest() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<name>the name</name>"
        + "<description>the description</description>";
    localeFilesProcessor = new LocaleFilesProcessor();
    assertFalse( localeFilesProcessor.isXMLlocale( new ByteArrayInputStream( xml.getBytes() ) ) );
  }

  @Test
  public void isXMLLocaleEmptyValuesTest() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<index>"
        + "<name></name>"
        + "<description></description>"
        + "</index>";
    localeFilesProcessor = new LocaleFilesProcessor();
    assertFalse( localeFilesProcessor.isXMLlocale( new ByteArrayInputStream( xml.getBytes() ) ) );
  }

  private boolean processIsLocalFile( String fileName, StringBuffer localeContent ) throws Exception {
    RepositoryFile file = new RepositoryFile.Builder( fileName ).build();
    RepositoryFileBundle repoFileBundle =
        new RepositoryFileBundle( file, null, StringUtils.EMPTY, null, DEFAULT_ENCODING, null );
    return localeFilesProcessor.isLocaleFile( repoFileBundle, "/", localeContent.toString().getBytes() );
  }

}
