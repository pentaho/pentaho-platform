/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.repository.hibernate.usertypes;

import java.sql.Types;

public class LongVarBinaryUserType extends BinaryUserType {
  private static final int[] SQLTYPE = { Types.LONGVARBINARY };

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  @Override
  public int[] sqlTypes() {
    return LongVarBinaryUserType.SQLTYPE;
  }

}
