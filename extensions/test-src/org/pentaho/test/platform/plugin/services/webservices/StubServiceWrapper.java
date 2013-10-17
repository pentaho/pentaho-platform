/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.webservices;

import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;

import java.util.ArrayList;
import java.util.Collection;

public class StubServiceWrapper extends ServiceConfig {

  public Class<?> getServiceClass() {
    return StubService.class;
  }

  public String getTitle() {
    return "test title"; //$NON-NLS-1$
  }

  public String getDescription() {
    return "test description"; //$NON-NLS-1$
  }

  @Override
  public Collection<Class<?>> getExtraClasses() {
    ArrayList<Class<?>> extraClasses = new ArrayList<Class<?>>();
    extraClasses.add( ComplexType.class );
    return extraClasses;
  }
}
