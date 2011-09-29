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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class MantleServiceCache {

  public static MantleServiceAsync service = null;

  public static MantleServiceAsync getService() {
    if (service == null) {
      service = (MantleServiceAsync) GWT.create(MantleService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) service;
      String moduleRelativeURL = GWT.getModuleBaseURL() + "MantleService"; //$NON-NLS-1$
      endpoint.setServiceEntryPoint(moduleRelativeURL);
    }
    return service;
  }
}
