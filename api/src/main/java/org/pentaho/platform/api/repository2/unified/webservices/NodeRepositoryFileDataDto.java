/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.api.repository2.unified.webservices;

import java.io.Serializable;

public class NodeRepositoryFileDataDto implements Serializable {
  private static final long serialVersionUID = -1249741993478762926L;

  public NodeRepositoryFileDataDto() {
    super();
  }

  private DataNodeDto node;

  public DataNodeDto getNode() {
    return node;
  }

  public void setNode( DataNodeDto node ) {
    this.node = node;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "NodeRepositoryFileDataDto [node=" + node + "]";
  }
}
