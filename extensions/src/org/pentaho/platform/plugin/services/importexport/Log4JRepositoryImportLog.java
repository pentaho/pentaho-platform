package org.pentaho.platform.plugin.services.importexport;

import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.WriterAppender;

public class Log4JRepositoryImportLog {
	
	Logger logger; 
	static final String FILE_KEY = "currentFile";
	OutputStream outputStream;
	String currentFilePath;
	String logName;
	String importRootPath;
	Level logLevel;
	WriterAppender writeAppender;

	/**
	 * Constructs an object that keeps track of additional fields for Log4j
	 * logging and writes/formats an html file to the output stream provided.
	 * 
	 * @param outputStream
	 */
	Log4JRepositoryImportLog(OutputStream outputStream, String importRootPath, Level logLevel) {
		this.outputStream = outputStream;
		this.importRootPath = importRootPath;
		init();
	}

	private void init() {
		logName = "RepositoryImportLog." + getThreadName();
		logger = Logger.getLogger(logName);
		logger.setLevel(logLevel);
		RepositoryImportHTMLLayout htmlLayout = new RepositoryImportHTMLLayout();
		htmlLayout.setTitle("Repository Import Log");
		htmlLayout.setLocationInfo(true);
		writeAppender = new WriterAppender(htmlLayout, outputStream);
		logger.addAppender(writeAppender);
	}
	
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @return the currentFilePath
	 */
	public String getCurrentFilePath() {
		return currentFilePath;
	}

	/**
	 * @param currentFilePath
	 *          the currentFilePath to set
	 */
	public void setCurrentFilePath(String currentFilePath) {
		this.currentFilePath = currentFilePath;
		MDC.put(FILE_KEY, currentFilePath);
	}
	
	/**
	 * @return the importRootPath
	 */
	public String getImportRootPath() {
		return importRootPath;
	}
	
	protected void endJob() {
		try {
			outputStream.write(writeAppender.getLayout().getFooter().getBytes());
		} catch (Exception e) {
			System.out.println(e);
			//Don't try logging a log error.
		}
		logger.removeAppender(logName);
	}
	
	private String getThreadName(){
		return Thread.currentThread().getName();
	}

}

