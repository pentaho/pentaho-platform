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
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

    String jobFileFullPath = getJobFileFullPath();

    TransMeta transMeta = new TransMeta( jobFileFullPath );
    if ( transMeta != null ) {
      Trans trans = new Trans( transMeta );
      trans.execute( null );
      trans.waitUntilFinished();
    }
  }

  protected String getJobFileFullPath() {
    String systemSolutionfolder = PentahoSystem.getApplicationContext().getSolutionPath( "system" );
    return systemSolutionfolder + "/" + getTransFileName();
  }

  public String getTransFileName() {
    return transFileName;
  }

  public void setTransFileName( String transFileName ) {
    this.transFileName = transFileName;
  }

}
