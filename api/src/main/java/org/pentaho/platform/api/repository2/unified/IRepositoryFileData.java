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


package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;

/**
 * The data or payload of a {@link RepositoryFile}.
 * 
 * @author mlowery
 */

public interface IRepositoryFileData extends Serializable {

  public static final String NODE_CONTENT_TYPE = "node"; //$NON-NLS-1$

  public static final String SAMPLE_CONTENT_TYPE = "sample"; //$NON-NLS-1$

  public static final String SIMPLE_CONTENT_TYPE = "simple"; //$NON-NLS-1$

  public long getDataSize();
}
