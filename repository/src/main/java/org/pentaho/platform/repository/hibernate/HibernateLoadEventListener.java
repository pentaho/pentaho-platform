/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.event.LoadEvent;
import org.hibernate.event.LoadEventListener;
import org.hibernate.event.def.DefaultLoadEventListener;
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
