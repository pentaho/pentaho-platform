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
 * Any subsystem that implements the IAuditable interface is candidate for auditing within the platform. This
 * interface outlines the necessary pieces of information that an object must be able to return in order to be
 * auditable.
 */
public interface IAuditable {

  /**
   * Returns the Java class name for this object.
   * 
   * @return the name of the object (the Java class name)
   */
  public String getObjectName();

  /**
   * Return the id for the execution of a given action sequence document.
   * 
   * @return the process id
   */
  public String getProcessId();

  /**
   * Return the name of the action sequence. Today, that name is synonymous with the name of the action sequence
   * document in the solution repository.
   * 
   * @return the name of the action sequence
   */
  public String getActionName();

  /**
   * Returns a unique id (across classes and instances) for this auditable object.
   * 
   * @return the auditable's id
   */
  public String getId();

}
