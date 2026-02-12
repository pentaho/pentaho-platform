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

/**
 * Defines the interface for getting objects published in the platform. Publishers are defined in the pentaho.xml,
 * and show up on the console in the Publish jsp.
 * 
 * @author mbatchel
 * @see SolutionPublisher
 * @see SettingsPublisher
 * 
 */

public interface IPentahoPublisher {

  /**
   * Called when the publish is requested.
   * 
   * @param session
   *          The session of the requester
   * @param loggingLevel
   *          Log granularity level
   * @return message indicating success or failure. This message is shown back to the user that requested the
   *         publish.
   */
  public String publish( IPentahoSession session, int loggingLevel );

  /**
   * @return The name of the publisher. This name is displayed in the Publish JSP
   */
  public String getName();

  /**
   * @return The description of the publisher. This string is displayed in the Publish JSP
   */
  public String getDescription();

}
