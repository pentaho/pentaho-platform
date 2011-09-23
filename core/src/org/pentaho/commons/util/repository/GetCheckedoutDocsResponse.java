/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2010 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jan, 2010 
 * @author James Dixon
 * 
 */
package org.pentaho.commons.util.repository;

import java.util.List;

import org.pentaho.commons.util.repository.type.CmisObject;

public class GetCheckedoutDocsResponse {

  private List<CmisObject> docs;
  
  private boolean hasMoreItems;

  public List<CmisObject> getDocs() {
    return docs;
  }

  public void setDocs(List<CmisObject> docs) {
    this.docs = docs;
  }

  public boolean isHasMoreItems() {
    return hasMoreItems;
  }

  public void setHasMoreItems(boolean hasMoreItems) {
    this.hasMoreItems = hasMoreItems;
  }
  
}
