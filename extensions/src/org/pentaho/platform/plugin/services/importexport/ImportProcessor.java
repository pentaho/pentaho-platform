package org.pentaho.platform.plugin.services.importexport;

/**
 * The processor for handling the import process. The processor will be responsible for maintaining an ordered set of
 * {@link ImportHandler}s that will be used to process a set of {@link ImportSource.IRepositoryFileBundle}s
 * provided by the {@link ImportSource}.
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public interface ImportProcessor {
  /**
   * Sets the {@link ImportSource} for this import processor
   *
   * @param importSource
   */
  public void setImportSource(final ImportSource importSource);

  /**
   * Adds an {@link ImportHandler} to the end of the list of Import Handlers. The first ImportHandler added
   * will be the first to get a chance to process the data
   *
   * @param importHandler
   */
  public void addImportHandler(final ImportHandler importHandler);

  /**
   * Performs the import process
   *
   * @throws ImportException indicates an error in import processing
   */
  public void performImport() throws ImportException;
  
  /**
   * Performs the import process 
   * overwrite flag exposed as client selection 
   *
   * @throws ImportException indicates an error in import processing
   */
  public void performImport(boolean overwrite) throws ImportException;
}
