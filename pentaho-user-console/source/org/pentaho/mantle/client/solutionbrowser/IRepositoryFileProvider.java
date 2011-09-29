package org.pentaho.mantle.client.solutionbrowser;

import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;



/**
 * Provides the currently selected file as an {@link IFileSummary}.
 * 
 * @author mlowery
 */
public interface IRepositoryFileProvider {
  List<RepositoryFile> getRepositoryFiles();
}
