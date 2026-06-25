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



package org.pentaho.platform.api.locale;

import java.util.Locale;

public interface IPentahoLocale {

  /**
   * Returns a single {@link Locale} object
   * 
   * @return {@link Locale}
   */
  Locale getLocale();
}
