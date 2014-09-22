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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: nbaker Date: 6/25/12
 */
public class MetadataImportHandlerTest {

  IRepositoryImportLogger importLogger;

  List<IPlatformImportHandler> handlers = new ArrayList<IPlatformImportHandler>();

  Mockery context;

  IPentahoMetadataDomainRepositoryImporter metadataImporter;

  MetadataImportHandler metadataHandler;

  PentahoPlatformImporter importer;

  @Before
  public void setUp() throws Exception {
    // mock logger to prevent npe
    importLogger = new Log4JRepositoryImportLogger();

    context = new Mockery();
    metadataImporter = context.mock( IPentahoMetadataDomainRepositoryImporter.class );

    MicroPlatform microPlatform = new MicroPlatform();
    NameBaseMimeResolver nameResolver = new NameBaseMimeResolver();
    microPlatform.defineInstance( IPlatformImportMimeResolver.class, nameResolver );

    List<MimeType> mimeList = new ArrayList<MimeType>();
    mimeList.add( new MimeType( "text/xmi+xml", "xmi" ) );

    metadataHandler = new MetadataImportHandler( mimeList, metadataImporter );

    handlers.add( metadataHandler );

    importer =
        new PentahoPlatformImporter( handlers, new DefaultRepositoryContentConverterHandler(
            new HashMap<String, Converter>() ) );
    importer.setRepositoryImportLogger( importLogger );
  }

  @Test
  public void testDomainOnlyImport() throws Exception {
    FileInputStream in = new FileInputStream( new File( "test-res/ImportTest/steel-wheels.xmi" ) );

    // With custom domain id
    final IPlatformImportBundle bundle1 =
        ( new RepositoryFileImportBundle.Builder().input( in ).charSet( "UTF-8" ).mime( "text/xmi+xml" ).hidden( false )
            .overwriteFile( true ).name( "steel-wheels.xmi" ).comment( "Test Metadata Import" ).withParam( "domain-id",
                "parameterized-domain-id" ) ).build();

    context.checking( new Expectations() {
      {
        oneOf( metadataImporter ).storeDomain( with( any( InputStream.class ) ),
            with( equal( "parameterized-domain-id" ) ), with( equal( true ) ) );
      }
    } );

    importer.importFile( bundle1 );

    context.assertIsSatisfied();
  }

  @Test
  public void testDomainWithLocaleFiles() throws Exception {
    final FileInputStream propIn = new FileInputStream( new File( "test-res/ImportTest/steel-wheels_en.properties" ) );
    final IPlatformImportBundle localizationBundle =
        new RepositoryFileImportBundle.Builder().input( propIn ).charSet( "UTF-8" ).hidden( false ).name(
            "steel-wheels_en.properties" ).build();

    final IPlatformImportBundle localizationBundle2 =
        new RepositoryFileImportBundle.Builder().input( propIn ).charSet( "UTF-8" ).hidden( false ).name(
            "steel-wheels_en_US.properties" ).build();

    final FileInputStream in = new FileInputStream( new File( "test-res/ImportTest/steel-wheels.xmi" ) );
    final IPlatformImportBundle bundle =
        new RepositoryFileImportBundle.Builder().input( in ).charSet( "UTF-8" ).hidden( false ).overwriteFile( true )
            .mime( "text/xmi+xml" ).name( "steel-wheels.xmi" ).comment( "Test Metadata Import" ).withParam(
            "domain-id", "steel-wheels" ).addChildBundle( localizationBundle ).addChildBundle( localizationBundle2 )
            .build();

    context.checking( new Expectations() {
      {
        oneOf( metadataImporter ).storeDomain( with( any( InputStream.class ) ), with( equal( "steel-wheels" ) ),
            with( equal( true ) ) );
        atLeast( 1 ).of( metadataImporter ).addLocalizationFile( "steel-wheels", "en", propIn, true );
        atLeast( 1 ).of( metadataImporter ).addLocalizationFile( "steel-wheels", "en_US", propIn, true );
      }
    } );

    importer.importFile( bundle );

    context.assertIsSatisfied();
  }

  @Test
  public void testPreserveDsw() throws Exception {
    final String domainId = "AModel.xmi";
    final InputStream fileIn = new FileInputStream( new File( "test-res/ImportTest/AModel.xmi" ) );
    try {
      final IPlatformImportBundle bundle = getMetadataImport( domainId, fileIn ).preserveDsw( true ).build();
      context.checking( new Expectations() {
        {
          oneOf( metadataImporter )
              .storeDomain( with( xmiInputStreamHasOlap( true ) ), with( equal( domainId ) ), with( equal( true ) ) );
        }
      } );
      importer.importFile( bundle );
      context.assertIsSatisfied();
    } finally {
      IOUtils.closeQuietly( fileIn );
    }
  }

  @Test
  public void testNotPreserveDsw() throws Exception {
    final String domainId = "AModel";
    final InputStream fileIn = new FileInputStream( new File( "test-res/ImportTest/AModel.xmi" ) );
    try {
      final IPlatformImportBundle bundle = getMetadataImport( domainId, fileIn ).build();
      context.checking( new Expectations() {
        {
          oneOf( metadataImporter )
              .storeDomain( with( xmiInputStreamHasOlap( false ) ), with( equal( domainId ) ), with( equal( true ) ) );
        }
      } );
      importer.importFile( bundle );
      context.assertIsSatisfied();
    } finally {
      IOUtils.closeQuietly( fileIn );
    }
  }

  private RepositoryFileImportBundle.Builder getMetadataImport( final String domainId, final InputStream input ) {
    return new RepositoryFileImportBundle.Builder()
        .input( input ).charSet( "UTF-8" )
        .mime( "text/xmi+xml" )
        .hidden( false )
        .overwriteFile( true )
        .withParam( "domain-id", domainId );
  }

  @Factory
  public static Matcher<InputStream> xmiInputStreamHasOlap( boolean yes ) {
    return new XmiInputStreamHasOlap( yes );
  }

  private static class XmiInputStreamHasOlap extends TypeSafeMatcher<InputStream> {
    private XmiParser xmiParser = new XmiParser();
    // not() wasn't working
    private final boolean yes;

    public XmiInputStreamHasOlap( boolean yes ) {
      this.yes = yes;
    }

    @Override
    protected boolean matchesSafely( InputStream in ) {
      try {
        Domain domain = xmiParser.parseXmi( in );
        if ( domain.getLogicalModels().size() > 1
            && domain.getLogicalModels().get( 1 ).getProperty( LogicalModel.PROPERTY_OLAP_DIMS ) != null ) {
          return yes;
        }
        return !yes;
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }

    }

    @Override
    public void describeTo( Description description ) {
      description.appendText( "xmi input stream " + ( yes ? "with" : "without" ) + " an OLAP model" );
    }
  }
}
