package org.pentaho.platform.plugin.services.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.platform.plugin.services.importexport.ImportSession;

/**
 * Will import all the zip files in a given directory using the supplied IPlatformImporter
 *
 * User: kwalker
 * Date: 6/20/13
 */
public class ArchiveLoader {
  public static final FilenameFilter ZIPS_FILTER = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(".zip");
    }
  };
  static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(".yyyyMMddHHmm");

  private IPlatformImporter importer;
  private Date loadStamp;

  public ArchiveLoader(IPlatformImporter importer) {
    this(importer, new Date());
  }

  ArchiveLoader(IPlatformImporter importer, Date loadStamp)
  {
    this.importer = importer;
    ImportSession.getSession().setAclProperties(true, true, false);
    this.loadStamp = loadStamp;
  }

  public void loadAll(File directory, FilenameFilter filenameFilter) {
    File[] files = directory.listFiles(filenameFilter);
    if (files != null) {
      for (File file : files) {
        try {
          importer.getRepositoryImportLogger().debug(this.getClass().getName() + ": importing " + file.getName());
          importer.importFile(createBundle(file));
          file.renameTo(new File(file.getPath() + DATE_FORMAT.format(loadStamp)));
        } catch (Exception e) {
          importer.getRepositoryImportLogger().error(e);
        }
      }
    }
  }

  private IPlatformImportBundle createBundle(File file) throws FileNotFoundException {
	    
    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
    bundleBuilder.input(createInputStream(file));
    bundleBuilder.charSet("UTF-8");
    bundleBuilder.hidden(false);
    bundleBuilder.path("/");
    bundleBuilder.overwriteFile(false);
    bundleBuilder.name(file.getName());
    bundleBuilder.applyAclSettings(true);
    bundleBuilder.overwriteAclSettings(false);
    bundleBuilder.retainOwnership(true);
    return bundleBuilder.build();
  }

  FileInputStream createInputStream(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }
}
