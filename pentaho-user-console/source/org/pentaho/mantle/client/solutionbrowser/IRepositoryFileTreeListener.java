package org.pentaho.mantle.client.solutionbrowser;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;

public interface IRepositoryFileTreeListener {
  public void onFetchRepositoryFileTree(RepositoryFileTree fileTree);
  public void beforeFetchRepositoryFileTree();
}
