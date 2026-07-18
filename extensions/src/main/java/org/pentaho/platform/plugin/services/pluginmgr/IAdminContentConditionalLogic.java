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



package org.pentaho.platform.plugin.services.pluginmgr;

public interface IAdminContentConditionalLogic {

  public static int DISPLAY_ADMIN_CONTENT = 0;
  public static int AVOID_ADMIN_CONTENT = 1;
  public static int DISPLAY_EXCEPTION_MESSAGE = 2;

  int validate();
}
