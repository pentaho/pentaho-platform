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
 * Actions are the lightweight alternative to platform components (see
 * {@link org.pentaho.platform.api.engine.IComponent}). Actions participate in action sequences and can be provided
 * inputs and resources by the typical means, as defined in xaction solution files. The {@link IAction} family of
 * interfaces focuses on describing the *minimal* contract between the Pentaho BI Platform and an Action in the
 * same way that {@link org.pentaho.platform.api.engine.IComponent} describes a similar but more involved contract.
 * <p>
 * The Pentaho BI Platform expects Action objects to be Java bean API compliant with respect to setting inputs,
 * setting resources, and getting outputs. In other words, if your action needs takes a string input, the action
 * definition in the xaction solution file will specify this string input, and the Action framework will cause that
 * value to be set via a setter method on the Action object. You do not see parameter Maps and such in the Action
 * API for this reason. All inputs, output, and resources IO will involve Java bean reflection on your Action
 * object to find the appropriate IO methods.
 * 
 * @see IStreamingAction
 * @see ILoggingAction
 * @see ISessionAwareAction
 * @see IVarArgsAction
 * @see IPreProcessingAction
 * @see IDefinitionAwareAction
 * @author aphillips
 * @since 3.6
 * 
 */
public interface IAction {

  /**
   * The method in your Action that does the work.
   * 
   * @throws Exception
   *           if there was an error executing the Action
   */
  public void execute() throws Exception;

}
