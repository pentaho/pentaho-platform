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


package org.pentaho.platform.repository.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.spi.LoadEventListener;
import org.pentaho.platform.api.repository.IRuntimeElement;

public class HibernateLoadEventListener extends DefaultLoadEventListener {

  private static final long serialVersionUID = 2080567681499103474L;

  // Change to work with latest version of hibernate3.jar

  @Override
  public void onLoad( final LoadEvent event, final LoadEventListener.LoadType loadType ) throws HibernateException {
    super.onLoad( event, loadType );
    Object obj = event.getResult();
    if ( obj instanceof IRuntimeElement ) {
      ( (IRuntimeElement) obj ).setLoaded( true );
    }
  }

}
