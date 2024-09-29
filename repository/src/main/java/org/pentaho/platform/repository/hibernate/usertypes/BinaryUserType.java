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
