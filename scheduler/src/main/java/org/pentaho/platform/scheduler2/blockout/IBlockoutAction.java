/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.scheduler2.blockout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;

import java.util.Date;
import java.util.Map;

/**
 * @author wseyler This is the job that executes when the a block out trigger fires. This job essentially does nothing
 *         more than logging the firing of the trigger.
 */
public interface IBlockoutAction {
  public static String getCanonicalName() {
    return "org.pentaho.platform.scheduler2.blockout.BlockoutAction";
  }
   public void execute() throws Exception;
}
