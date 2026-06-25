/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.repository.hibernate.usertypes;

import java.sql.Types;

public class LongVarBinaryUserType extends BinaryUserType {
  private static final int SQLTYPE = Types.LONGVARBINARY;

  /*
   * (non-Javadoc)
   * 
   * @see org.hibernate.usertype.UserType#getSqlType()
   */
  @Override
  public int getSqlType() {
    return LongVarBinaryUserType.SQLTYPE;
  }

}
