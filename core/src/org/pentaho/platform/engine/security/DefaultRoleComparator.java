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

package org.pentaho.platform.engine.security;

import java.util.Comparator;

public class DefaultRoleComparator implements Comparator<String> {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private boolean caseSensitive = true;

  // ~ Constructors
  // ====================================================================================================

  // ~ Methods
  // =========================================================================================================

  public DefaultRoleComparator() {
    this( true );
  }

  public DefaultRoleComparator( final boolean caseSensitive ) {
    super();
    this.caseSensitive = caseSensitive;
  }

  public int compare( final String o1, final String o2 ) {
    return caseSensitive ? o1.compareTo( o2 ) : o1.compareToIgnoreCase( o2 );
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive( final boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

}
