package org.pentaho.mantle.client.solutionbrowser;

import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;

public interface IRepositoryFileTreeListener {
  public void onFetchRepositoryFileTree(RepositoryFileTree fileTree, List<RepositoryFile> trashItems);
  public void beforeFetchRepositoryFileTree();
}
