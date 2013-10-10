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

/*
 * Class for persisting lists and other collections. Using serialization to persist these items.
 * I'm using this class because I have a requirement to have a map element that may be a map or
 * some other collection of strings.
 */

package org.pentaho.platform.repository.hibernate.usertypes;

import java.sql.Types;

public class BinaryUserType extends BlobUserType {
  private static final int[] SQLTYPE = { Types.BINARY };

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  @Override
  public int[] sqlTypes() {
    return BinaryUserType.SQLTYPE;
  }

}
