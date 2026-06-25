/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.mantle.client.solutionbrowser;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;

import java.util.List;

/**
 * Provides the currently selected file as an {@link IFileSummary}.
 * 
 * @author mlowery
 */
public interface IRepositoryFileProvider {
  List<RepositoryFile> getRepositoryFiles();
}
