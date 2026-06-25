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



package org.pentaho.platform.api.usersettings.pojo;

import java.io.Serializable;

public interface IUserSetting extends Serializable {

  public void setSettingName( String settingName );

  public String getSettingName();

  public void setSettingValue( String settingValue );

  public String getSettingValue();

  public String getUsername();

  public void setUsername( String username );
}
