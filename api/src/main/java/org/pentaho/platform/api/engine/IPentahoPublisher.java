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
