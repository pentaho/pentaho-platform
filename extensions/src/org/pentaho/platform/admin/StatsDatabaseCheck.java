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
  
   public boolean startup(final IPentahoSession session) {
       
     String systemSolutionfolder = PentahoSystem.getApplicationContext().getSolutionPath("system");
     String jobFileFullPath = systemSolutionfolder+"/"+jobFileName;
     
     JobMeta jobMeta = null;
     try {
        jobMeta = new JobMeta(jobFileFullPath, null);
     }
     catch (KettleXMLException kxe) {
      Logger.error("Error opening "+jobFileFullPath, kxe.getMessage());
      return false;
     }
     
     if (jobMeta != null) {
        Job job = new Job(null, jobMeta);
        Result result = new Result();
        try {
           job.execute(0, result);
           job.waitUntilFinished();
        }
        catch (KettleException ke) {
           Logger.error("Error executing "+jobFileFullPath, ke.getMessage());
           return false;
        }
    
     }
     
    return true;
  }

  public void shutdown() {
    // Nothing required
  }
  
  public String getJobFileName() {
     return jobFileName;
  }

  public void setJobFileName(String jobFileName) {
     this.jobFileName = jobFileName;
  }
  
}
