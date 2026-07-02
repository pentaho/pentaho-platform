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


package org.pentaho.mantle.client.solutionbrowser;

/**
 * Instances contain vital information about a solution file: its name, solution, path, and localized name. This
 * interface allows {@code FileCommand} to know the file (whether it be a folder in the solution tree or a file in
 * the files list) being acted on.
 * 
 * @author mlowery
 */
public interface IFileSummary {
  String getLocalizedName();

  String getName();

  String getPath();
}
