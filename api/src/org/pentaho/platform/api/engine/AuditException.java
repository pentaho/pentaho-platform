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

import org.pentaho.platform.api.util.PentahoChainedException;

/**
 * @author mbatchel
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class AuditException extends PentahoChainedException {

  /**
   * 
   */
  private static final long serialVersionUID = -1428382933476958337L;

  /**
   * 
   */
  public AuditException() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public AuditException( final String message ) {
    super( message );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param reas
   */
  public AuditException( final String message, final Throwable reas ) {
    super( message, reas );
    // TODO Auto-generated constructor stub
  }

  /**
   * @param reas
   */
  public AuditException( final Throwable reas ) {
    super( reas );
    // TODO Auto-generated constructor stub
  }

}
