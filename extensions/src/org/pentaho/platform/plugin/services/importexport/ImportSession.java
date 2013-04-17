package org.pentaho.platform.plugin.services.importexport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler;

/**
 * General purpose objects whose lifecycle is that of an formal import session
 * 
 * @author TKafalas
 *
 */
public class ImportSession {

  private HashSet<String> skippedFiles = new HashSet<String>(); //Files skipped due to overwriteFlag = false
  private Log log;
  private Boolean isNotRunningImport = true;

  public Log getLogger() {
    if (log == null) {
      IRepositoryImportLogger logger = PentahoSystem.get(IPlatformImporter.class).getRepositoryImportLogger();
      if (logger != null && logger.hasLogger()) {
        //An import is running from the /repo/file/import endpoint
        log = logger;
        isNotRunningImport = false;
      } else {
        //A publish is running from some other endpoint
        log = LogFactory.getLog(RepositoryFileImportFileHandler.class);
        isNotRunningImport = true;
      }
    }
    return log;
  }

  /**
   * @return the skippedFiles
   */
  public HashSet<String> getSkippedFiles() {
    return skippedFiles;
  }

  /**
   * @param skippedFiles the skippedFiles to set
   */
  public void setSkippedFiles(HashSet<String> skippedFiles) {
    this.skippedFiles = skippedFiles;
  }

  /**
   * @return the isNotRunningImport
   */
  public Boolean getIsNotRunningImport() {
    return isNotRunningImport;
  }
  
  
}
