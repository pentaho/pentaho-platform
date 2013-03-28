/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 20, 2013 
 * @author wseyler
 */

package org.pentaho.platform.web.http.api.resources.proxies;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.quartz.SimpleTrigger;

/**
 * @author wseyler
 */
@XmlRootElement
public class SimpleTriggerProxy extends SimpleTrigger implements Serializable {

  public SimpleTriggerProxy() {
    super();
  }
  
  public SimpleTriggerProxy(SimpleTrigger simpleTrigger) {
    this();
    this.setCalendarName(simpleTrigger.getCalendarName());
    this.setDescription(simpleTrigger.getDescription());
    this.setEndTime(simpleTrigger.getEndTime());
    this.setGroup(simpleTrigger.getGroup());
    this.setJobDataMap(simpleTrigger.getJobDataMap());
    this.setJobName(simpleTrigger.getFullJobName());
    this.setMisfireInstruction(simpleTrigger.getMisfireInstruction());
    this.setName(simpleTrigger.getName());
    this.setPriority(simpleTrigger.getPriority());
    this.setRepeatCount(simpleTrigger.getRepeatCount());
    this.setRepeatInterval(simpleTrigger.getRepeatInterval());
  }
}
