package org.pentaho.platform.plugin.services.importer;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.plugin.services.importer.*;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nbaker
 * Date: 6/13/12
 */
public class NewImporterTest {

  @Test
  public void testDomainOnlyImport() throws Exception {
    Map<String, IPlatformImportHandler> handlers = new HashMap<String, IPlatformImportHandler>();

    Mockery context = new Mockery();
    IUnifiedRepository mockRepo = context.mock(IUnifiedRepository.class);
    final IPentahoMetadataDomainRepositoryImporter metadataImporter = context.mock(IPentahoMetadataDomainRepositoryImporter.class);

    final MetadataImportHandler metadataHandler = new MetadataImportHandler(metadataImporter);
    handlers.put("text/xmi+xml", metadataHandler);

    Map<String, String> mimes = new HashMap<String, String>();
    mimes.put("xmi", "text/xmi+xml");
    PentahoPlatformImporter importer = new PentahoPlatformImporter(handlers, new NameBaseMimeResolver(mimes));

    FileInputStream in = new FileInputStream(new File("test-res/ImportTest/steel-wheels.xmi"));

    // With custom domain id
    final IPlatformImportBundle bundle1 = (new RepositoryFileImportBundle.Builder().input(in).charSet("UTF-8").hidden(false).name("steel-wheels.xmi").comment("Test Metadata Import").withParam("domain-id", "parameterized-domain-id")).build();


    context.checking(new Expectations() {{
      oneOf(metadataImporter).storeDomain(bundle1.getInputStream(), "parameterized-domain-id", true);
    }});

    importer.importFile(bundle1);

    context.assertIsSatisfied();

  }

  @Test
  public void testDomainWithLocaleFiles() throws Exception {
    Map<String, IPlatformImportHandler> handlers = new HashMap<String, IPlatformImportHandler>();

    Mockery context = new Mockery();
    IUnifiedRepository mockRepo = context.mock(IUnifiedRepository.class);
    final IPentahoMetadataDomainRepositoryImporter metadataImporter = context.mock(IPentahoMetadataDomainRepositoryImporter.class);

    final MetadataImportHandler metadataHandler = new MetadataImportHandler(metadataImporter);
    handlers.put("text/xmi+xml", metadataHandler);

    Map<String, String> mimes = new HashMap<String, String>();
    mimes.put("xmi", "text/xmi+xml");
    PentahoPlatformImporter importer = new PentahoPlatformImporter(handlers, new NameBaseMimeResolver(mimes));


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
        .name("steel-wheels.xmi")
        .comment("Test Metadata Import")
        .withParam("domain-id", "steel-wheels")
        .addChildBundle(localizationBundle)
        .addChildBundle(localizationBundle2)
        .build();

    context.checking(new Expectations() {{
      oneOf(metadataImporter).storeDomain(in, "steel-wheels", true);
      atLeast(1).of (metadataImporter).addLocalizationFile("steel-wheels", "en", propIn, true);
      atLeast(1).of (metadataImporter).addLocalizationFile("steel-wheels", "en_US", propIn, true);
    }});

    importer.importFile(bundle);

    context.assertIsSatisfied();

  }

}
