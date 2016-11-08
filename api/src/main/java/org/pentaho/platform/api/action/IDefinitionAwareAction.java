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

import java.util.List;

/**
 * Makes an Action privy to certain details about the action definition that is responsible for executing it. This
 * is the only Action interface that should know anything about an action definition or the fact that it is even
 * being executed by way of an action sequence at all, for that matter. This interface is often used in concert
 * with {@link IPreProcessingAction}.
 * 
 * @see IAction
 * @author aphillips
 * @since 3.6
 */
public interface IDefinitionAwareAction extends IAction {

  /**
   * Informs the Action of the inputs that will be given to it, as specified in the action definition. This method
   * is called on an Action just prior to execution.
   * 
   * @param inputNames
   *          names of the action definition inputs
   */
  public void setInputNames( List<String> inputNames );

  /**
   * Informs the Action of the outputs that will be expected of it, as specified in the action definition. This
   * method is called on an Action just prior to execution.
   * 
   * @param inputNames
   *          names of the action definition inputs
   */
  public void setOutputNames( List<String> outputNames );
}
