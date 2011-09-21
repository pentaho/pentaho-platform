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
 * Created Nov 8, 2005 
 * @author mbatchel
 */
package org.pentaho.platform.api.repository;

public class SubscriptionRepositoryException extends RepositoryException {

  /**
   * 
   */
  private static final long serialVersionUID = -3190555068029192935L;

  /**
   * Constructor
   * 
   * Prevent zero-argument construction by making it public
   */
  @SuppressWarnings("unused")
  private SubscriptionRepositoryException() {
    super();
  }

  /**
   * Constructor
   * 
   * @param message
   *            The message
   */
  public SubscriptionRepositoryException(final String message) {
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
  public SubscriptionRepositoryException(final String message, final Throwable reas) {
    super(message, reas);
  }

  /**
   * Constructor
   * 
   * @param reas
   *            The throwable reason
   */
  public SubscriptionRepositoryException(final Throwable reas) {
    super(reas);
  }

}
