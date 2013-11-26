/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoRequestContextHolderStrategy;

/**
 * A {@code static} field-based implementation of {@link IPentahoRequestContextHolderStrategy}.
 * 
 * <p>
 * This means that all instances in the JVM share the same {@code IPentahoRequestContext}. This is generally useful
 * with rich clients, such as Swing.
 * </p>
 * 
 * @author rmansoor
 */
public class GlobalPentahoRequestContextHolderStrategy implements IPentahoRequestContextHolderStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private static IPentahoRequestContext requestContext;

  // ~ Constructors
  // ====================================================================================================

  public GlobalPentahoRequestContextHolderStrategy() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public IPentahoRequestContext getRequestContext() {
    return requestContext;
  }

  public void removeRequestContext() {
    requestContext = null;
  }

  public void setRequestContext( IPentahoRequestContext inRequestContext ) {
    requestContext = inRequestContext;
  }

}
