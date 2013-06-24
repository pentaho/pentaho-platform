package org.pentaho.platform.plugin.services.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

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

  private IPlatformImporter importer;

  public ArchiveLoader(IPlatformImporter importer) {
    this.importer = importer;
  }


  public void loadAll(File directory, FilenameFilter filenameFilter) {
    File[] files = directory.listFiles(filenameFilter);
  if (files != null && files.length > 0 ) {
	    for (File file : files) {
	      try {
	    	System.out.println(this.getClass().getName() + ": importing " + file.getName());
	        importer.importFile(createBundle(file));
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
    return bundleBuilder.build();
  }

  FileInputStream createInputStream(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }
}
