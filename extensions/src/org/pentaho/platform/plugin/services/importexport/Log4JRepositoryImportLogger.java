package org.pentaho.platform.plugin.services.importexport;

import java.io.OutputStream;

import org.apache.log4j.Level;
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

	public void startJob(OutputStream outputStream, String importRootPath, Level logLevel) {
		repositoryImportLog.set(new Log4JRepositoryImportLog(outputStream,
				importRootPath, logLevel));
		getLog4JRepositoryImportLog().setCurrentFilePath(
				getLog4JRepositoryImportLog().getImportRootPath());
		getLogger().info("Start Import Job");
	}

	public void endJob() {
	  getLog4JRepositoryImportLog().setCurrentFilePath(
	      getLog4JRepositoryImportLog().getImportRootPath());
		getLogger().info("End Import Job");
		getLog4JRepositoryImportLog().endJob();
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
	
	public boolean hasLogger() {
	  return (repositoryImportLog.get() == null) ? false: true;
	}

  @Override
  public void debug(Object arg0) {
    getLogger().debug(arg0);
  }

  @Override
  public void debug(Object arg0, Throwable arg1) {
    getLogger().debug(arg0, arg1);
  }

  @Override
  public void error(Object arg0) {
    getLogger().error(arg0);
  }

  @Override
  public void error(Object arg0, Throwable arg1) {
    getLogger().error(arg0, arg1);
    
  }

  @Override
  public void fatal(Object arg0) {
    getLogger().fatal(arg0);
    
  }

  @Override
  public void fatal(Object arg0, Throwable arg1) {
    getLogger().fatal(arg0, arg1);
    
  }

  @Override
  public void info(Object arg0) {
    getLogger().info(arg0);
    
  }

  @Override
  public void info(Object arg0, Throwable arg1) {
    getLogger().info(arg0, arg1);
    
  }

  @Override
  public boolean isDebugEnabled() {
    return getLogger().isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return Level.ERROR.isGreaterOrEqual(getLogger().getLevel());
  }

  @Override
  public boolean isFatalEnabled() {
    return Level.FATAL.isGreaterOrEqual(getLogger().getLevel());
  }

  @Override
  public boolean isInfoEnabled() {
    return getLogger().isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return getLogger().isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return Level.WARN.isGreaterOrEqual(getLogger().getLevel());
  }

  @Override
  public void trace(Object arg0) {
    getLogger().trace(arg0);
  }

  @Override
  public void trace(Object arg0, Throwable arg1) {
    getLogger().trace(arg0, arg1);
  }

  @Override
  public void warn(Object arg0) {
    getLogger().warn(arg0);
  }

  @Override
  public void warn(Object arg0, Throwable arg1) {
    getLogger().warn(arg0,arg1);
  }

}
