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


package org.pentaho.platform.api.data;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IComponent;

/**
 * The data component interface extends component, and adds a single method - <code>getResultSet()</code>.
 * 
 * @author mbatchel
 * @see MDXBaseComponent
 * @see SQLBaseComponent
 * @see IPentahoResultSet
 * 
 */
public interface IDataComponent extends IComponent {

  /**
   * @return Returns the resultSet that the component currently has.
   */
  public IPentahoResultSet getResultSet();

  /**
   * Disposes of resources held by the data component
   */
  public void dispose();

}
