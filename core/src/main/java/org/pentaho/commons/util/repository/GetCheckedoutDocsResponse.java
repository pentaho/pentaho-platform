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


package org.pentaho.commons.util.repository;

import org.pentaho.commons.util.repository.type.CmisObject;

import java.util.List;

public class GetCheckedoutDocsResponse {

  private List<CmisObject> docs;

  private boolean hasMoreItems;

  public List<CmisObject> getDocs() {
    return docs;
  }

  public void setDocs( List<CmisObject> docs ) {
    this.docs = docs;
  }

  public boolean isHasMoreItems() {
    return hasMoreItems;
  }

  public void setHasMoreItems( boolean hasMoreItems ) {
    this.hasMoreItems = hasMoreItems;
  }

}
