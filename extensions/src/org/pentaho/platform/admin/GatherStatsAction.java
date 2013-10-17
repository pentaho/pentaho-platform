/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.admin;

/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 * Marc Batchelor
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class GatherStatsAction implements IAction {

  // private static final String TRANS_FILE_NAME = "MonitorLocalCarteServer.ktr" ;

  private String transFileName;

  public Log getLogger() {
    return LogFactory.getLog( GatherStatsAction.class );
  }

  public void execute() throws KettleXMLException, KettleException {

    String systemSolutionfolder = PentahoSystem.getApplicationContext().getSolutionPath( "system" );
    String jobFileFullPath = systemSolutionfolder + "/" + getTransFileName();

    TransMeta transMeta = new TransMeta( jobFileFullPath );
    if ( transMeta != null ) {
      Trans trans = new Trans( transMeta );
      trans.execute( null );
      trans.waitUntilFinished();
    }
  }

  public String getTransFileName() {
    return transFileName;
  }

  public void setTransFileName( String transFileName ) {
    this.transFileName = transFileName;
  }

}
