/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.util;

public class ServerTypeUtil {
  private static final String PLATFORM_PRODUCT_ID = "POBS";

  public static boolean isPlatformServer() {
    String productId;
    productId = VersionHelper.getVersionInfo().getProductID();

    if ( productId == null ) {
      return false;
    }

    return productId.equals( PLATFORM_PRODUCT_ID );
  }
}
