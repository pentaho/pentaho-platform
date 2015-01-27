/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.plugin.services.importer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;


public class LocaleImportHandlerTest {

	private static final String DEFAULT_ENCODING = "UTF-8";
	
	PentahoPlatformImporter importer;
	LocaleFilesProcessor localeFilesProcessor;

	@Before
	public void setUp() throws Exception {

		MicroPlatform microPlatform = new MicroPlatform();
		Mockery context = new Mockery();

		Map<String, String> mimeMap = new HashMap<String, String>();
		mimeMap.put("locale", "text/locale");
		microPlatform.defineInstance(NameBaseMimeResolver.class, new NameBaseMimeResolver(mimeMap));

		IPlatformImportMimeResolver nameBaseMimeResolver = context.mock(IPlatformImportMimeResolver.class);
		microPlatform.defineInstance(IPlatformImportMimeResolver.class, nameBaseMimeResolver);

		List<String> allowedArtifacts = new ArrayList<String>();
		allowedArtifacts.add("xaction");
		allowedArtifacts.add("url");
		
		List<String> approvedExtensionsList = new ArrayList<String>();
    approvedExtensionsList.add(".prpt");
    approvedExtensionsList.add(".xaction");

		List<String> hiddenExtensionsList = new ArrayList<String>();
    hiddenExtensionsList.add(".xml");
    hiddenExtensionsList.add(".png");
    
    	

		LocaleImportHandler localeImportHandler = new LocaleImportHandler(allowedArtifacts, approvedExtensionsList, hiddenExtensionsList);
		LocaleImportHandler spylocaleImportHandler = spy( localeImportHandler );
		Log log = mock( Log.class );
		doReturn( log ).when( spylocaleImportHandler ).getLogger();

		Map<String, IPlatformImportHandler> handlers = new HashMap<String, IPlatformImportHandler>();
		handlers.put("text/locale", localeImportHandler);
		

		Map<String, String> mimes = new HashMap<String, String>();
		mimes.put("locale", "text/locale");
		importer = new PentahoPlatformImporter(handlers, new NameBaseMimeResolver(mimes));
		importer.setRepositoryImportLogger(new Log4JRepositoryImportLogger());
	}

	@Test
	public void testImportLocaleFiles() throws Exception {

		StringBuffer localeContent = new StringBuffer();
		localeContent.append("name=Test");
		localeContent.append("\n");
		localeContent.append("description=Test description");

		RepositoryFile file = new RepositoryFile.Builder("test.properties").build();
		RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(file, null, "", null, "UTF-8", null);

		LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
		localeFilesProcessor.isLocaleFile(repoFileBundle, "/", localeContent.toString().getBytes());
		
		assertTrue( processIsLocalFile( "test.properties", localeContent ) );
		assertFalse( processIsLocalFile( "test.bla", localeContent ) );
	
		localeContent = new StringBuffer( "bla bla" );
		assertFalse( processIsLocalFile( "test.properties", localeContent ) );

		localeFilesProcessor.processLocaleFiles(importer);
	}
	
	@Test
	public void testValidImportIndexLocaleFile() {
		String localeContent =
				"<index><name>My name</name><description>My descript</description><icon>samples.png</icon><visible>true</visible><display-type>icons</display-type></index>";
		RepositoryFileImportBundle importBundle = createBundle( localeContent, "index.xml" );

		IUnifiedRepository unifiedRepository = initLocaleHandler( importBundle );

		try {
			importer.importFile( importBundle );

			verify( unifiedRepository, times( 1 ) ).getFile( anyString() );
			verify( unifiedRepository, never() ).getChildren( anyInt() );
			verify( unifiedRepository, times( 1 ) ).setLocalePropertiesForFile( any( RepositoryFile.class ), anyString(),
					any( Properties.class ) );
		} catch ( PlatformImportException e ) {
			fail( e.getMessage() );
		}

	}
 
	@Test
	public void testInValidImportIndexLocaleFile() {
		String localeContent =
				"<index><name>%name</name><description>%description</description><icon>samples.png</icon><visible>true</visible><display-type>icons</display-type></index>";
		RepositoryFileImportBundle importBundle = createBundle( localeContent, "index.xml" );
 
		IUnifiedRepository unifiedRepository = initLocaleHandler( importBundle );
 
		try {
			importer.importFile( importBundle );
 
			verify( unifiedRepository, times( 1 ) ).getFile( anyString() );
			verify( unifiedRepository, times( 1 ) ).getChildren( anyInt() );
			verify( unifiedRepository, never() ).setLocalePropertiesForFile( any( RepositoryFile.class ), anyString(),
					any( Properties.class ) );
		} catch ( PlatformImportException e ) {
			fail( e.getMessage() );
		}
 
	}
 
	@Test
	public void testImportNotLocaleFile() {
		String localeContent = "<index></display-type></index>";
		RepositoryFileImportBundle importBundle = createBundle( localeContent, "test.xml" );
 
		IUnifiedRepository unifiedRepository = initLocaleHandler( importBundle );
 
		try {
			importer.importFile( importBundle );
 
			verify( unifiedRepository, times( 1 ) ).getFile( anyString() );
			verify( unifiedRepository, times( 1 ) ).getChildren( anyInt() );
			verify( unifiedRepository, never() ).setLocalePropertiesForFile( any( RepositoryFile.class ), anyString(),
					any( Properties.class ) );
		} catch ( PlatformImportException e ) {
			fail( e.getMessage() );
		}
 
	}
 
	private IUnifiedRepository initLocaleHandler( RepositoryFileImportBundle importBundle ) {
		IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
		when( unifiedRepository.getFile( importBundle.getPath() ) ).thenReturn( importBundle.getFile() );
		
		return unifiedRepository;
	}
 
	private RepositoryFileImportBundle createBundle( String localeContent, String fileName ) {
		InputStream in = new ByteArrayInputStream( localeContent.getBytes() );
 
		RepositoryFile repoFile = new RepositoryFile.Builder( fileName ).build();
 
		RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
		bundleBuilder.path( "/pentaho-solutions/my-test/" + fileName );
		bundleBuilder.mime( "text/locale" );
		bundleBuilder.input( in );
		bundleBuilder.charSet( DEFAULT_ENCODING );
		bundleBuilder.overwriteFile( true );
		bundleBuilder.applyAclSettings( true );
		bundleBuilder.overwriteAclSettings( true );
		bundleBuilder.retainOwnership( false );
		bundleBuilder.name( fileName );
		bundleBuilder.file( repoFile );
 
		RepositoryFileImportBundle importBundle = bundleBuilder.build();
		return importBundle;
	}
	
	private boolean processIsLocalFile( String fileName, StringBuffer localeContent ) throws Exception {
	     RepositoryFile file = new RepositoryFile.Builder( fileName ).build();
	     RepositoryFileBundle repoFileBundle =
	    		 new RepositoryFileBundle( file, null, StringUtils.EMPTY, null, DEFAULT_ENCODING, null );
	     return localeFilesProcessor.isLocaleFile( repoFileBundle, "/", localeContent.toString().getBytes() );
	}
}
