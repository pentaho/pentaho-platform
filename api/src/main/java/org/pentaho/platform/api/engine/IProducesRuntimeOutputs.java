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


package org.pentaho.platform.api.engine;

import java.util.Map;

/**
 * @deprecated Pojo components are deprecated, use {@link org.pentaho.platform.api.action.IAction}
 */
@Deprecated
public interface IProducesRuntimeOutputs {

  public Map<String, Object> getOutputs();

}
