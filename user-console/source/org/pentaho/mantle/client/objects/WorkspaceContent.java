package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class WorkspaceContent implements Serializable
{
  private static final long serialVersionUID = -4201572866324739341L;
  private ArrayList<JobDetail> scheduledJobs;
  private ArrayList<JobDetail> completedJobs;
  private ArrayList<JobSchedule> mySchedules;
  private ArrayList<JobSchedule> allSchedules;

  public WorkspaceContent()
  {
  }

  public ArrayList<JobDetail> getScheduledJobs()
  {
    return scheduledJobs;
  }

  public void setScheduledJobs(ArrayList<JobDetail> scheduledJobs)
  {
    this.scheduledJobs = scheduledJobs;
  }

  public ArrayList<JobDetail> getCompletedJobs()
  {
    return completedJobs;
  }

  public void setCompletedJobs(ArrayList<JobDetail> completedJobs)
  {
    this.completedJobs = completedJobs;
  }

  public ArrayList<JobSchedule> getMySchedules()
  {
    return mySchedules;
  }

  public void setMySchedules(ArrayList<JobSchedule> mySchedules)
  {
    this.mySchedules = mySchedules;
  }

  public ArrayList<JobSchedule> getAllSchedules()
  {
    return allSchedules;
  }

  public void setAllSchedules(ArrayList<JobSchedule> allSchedules)
  {
    this.allSchedules = allSchedules;
  }

}
