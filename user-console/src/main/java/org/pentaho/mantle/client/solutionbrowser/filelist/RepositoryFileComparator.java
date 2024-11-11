/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.solutionbrowser.filelist;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.TreeItemComparator;

import java.util.Comparator;

/**
 * @author Rowell Belen
 */
public class RepositoryFileComparator implements Comparator<RepositoryFile> {

  private TreeItemComparator comparator = new TreeItemComparator();

  @Override
  public int compare( RepositoryFile repositoryFile, RepositoryFile repositoryFile2 ) {
    return comparator.compare( repositoryFile.getTitle(), repositoryFile2.getTitle() );
  }

}
