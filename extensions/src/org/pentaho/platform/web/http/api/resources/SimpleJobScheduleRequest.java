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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 5/2/2011
 * @author Angelo Rodriguez
 *
 */
package org.pentaho.platform.web.http.api.resources;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;

@XmlRootElement
public class SimpleJobScheduleRequest implements Serializable {
  private static final long serialVersionUID = -6145183300070801027L;
  
  String inputFile;
  String outputFile;
  SimpleJobTrigger jobTrigger;
  
  public String getInputFile() {
    return inputFile;
  }
  
  public void setInputFile(String file) {
    this.inputFile = file;
  }
  
  public String getOutputFile() {
    return outputFile;
  }
  
  public void setOutputFile(String file) {
    this.outputFile = file;
  }
  
  public SimpleJobTrigger getJobTrigger() {
    return jobTrigger;
  }
  
  public void setJobTrigger(SimpleJobTrigger jobTrigger) {
    this.jobTrigger = jobTrigger;
  }
}
