package org.pentaho.platform.api.scheduler2;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CronJobTrigger extends JobTrigger {
  String cronString;

  public CronJobTrigger() {
  }
  
  protected CronJobTrigger(String cronString) {
    this.cronString = cronString;
  }
  
  public String getCronString() {
    return cronString;
  }

  public void setCronString(String crongString) {
    this.cronString = crongString;
  }

  public String toString() {
    return cronString;
  }
  
}
