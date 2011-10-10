package org.pentaho.platform.api.scheduler2;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CronJobTrigger extends JobTrigger {
  String cronString;

  public CronJobTrigger() {
    this.cronString = cronString;
  }
  
  protected CronJobTrigger(String cronString) {
    this.cronString = cronString;
  }
  
  public String getCrongString() {
    return cronString;
  }

  public void setCrongString(String crongString) {
    this.cronString = crongString;
  }

  public String toString() {
    return cronString;
  }
  
}
