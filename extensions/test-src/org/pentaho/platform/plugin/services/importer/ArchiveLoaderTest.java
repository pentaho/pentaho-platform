package org.pentaho.platform.plugin.services.importer;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;

import java.io.*;

import static org.mockito.Mockito.*;
import static org.pentaho.platform.plugin.services.importer.ArchiveLoader.ZIPS_FILTER;

/**
 * Created with IntelliJ IDEA. User: kwalker Date: 6/20/13 Time: 12:37 PM
 */
public class ArchiveLoaderTest {
  @Test
  public void testWillImportAllZipsInADirectory() throws Exception {
    final IPlatformImporter importer = mock(IPlatformImporter.class);
    final FileInputStream inputStream = mock(FileInputStream.class);
    final ArchiveLoader loader = createArchiveLoader(importer, inputStream);
    final File directory = mock(File.class);
    final File jobs = mock(File.class);
    String jobsName = "jobs.zip";
    when(jobs.getName()).thenReturn(jobsName);
    final File reports = mock(File.class);
    String reportsName = "reports.zip";
    when(reports.getName()).thenReturn(reportsName);
    when(directory.listFiles(ZIPS_FILTER)).thenReturn(new File[] { jobs, reports });
    loader.loadAll(directory, ZIPS_FILTER);
    verify(importer).importFile(argThat(bundleMatcher(jobsName, inputStream)));
    verify(importer).importFile(argThat(bundleMatcher(reportsName, inputStream)));
  }

  @Test
  public void testWillContinueToLoadOnException() throws Exception {
    final IPlatformImporter importer = mock(IPlatformImporter.class);
    final FileInputStream inputStream = mock(FileInputStream.class);
    final ArchiveLoader loader = createArchiveLoader(importer, inputStream);
    final File directory = mock(File.class);
    final File jobs = mock(File.class);
    String jobsName = "jobs.zip";
    when(jobs.getName()).thenReturn(jobsName);
    final File reports = mock(File.class);
    String reportsName = "reports.zip";
    when(reports.getName()).thenReturn(reportsName);
    when(directory.listFiles(ZIPS_FILTER)).thenReturn(new File[] { jobs, reports });
    Exception exception = new RuntimeException();
    doThrow(exception).when(importer).importFile(argThat(bundleMatcher(jobsName, inputStream)));
    IRepositoryImportLogger logger = mock(IRepositoryImportLogger.class);
    when(importer.getRepositoryImportLogger()).thenReturn(logger);
    loader.loadAll(directory, ZIPS_FILTER);
    verify(importer).importFile(argThat(bundleMatcher(jobsName, inputStream)));
    verify(importer).importFile(argThat(bundleMatcher(reportsName, inputStream)));
    verify(logger).error(exception);

  }

  private ArchiveLoader createArchiveLoader(final IPlatformImporter importer, final FileInputStream inputStream) {
    return new ArchiveLoader(importer) {
      @Override
      FileInputStream createInputStream(File file) throws FileNotFoundException {
        return inputStream;
      }
    };
  }

  private ArgumentMatcher<IPlatformImportBundle> bundleMatcher(final String filename, final InputStream inputStream) {
    return new ArgumentMatcher<IPlatformImportBundle>() {
      @Override
      public boolean matches(Object argument) {
        IPlatformImportBundle bundle = (IPlatformImportBundle) argument;
        try {
          return bundle.getName().equals(filename) && bundle.getAcl() == null
              && bundle.getInputStream().equals(inputStream);
        } catch (IOException e) {
          return false;
        }
      }
    };
  }
}
