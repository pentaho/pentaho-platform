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


package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomSettingsParameterProvider extends BaseParameterProvider {

  private IPentahoSession session;

  public void setSession( IPentahoSession session ) {
    this.session = session;
  }

  @Override
  public Object getParameter( String name ) {
    return getValue( name );
  }

  @Override
  protected String getValue( String path ) {
    // apply templates to the part
    if ( session != null ) {
      path = path.replace( "{$user}", session.getName() ); //$NON-NLS-1$
    }
    return SystemSettingsParameterProvider.getSystemSetting( "components/" + path ); //$NON-NLS-1$
  }

  public Iterator getParameterNames() {
    // this will return hundreds of things so we're just going to return an empty list
    List<String> list = new ArrayList<String>();
    return list.iterator();
  }

}
