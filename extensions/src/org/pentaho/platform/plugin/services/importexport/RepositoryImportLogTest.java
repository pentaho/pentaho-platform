package org.pentaho.platform.plugin.services.importexport;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;

public class RepositoryImportLogTest {
	public static void main(String[] args) {
    Thread t1 = new Thread(new RepositoryImportLogTest.TestRun("bin/logOutput1.html","1"));
    Thread t2 = new Thread(new RepositoryImportLogTest.TestRun("bin/logOutput2.html","2"));
    t1.start();
		t2.start();
	}
	
	public static class TestRun implements Runnable {
		String outputFile;
		String threadNumber;
		
		TestRun(String outputFile, String threadNumber) {
			this.outputFile = outputFile;
			this.threadNumber = threadNumber;
		}

		@Override
		public void run() {
			Logger logger = null;
			try {
				FileOutputStream fileStream = new FileOutputStream(outputFile);
				logger = RepositoryImportLogManager.getLogger(fileStream, mainDir());
				
				RepositoryImportLogManager.setCurrentFilePath(mainDir() + "/dir2/file1");
				logger.info("Start Import");
				logger.info("Success");
				
				RepositoryImportLogManager.setCurrentFilePath(mainDir() + "/dir2/file2");
				logger.info("Start Import");

				//Simulate an exception
				try {
					throw new RuntimeException("forced exception");
				} catch (Exception e) {
					logger.error(e);
				}
				//End of job
				RepositoryImportLogManager.endJob();

			} catch (Exception e) {
				System.out.println("Exception is = " + e.getMessage());
			}
		}
		
		private String mainDir() {
			return "/dir" + threadNumber;
		}
		
	}
}
