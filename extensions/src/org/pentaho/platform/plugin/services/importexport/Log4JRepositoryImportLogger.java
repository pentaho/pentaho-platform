package org.pentaho.platform.plugin.services.importexport;

import java.io.OutputStream;
import org.apache.log4j.Logger;

/**
 * {@inherit}
 * 
 * @author TKafalas
 * 
 */
public class Log4JRepositoryImportLogger implements IRepositoryImportLogger {

	private ThreadLocal<Log4JRepositoryImportLog> repositoryImportLog = new ThreadLocal<Log4JRepositoryImportLog>();

	public Log4JRepositoryImportLogger() {
	}

	public void startJob(OutputStream outputStream, String importRootPath) {
		repositoryImportLog.set(new Log4JRepositoryImportLog(outputStream,
				importRootPath));
		getLog4JRepositoryImportLog().setCurrentFilePath(
				getLog4JRepositoryImportLog().getImportRootPath());
		getLogger().info("Start Import Job");
	}

	public void endJob() {
		getLogger().info("End Import Job");
		getLog4JRepositoryImportLog().setCurrentFilePath(
				getLog4JRepositoryImportLog().getImportRootPath());
		getLog4JRepositoryImportLog().EndJob();
	}

	public void setCurrentFilePath(String currentFilePath) {
		getLog4JRepositoryImportLog().setCurrentFilePath(currentFilePath);
		getLogger().info("Start File Import");
	}

	public void info(String s) {
		getLogger().info(s);
	}

	public void error(String s) {
		getLogger().error(s);
	}

	public void debug(String s) {
		getLogger().debug(s);
	}

	public void warn(String s) {
		getLogger().debug(s);
	}

	@Override
	public void error(Exception e) {
		getLogger().error(e);

	}

	private Log4JRepositoryImportLog getLog4JRepositoryImportLog() {
		Log4JRepositoryImportLog currentLog = repositoryImportLog.get();
		if (currentLog == null) {
			throw new IllegalStateException("No job started for current Thread");
		}
		return currentLog;
	}

	private Logger getLogger() {
		return getLog4JRepositoryImportLog().getLogger();
	}

}
