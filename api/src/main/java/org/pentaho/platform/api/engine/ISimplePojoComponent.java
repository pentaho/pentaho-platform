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



package org.pentaho.platform.api.engine;

/**
 * @deprecated Pojo components are deprecated, use {@link org.pentaho.platform.api.action.IAction}
 */
@Deprecated
public interface ISimplePojoComponent {

  public boolean execute() throws Exception;

  public boolean validate() throws Exception;

}
