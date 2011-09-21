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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jun 23, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.api.repository;

public class ContentException extends RepositoryException {

  /**
   * 
   */
  private static final long serialVersionUID = -3190555068029192935L;

  /**
   * Constructor
   * 
   * @param message
   *            The message
   */
  public ContentException(final String message) {
    super(message);
  }

  /**
   * Constructor
   * 
   * @param message
   *            The exception message
   * @param reas
   *            The throwable reason
   */
  public ContentException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * Constructor
   * 
   * @param reas
   *            The throwable reason
   */
  public ContentException(final Throwable reas) {
    super(reas);
  }

}
