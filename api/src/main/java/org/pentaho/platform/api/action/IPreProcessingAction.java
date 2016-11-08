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

/**
 * Allows an Action to do some preliminary work prior to execution. This API also allows an Action to fail early
 * and thereby not actually execute. Typically this is used in combination with {@link IDefinitionAwareAction} to
 * verify that the required inputs are provided prior to execution.
 * 
 * @see IAction
 * @see IDefinitionAwareAction
 * @author aphillips
 * @since 3.6
 */
public interface IPreProcessingAction extends IAction {

  /**
   * This method is called on an Action just prior to execution. An Action can fail early for any reason here by
   * throwing an exception. If an exception is thrown here, actual execution of the Action will not occur. If you
   * need to do pre-execution validation of inputs, see {@link IDefinitionAwareAction}
   * 
   * @throws ActionPreProcessingException
   *           if the Action is not able to proceed with execution
   */
  public void doPreExecution() throws ActionPreProcessingException;

}
