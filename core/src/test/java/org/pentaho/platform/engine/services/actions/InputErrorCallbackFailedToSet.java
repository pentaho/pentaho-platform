/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.actions;

import org.pentaho.platform.api.action.IAction;

import java.util.ArrayList;
import java.util.List;

public class InputErrorCallbackFailedToSet implements IAction {

  private List<String> inputs = new ArrayList<String>();

  public void setInputs( List<String> messages ) {
    this.inputs = messages;
  }

  public List<String> getInputs() {
    return inputs;
  }

  public void execute() throws Exception {
  }

}
