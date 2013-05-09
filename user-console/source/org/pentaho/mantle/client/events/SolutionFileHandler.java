package org.pentaho.mantle.client.events;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;

/**
 * @author Rowell Belen
 */
public interface SolutionFileHandler {
  void handle(RepositoryFile repositoryFile);
}
