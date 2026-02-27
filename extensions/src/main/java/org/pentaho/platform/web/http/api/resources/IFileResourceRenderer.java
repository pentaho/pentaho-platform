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


package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public interface IFileResourceRenderer extends IStreamingAction {

  public void setRepositoryFile( RepositoryFile repositoryFile );

}
