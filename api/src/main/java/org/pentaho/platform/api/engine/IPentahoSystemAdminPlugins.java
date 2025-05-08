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

import org.dom4j.Document;

import java.util.List;

public interface IPentahoSystemAdminPlugins {

  public String publish( final IPentahoSession session, final String className );

  public List<IPentahoPublisher> getPublisherList();

  public Document getPublishersDocument();

  /**
   * Registers administrative capabilities that can be invoked later via
   * {@link PentahoSystem#publish(IPentahoSession, String)}
   * 
   * @param administrationPlugins
   *          a list of admin functions to register
   */
  public void setAdministrationPlugins( List<IPentahoPublisher> administrationPlugins );

}
