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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Aug 18, 2005 
 * @author James Dixon
 */

package org.pentaho.test.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.pentaho.platform.util.VersionHelper;

public class VersionHelperTest extends TestCase {

  public void testVersionHelper() {

    VersionHelper vh = new VersionHelper();
    String verInfo = vh.getVersionInformation();
    String verInfo2 = vh.getVersionInformation(this.getClass());

    System.out.println("Version Info   : " + verInfo); //$NON-NLS-1$ 
    System.out.println("Version Info 2 : " + verInfo2); //$NON-NLS-1$ 

    Assert.assertTrue(true);

  }

  public static void main(final String[] args) {
    VersionHelperTest test = new VersionHelperTest();
    try {
      test.testVersionHelper();
    } finally {
    }
  }

}
