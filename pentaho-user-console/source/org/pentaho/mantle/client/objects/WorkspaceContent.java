package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class WorkspaceContent implements Serializable
{
  private ArrayList<JobDetail> scheduledJobs;
  private ArrayList<JobDetail> completedJobs;
  private ArrayList<JobSchedule> mySchedules;
  private ArrayList<JobSchedule> allSchedules;
  private ArrayList<SubscriptionBean> subscriptions;

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

  public ArrayList<SubscriptionBean> getSubscriptions()
  {
    return subscriptions;
  }

  public void setSubscriptions(ArrayList<SubscriptionBean> subscriptions)
  {
    this.subscriptions = subscriptions;
  }
}
