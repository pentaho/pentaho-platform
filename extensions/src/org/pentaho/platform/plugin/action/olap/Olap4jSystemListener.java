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
package org.pentaho.platform.plugin.action.olap;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.List;
import java.util.Properties;

public class Olap4jSystemListener implements IPentahoSystemListener {
  private List<Olap4jConnectionBean> olap4jConnectionList;
  private List<String> removeList;

  @Override public boolean startup( IPentahoSession session ) {
    IOlapService olapService = getOlapService( session );
    if ( olapService != null ) {
      for ( Olap4jConnectionBean bean : olap4jConnectionList ) {
        olapService.addOlap4jCatalog(
          bean.getName(),
          bean.getClassName(),
          bean.getConnectString(),
          bean.getUser(),
          bean.getPassword(),
          new Properties(),
          true,
          session );
      }
      for ( String catalogName : removeList ) {
        olapService.removeCatalog( catalogName, session );
      }
    }
    return true;
  }

  IOlapService getOlapService( IPentahoSession session ) {
    return PentahoSystem.get( IOlapService.class, session );
  }

  @Override public void shutdown() {
  }

  public void setOlap4jConnectionList( List<Olap4jConnectionBean> olap4jConnectionList ) {
    this.olap4jConnectionList = olap4jConnectionList;
  }

  public void setOlap4jConnectionRemoveList( List<String> removeList ) {
    this.removeList = removeList;
  }
}
