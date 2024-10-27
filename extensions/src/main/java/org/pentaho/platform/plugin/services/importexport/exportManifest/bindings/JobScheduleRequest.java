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


//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.25 at 11:25:28 AM EDT 
//

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.pentaho.platform.api.scheduler.JobScheduleParam;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.JobState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for jobScheduleRequest complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="jobScheduleRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="actionClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="complexJobTrigger" type="{http://www.pentaho.com/schema/}complexJobTriggerProxy"
 *         minOccurs="0"/>
 *         &lt;element ref="{http://www.pentaho.com/schema/}cronJobTrigger" minOccurs="0"/>
 *         &lt;element name="duration" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="inputFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobParameters" type="{http://www.pentaho.com/schema/}jobScheduleParam"
 *         maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="outputFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://www.pentaho.com/schema/}simpleJobTrigger" minOccurs="0"/>
 *         &lt;element name="timeZone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jobId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "jobScheduleRequest", propOrder = { "actionClass", "complexJobTrigger", "cronJobTrigger", "duration",
    "inputFile", "jobName", "jobParameters", "outputFile", "simpleJobTrigger", "timeZone", "jobId", "jobState", "pdiParameters" } )
public class JobScheduleRequest {

  protected String actionClass;
  protected ComplexJobTriggerProxy complexJobTrigger;
  @XmlElement
  protected CronJobTrigger cronJobTrigger;
  protected long duration;
  protected String inputFile;
  protected String jobName;
  protected String jobId;
  @XmlElement( nillable = true )
  protected List<JobScheduleParam> jobParameters;
  protected String outputFile;
  @XmlElement
  protected SimpleJobTrigger simpleJobTrigger;
  protected String timeZone;
  protected JobState jobState;
  protected Map<String, String> pdiParameters;

  /**
   * Gets the value of the actionClass property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getActionClass() {
    return actionClass;
  }

  /**
   * Sets the value of the actionClass property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setActionClass( String value ) {
    this.actionClass = value;
  }

  /**
   * Gets the value of the complexJobTrigger property.
   * 
   * @return possible object is {@link ComplexJobTriggerProxy }
   * 
   */
  public ComplexJobTriggerProxy getComplexJobTrigger() {
    return complexJobTrigger;
  }

  /**
   * Sets the value of the complexJobTrigger property.
   * 
   * @param value
   *          allowed object is {@link ComplexJobTriggerProxy }
   * 
   */
  public void setComplexJobTrigger( ComplexJobTriggerProxy value ) {
    this.complexJobTrigger = value;
  }

  /**
   * Gets the value of the cronJobTrigger property.
   * 
   * @return possible object is {@link CronJobTrigger }
   * 
   */
  public CronJobTrigger getCronJobTrigger() {
    return cronJobTrigger;
  }

  /**
   * Sets the value of the cronJobTrigger property.
   * 
   * @param value
   *          allowed object is {@link CronJobTrigger }
   * 
   */
  public void setCronJobTrigger( CronJobTrigger value ) {
    this.cronJobTrigger = value;
  }

  /**
   * Gets the value of the duration property.
   * 
   */
  public long getDuration() {
    return duration;
  }

  /**
   * Sets the value of the duration property.
   * 
   */
  public void setDuration( long value ) {
    this.duration = value;
  }

  /**
   * Gets the value of the inputFile property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getInputFile() {
    return inputFile;
  }

  /**
   * Sets the value of the inputFile property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setInputFile( String value ) {
    this.inputFile = value;
  }

  /**
   * Gets the value of the jobName property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * Sets the value of the jobName property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setJobName( String value ) {
    this.jobName = value;
  }

  /**
   * Gets the value of the jobParameters property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
   * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
   * the jobParameters property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getJobParameters().add( newItem );
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link JobScheduleParam }
   * 
   * 
   */
  public List<JobScheduleParam> getJobParameters() {
    if ( jobParameters == null ) {
      jobParameters = new ArrayList<JobScheduleParam>();
    }
    return this.jobParameters;
  }

  /**
   * Gets the value of the outputFile property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getOutputFile() {
    return outputFile;
  }

  /**
   * Sets the value of the outputFile property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setOutputFile( String value ) {
    this.outputFile = value;
  }

  /**
   * Gets the value of the simpleJobTrigger property.
   * 
   * @return possible object is {@link SimpleJobTrigger }
   * 
   */
  public SimpleJobTrigger getSimpleJobTrigger() {
    return simpleJobTrigger;
  }

  /**
   * Sets the value of the simpleJobTrigger property.
   * 
   * @param value
   *          allowed object is {@link SimpleJobTrigger }
   * 
   */
  public void setSimpleJobTrigger( SimpleJobTrigger value ) {
    this.simpleJobTrigger = value;
  }

  /**
   * Gets the value of the timeZone property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getTimeZone() {
    return timeZone;
  }

  /**
   * Sets the value of the timeZone property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setTimeZone( String value ) {
    this.timeZone = value;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId( String jobId ) {
    this.jobId = jobId;
  }

  public JobState getJobState() {
    return jobState;
  }

  public void setJobState( JobState jobState ) {
    this.jobState = jobState;
  }

  public Map<String, String> getPdiParameters() {
    return pdiParameters;
  }

  public void setPdiParameters( Map<String, String> pdiParameters ) {
    this.pdiParameters = pdiParameters;
  }

  public void setJobParameters( List<JobScheduleParam> jobParameters ) {
    if ( jobParameters != getJobParameters()) {
      getJobParameters().clear();
      if ( jobParameters != null ) {
        getJobParameters().addAll( jobParameters );
      }
    }
  }
}
