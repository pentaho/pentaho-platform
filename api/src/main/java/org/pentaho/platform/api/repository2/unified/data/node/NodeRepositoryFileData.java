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


package org.pentaho.platform.api.repository2.unified.data.node;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

public class NodeRepositoryFileData implements IRepositoryFileData {

  private static final long serialVersionUID = 3986247263739435232L;

  private DataNode node;
  private long dataSize = 0;

  public NodeRepositoryFileData( DataNode node ) {
    super();
    this.node = node;
  }

  public NodeRepositoryFileData( DataNode node, long dataSize ) {
    this( node );
    this.dataSize = dataSize;
  }

  public DataNode getNode() {
    return node;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.IRepositoryFileData#getDataSize()
   */
  @Override
  public long getDataSize() {
    return dataSize;
  }

}
