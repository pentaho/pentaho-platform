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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jul 26, 2009 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.web.http.session;

import java.io.Serializable;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.pentaho.platform.api.util.ITempFileDeleter;
import org.pentaho.platform.engine.core.system.StandaloneTempFileDeleter;

public class SessionTempFileDeleter extends StandaloneTempFileDeleter implements HttpSessionBindingListener, Serializable, ITempFileDeleter  {

  private static final long serialVersionUID = 1379936698516655051L;
  
  public void valueBound(HttpSessionBindingEvent event) {
  }

  public void valueUnbound(HttpSessionBindingEvent event) {
    doTempFileCleanup();
  }
  
}
