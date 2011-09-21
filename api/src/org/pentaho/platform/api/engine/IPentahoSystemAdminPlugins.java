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
package org.pentaho.platform.api.engine;

import java.util.List;

import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;

public interface IPentahoSystemAdminPlugins {

  public String publish(final IPentahoSession session, final String className);

  public List<IPentahoPublisher> getPublisherList();

  public Document getPublishersDocument();

  /**
   * Registers administrative capabilities that can be invoked later 
   * via {@link PentahoSystem#publish(IPentahoSession, String)}
   * 
   * @param administrationPlugins a list of admin functions to register
   */
  public void setAdministrationPlugins(List<IPentahoPublisher> administrationPlugins);

}
