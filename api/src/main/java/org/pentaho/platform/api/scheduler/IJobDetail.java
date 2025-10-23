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


package org.pentaho.platform.api.scheduler;

public interface IJobDetail {
  public String getSubmissionDate();

  public String getActionName();

  public String getFullName();

  public String getDescription();

  public String getName();

  public String getGroupName();
}
