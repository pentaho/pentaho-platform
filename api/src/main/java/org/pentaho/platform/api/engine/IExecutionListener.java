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

public interface IExecutionListener {

  public void loaded( IRuntimeContext runtime );

  public void validated( IRuntimeContext runtime );

  public void action( IRuntimeContext runtime, ISolutionActionDefinition action );

  public void loop( IRuntimeContext runtime, long count );

}
