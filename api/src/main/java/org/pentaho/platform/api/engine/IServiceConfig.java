/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
