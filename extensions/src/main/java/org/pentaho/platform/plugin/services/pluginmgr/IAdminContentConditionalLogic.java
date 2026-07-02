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


package org.pentaho.platform.plugin.services.pluginmgr;

public interface IAdminContentConditionalLogic {

  public static int DISPLAY_ADMIN_CONTENT = 0;
  public static int AVOID_ADMIN_CONTENT = 1;
  public static int DISPLAY_EXCEPTION_MESSAGE = 2;

  int validate();
}
