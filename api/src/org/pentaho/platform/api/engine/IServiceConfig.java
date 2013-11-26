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

package org.pentaho.platform.api.engine;

import java.util.Collection;

/**
 * The configuration spec for a platform managed service
 * 
 * @author aphillips
 */
public interface IServiceConfig {

  /**
   * Returns the unique id of this web service. This should not be localized
   * 
   * @return a unique id for this web service
   */
  public String getId();

  /**
   * Returns the enabled state of this service
   * 
   * @return Current enable/disable state
   */
  public boolean isEnabled();

  public Collection<Class<?>> getExtraClasses();

  /**
   * Returns the web service bean class
   * 
   * @return bean class or id by which the class can be looked up
   */
  public Class<?> getServiceClass();

  /**
   * Returns the localized title for this web service. This is shown on the services list page. Defaults to service
   * id if not set.
   * 
   * @return natural language name for the service
   */
  public String getTitle();

  /**
   * Returns the localized title for this web service. This is shown on the services list page.
   * 
   * @return Description
   */
  public String getDescription();

  public String getServiceType();

}
