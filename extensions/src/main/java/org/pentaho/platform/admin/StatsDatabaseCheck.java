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

import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

public class StatsDatabaseCheck implements IPentahoSystemListener {

  private String jobFileName;

  public boolean startup( final IPentahoSession session ) {

    JobMeta jobMeta = null;
    String jobFileFullPath = getJobFileFullPath();
    try {
      jobMeta = new JobMeta( jobFileFullPath, null );
    } catch ( KettleXMLException kxe ) {
      Logger.error( "Error opening " + jobFileFullPath, kxe.getMessage() );
      return false;
    }

    return executeJob( jobMeta, jobFileFullPath );
  }

  protected boolean executeJob( JobMeta jobMeta, String jobFileFullPath ) {
    if ( jobMeta != null ) {
      Job job = new Job( null, jobMeta );
      Result result = new Result();
      try {
        job.execute( 0, result );
        job.waitUntilFinished();
      } catch ( KettleException ke ) {
        Logger.error( "Error executing " + jobFileFullPath, ke.getMessage() );
        return false;
      }
    }
    return true;
  }

  protected String getJobFileFullPath() {
    String systemSolutionfolder = PentahoSystem.getApplicationContext().getSolutionPath( "system" );
    return systemSolutionfolder + "/" + getJobFileName();
  }

  public void shutdown() {
    // Nothing required
  }

  public String getJobFileName() {
    return jobFileName;
  }

  public void setJobFileName( String jobFileName ) {
    this.jobFileName = jobFileName;
  }

}
