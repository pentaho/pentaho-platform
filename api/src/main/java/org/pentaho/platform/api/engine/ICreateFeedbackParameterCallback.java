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

import java.util.List;
import java.util.Map;

public interface ICreateFeedbackParameterCallback {
  @SuppressWarnings( "rawtypes" )
  public void createFeedbackParameter( IRuntimeContext runtimeContext, String fieldName, final String displayName,
      String hint, Object defaultValues, final List values, final Map dispNames, final String displayStyle,
      final boolean optional, final boolean visible );
}
