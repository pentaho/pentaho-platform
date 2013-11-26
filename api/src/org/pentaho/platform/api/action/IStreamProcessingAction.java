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

package org.pentaho.platform.api.action;

import java.io.InputStream;

/**
 * The interface for Actions that want to process the contents of a stream provided by the caller. Actions that
 * process the contents of a file contained in the Pentaho JCR repository and want the ability to be scheduled to
 * run should implement this method. The Pentaho scheduler will, upon execution of this action, open an input
 * stream to the file scheduled for execution and pass the input stream to this action.
 * 
 * @see IAction
 * @author arodriguez
 */
public interface IStreamProcessingAction extends IAction {

  /**
   * Sets the input stream containing the contents to be processed.
   * 
   * @param inputStream
   *          the input stream
   */
  public void setInputStream( InputStream inputStream );

}
