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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;

/**
 * User: nbaker
 * Date: 6/25/12
 */
public class MetadataImportHandlerTest {

  IRepositoryImportLogger importLogger;

  Map<String, IPlatformImportHandler> handlers = new HashMap<String, IPlatformImportHandler>();

  Mockery context;

  IPentahoMetadataDomainRepositoryImporter metadataImporter;

  MetadataImportHandler metadataHandler;

  PentahoPlatformImporter importer;

  @Before
  public void setUp() throws Exception {
    // mock logger to prevent npe
    importLogger = new Log4JRepositoryImportLogger();

    context = new Mockery();
    metadataImporter = context.mock(IPentahoMetadataDomainRepositoryImporter.class);

    metadataHandler = new MetadataImportHandler(metadataImporter);
    handlers.put("text/xmi+xml", metadataHandler);

    Map<String, String> mimes = new HashMap<String, String>();
    mimes.put("xmi", "text/xmi+xml");
    importer = new PentahoPlatformImporter(handlers, new NameBaseMimeResolver(mimes));
    importer.setRepositoryImportLogger(importLogger);
  }

  @Test
  public void testDomainOnlyImport() throws Exception {
    FileInputStream in = new FileInputStream(new File("test-res/ImportTest/steel-wheels.xmi"));

    // With custom domain id
    final IPlatformImportBundle bundle1 = (new RepositoryFileImportBundle.Builder().input(in).charSet("UTF-8").mime("text/xmi+xml").hidden(false).overwriteFile(true).name("steel-wheels.xmi").comment("Test Metadata Import").withParam("domain-id", "parameterized-domain-id")).build();

    context.checking(new Expectations() {{
      oneOf(metadataImporter).storeDomain(with(any(InputStream.class)), with(equal("parameterized-domain-id")), with(equal(true)));
    }});

    importer.importFile(bundle1);

    context.assertIsSatisfied();
  }

  @Test
  public void testDomainWithLocaleFiles() throws Exception {
    final FileInputStream propIn = new FileInputStream(new File("test-res/ImportTest/steel-wheels_en.properties"));
    final IPlatformImportBundle localizationBundle = new RepositoryFileImportBundle.Builder()
        .input(propIn)
        .charSet("UTF-8")
        .hidden(false)
        .name("steel-wheels_en.properties")
        .build();

    final IPlatformImportBundle localizationBundle2 = new RepositoryFileImportBundle.Builder()
        .input(propIn)
        .charSet("UTF-8")
        .hidden(false)
        .name("steel-wheels_en_US.properties")
        .build();

    final FileInputStream in = new FileInputStream(new File("test-res/ImportTest/steel-wheels.xmi"));
    final IPlatformImportBundle bundle = new RepositoryFileImportBundle.Builder()
        .input(in)
        .charSet("UTF-8")
        .hidden(false)
        .overwriteFile(true)
        .mime("text/xmi+xml")
        .name("steel-wheels.xmi")
        .comment("Test Metadata Import")
        .withParam("domain-id", "steel-wheels")
        .addChildBundle(localizationBundle)
        .addChildBundle(localizationBundle2)
        .build();
    
    context.checking(new Expectations() {{
      oneOf(metadataImporter).storeDomain(with(any(InputStream.class)), with(equal("steel-wheels")), with(equal(true)));
      atLeast(1).of (metadataImporter).addLocalizationFile("steel-wheels", "en", propIn, true);
      atLeast(1).of (metadataImporter).addLocalizationFile("steel-wheels", "en_US", propIn, true);
    }});

    importer.importFile(bundle);

    context.assertIsSatisfied();
  }
}
