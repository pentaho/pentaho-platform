package org.pentaho.platform.api.scheduler2;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;

public class JobTriggerAdapter extends XmlAdapter<JobTrigger, JobTrigger>{

  public JobTrigger marshal(JobTrigger v) throws Exception {
    return v instanceof ComplexJobTrigger ? new CronJobTrigger(v.toString()) : v;
  }

  public JobTrigger unmarshal(JobTrigger v) throws Exception {
    return v instanceof CronJobTrigger ? QuartzScheduler.createComplexTrigger(v.toString()) : v;
  }

}
