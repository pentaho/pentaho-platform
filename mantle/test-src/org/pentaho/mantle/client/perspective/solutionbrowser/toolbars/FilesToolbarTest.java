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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Aug 27, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.solutionbrowser.toolbars;

import org.pentaho.mantle.client.solutionbrowser.FileItem;
import org.pentaho.mantle.client.solutionbrowser.IFileItemCallback;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author wseyler
 *
 */
public class FilesToolbarTest extends GWTTestCase {

  /* (non-Javadoc)
   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
   */
  @Override
  public String getModuleName() {

    return "org.pentaho.mantle.MantleApplication"; //$NON-NLS-1$
  }

  public void testCreate() {
    FilesToolbar toolbar = new FilesToolbar(new MockCallback());
    assertNotNull(toolbar);
  }

  /**
   * @author wseyler
   *
   */
  public class MockCallback implements IFileItemCallback {

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#createSchedule(java.lang.String)
     */
    public void createSchedule(String cronExpression) {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#createSchedule()
     */
    public void createSchedule() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#editFile()
     */
    public void editFile() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#getSelectedFileItem()
     */
    public FileItem getSelectedFileItem() {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#loadPropertiesDialog()
     */
    public void loadPropertiesDialog() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#openFile(int)
     */
    public void openFile(int mode) {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.pentaho.mantle.client.solutionbrowser.IFileItemCallback#setSelectedFileItem(org.pentaho.mantle.client.solutionbrowser.FileItem)
     */
    public void setSelectedFileItem(FileItem fileItem) {
      // TODO Auto-generated method stub

    }

  }

}
