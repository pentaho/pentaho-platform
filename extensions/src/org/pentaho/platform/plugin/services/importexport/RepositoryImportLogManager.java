package org.pentaho.platform.plugin.services.importexport;

import java.io.OutputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

public class RepositoryImportLogManager {
  static HashMap<String,RepositoryImportLog> repositoryImportLogs = new HashMap<String,RepositoryImportLog>();
	
	public static Logger getLogger(OutputStream outputStream, String importRootPath){
		RepositoryImportLog importLog = new RepositoryImportLog(outputStream, importRootPath);
		repositoryImportLogs.put(Thread.currentThread().getName(), importLog);
		getRepositoryImportLog().setCurrentFilePath(importRootPath);
		getLogger().info("Start Import Job");
		return getLogger();
	}
	
	public static Logger getLogger() {
		RepositoryImportLog repositoryImportLog = repositoryImportLogs.get(Thread.currentThread().getName());
		return repositoryImportLog.getLogger();
	}
	
	/**
	 * @return the currentFilePath
	 */
	public static String getCurrentFilePath() {
		return getRepositoryImportLog().getCurrentFilePath();
	}

	/**
	 * @param currentFilePath
	 *          the currentFilePath to set
	 */
	public static void setCurrentFilePath(String currentFilePath) {
		checkRepositoryImportLog();
		getRepositoryImportLog().setCurrentFilePath(currentFilePath);
	}
	
	private static RepositoryImportLog getRepositoryImportLog() {
		checkRepositoryImportLog();
		return repositoryImportLogs.get(Thread.currentThread().getName());
	}
	
	private static void checkRepositoryImportLog() {
		if (repositoryImportLogs.get(Thread.currentThread().getName()) == null) {
			throw new RuntimeException("No import log associated with this thread.  Use getLogger(OutputStream) to create one.");
		}
	}
	
	public static void endJob() {
		checkRepositoryImportLog();
		getRepositoryImportLog().setCurrentFilePath(getRepositoryImportLog().getImportRootPath());
		getLogger().info("End Import Job");
		getRepositoryImportLog().EndJob();
		repositoryImportLogs.remove(Thread.currentThread().getName());
	}
}
