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


package org.pentaho.platform.api.action;

import java.util.List;

import org.pentaho.platform.api.repository.IContentItem;

/**
 * The interface for Actions that allows caller get information from Action
 *
 */
public interface IPostProcessingAction extends IAction {

  public List<IContentItem> getActionOutputContents();

}
