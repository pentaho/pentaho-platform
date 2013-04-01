package org.pentaho.platform.plugin.services.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.InitializationException;
import org.pentaho.platform.plugin.services.importexport.legacy.ZipSolutionRepositoryImportSource;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public class PRPTImportHandler extends SolutionImportHandler implements IPlatformImportHandler {

  private static final Log log = LogFactory.getLog(PRPTImportHandler.class);

  public PRPTImportHandler(IPlatformImportMimeResolver mimeResolver) {
    super(mimeResolver);

  }

  @Override
  public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    ZipInputStream zipImportStream = new ZipInputStream(bundle.getInputStream());
    SolutionRepositoryImportSource importSource = new SolutionRepositoryImportSource(zipImportStream);
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();

    IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
    for (IRepositoryFileBundle file : importSource.getFiles()) {
      String fileName = file.getFile().getName();
      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
      String repositoryFilePath = RepositoryFilenameUtils.concat(
          PentahoPlatformImporter.computeBundlePath(file.getPath()), fileName);

      // Validate against importing system related artifacts.
      if (isSystemPath(repositoryFilePath)) {
        log.trace("Skipping [" + repositoryFilePath + "], it is in admin / system folders");
        continue;
      }

      byte[] bytes = IOUtils.toByteArray(file.getInputStream());
      InputStream bundleInputStream = new ByteArrayInputStream(bytes);
      if (file.getFile().isFolder()) {
        bundleBuilder.mime("text/directory");
        bundleBuilder.file(file.getFile());
        fileName = repositoryFilePath;
        repositoryFilePath = importBundle.getPath();
      } else {
        // If is locale file store it for later processing.
        //need to extract this from meta.xml
        bundleBuilder.input(bundleInputStream);
        bundleBuilder.mime(mimeResolver.resolveMimeForFileName(fileName));
        String filePath = (file.getPath().equals("/") || file.getPath().equals("\\")) ? "" : file.getPath();
        repositoryFilePath = RepositoryFilenameUtils.concat(importBundle.getPath(), filePath);

        // Process locale file from meta.xml.
        try {
          convertStreamToProperties(localeFilesProcessor, bytes);
        } catch (InitializationException e) {
          log.error(e.getMessage(), e);
          e.printStackTrace();
        } catch (DocumentException e) {
          log.error(e.getMessage(), e);
        }

      }
      bundleBuilder.name(fileName);
      bundleBuilder.path(repositoryFilePath);
      String sourcePath = file.getPath().startsWith("/") ? file.getPath().substring(1) : file.getPath();
      sourcePath = RepositoryFilenameUtils.concat(sourcePath, fileName);

      bundleBuilder.charSet(bundle.getCharset());
      bundleBuilder.overwriteFile(bundle.overwriteInRepository());
      bundleBuilder.hidden(isBlackListed(fileName));
      bundleBuilder.applyAclSettings(bundle.isApplyAclSettings());
      bundleBuilder.retainOwnership(bundle.isRetainOwnership());
      bundleBuilder.overwriteAclSettings(bundle.isOverwriteAclSettings());
      bundleBuilder.acl(processAclForFile(bundle, sourcePath));
      IPlatformImportBundle platformImportBundle = bundleBuilder.build();
      importer.importFile(platformImportBundle);

      if (bundleInputStream != null) {
        bundleInputStream.close();
        bundleInputStream = null;
      }
      localeFilesProcessor.processLocaleFiles(importer);
    }
  }

  /**
   * extract the contents of the file meta.xml and place in the locales process entry
   * @param localeFilesProcessor
   * @param bytes
   * @throws InitializationException
   * @throws IOException
   * @throws DocumentException
   */
  private void convertStreamToProperties(LocaleFilesProcessor localeFilesProcessor, byte[] bytes)
      throws InitializationException, IOException, DocumentException {

    InputStream zipInput = new ByteArrayInputStream(bytes);
    ZipInputStream zipInputStream = new ZipInputStream(zipInput);
    ZipSolutionRepositoryImportSource zip = new ZipSolutionRepositoryImportSource(zipInputStream, "UTF-8");
    for (IRepositoryFileBundle fileBundle : zip.getFiles()) {
      RepositoryFile rf = fileBundle.getFile();
      if (rf.getName().equals("meta.xml")) {
        Document doc = XmlDom4JHelper.getDocFromStream(fileBundle.getInputStream());
        String description = "";
        String title = "";
        String name = "";
        localeFilesProcessor.createLocaleEntry(rf.getPath(), name, title, description, fileBundle.getFile());
        break;
      }
    }

  }
}
