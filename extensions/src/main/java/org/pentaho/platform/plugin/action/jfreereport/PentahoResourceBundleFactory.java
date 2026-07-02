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


package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Creation-Date: 08.07.2006, 13:19:45
 * 
 * @author Thomas Morgner
 * @deprecated This is an empty stub in case we have to maintain backward compatiblity.
 */
@Deprecated
public class PentahoResourceBundleFactory extends
    org.pentaho.platform.plugin.action.jfreereport.helper.PentahoResourceBundleFactory {
  private static final long serialVersionUID = 264302209954377201L;

  public PentahoResourceBundleFactory( final String path, final String baseName, final IPentahoSession session ) {
    super( path, baseName, session );
  }
}
