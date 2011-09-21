/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created on Apr 24, 2005
 */
package org.pentaho.platform.repository.solution.filebased;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.pentaho.platform.api.engine.ILogger;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FileIterator implements Iterator {

  private List nodeList;

  private Iterator nodeIterator;

  private ILogger logger;

  public FileIterator(final List nodeList, final ILogger logger) {
    this.nodeList = nodeList;
    this.logger = logger;
    if (nodeList == null) {
      nodeIterator = null;
    } else {
      nodeIterator = nodeList.iterator();
    }
  }

  public boolean hasNext() {
    if (nodeIterator != null) {
      return nodeIterator.hasNext();
    }
    return false;
  }

  public Object next() {
    if (nodeIterator != null) {
      Element node = (Element) nodeIterator.next();
      FileInfo fileInfo = new FileInfo(node, logger);
      return fileInfo;
    }
    return null;
  }

  public FileInfo nextFile() {
    if (nodeIterator != null) {
      Element node = (Element) nodeIterator.next();
      FileInfo fileInfo = new FileInfo(node, logger);
      return fileInfo;
    }
    return null;

  }

  public void remove() {
    if (nodeIterator != null) {
      nodeIterator.remove();
    }
  }

  public int size() {
    if (nodeList != null) {
      return nodeList.size();
    }
    return 0;
  }
}
