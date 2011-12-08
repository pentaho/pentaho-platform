/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created April 7th, 2008 
 * @author Will Gorman (wgorman@pentaho.com)
 */
package org.pentaho.platform.config;

import org.pentaho.platform.api.util.IPasswordService;

public class PasswordServiceFactory {
  
  private static final String DEFAULT_IMPL = "org.pentaho.platform.util.Base64PasswordService";  //$NON-NLS-1$
  private static IPasswordService currentService;

  static {
    init(DEFAULT_IMPL);
  }
  
  public synchronized static void init(String classname) {
    try {
      currentService = (IPasswordService)Class.forName(classname).newInstance();
    } catch (Exception e) {
      // wrap this as a runtime exception.  This type of error is configuration related
      throw new RuntimeException(e);
    }
  }
  
  /**
   * returns the current implementation of IPasswordService
   *
   * @return datasource service
   * 
   * @throws RuntimeException if class cannot be instantiated
   */
  public static IPasswordService getPasswordService() {
    return currentService;
  }
}
