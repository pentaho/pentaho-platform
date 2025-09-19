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


package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class GenericEvent extends GwtEvent<GenericEventHandler> {

  public static Type<GenericEventHandler> TYPE = new Type<GenericEventHandler>();

  private String eventSubType;

  private String stringParam;
  private Integer integerParam;
  private Float floatParam;
  private Boolean booleanParam;
  private Long testLongParam;
  private Short testShortParam;
  private Double testDoubleParam;

  public GenericEvent() {
  }

  public Type<GenericEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch( GenericEventHandler handler ) {
    handler.onGenericEventFired( this );
  }

  public String getEventSubType() {
    return eventSubType;
  }

  public void setEventSubType( String eventSubType ) {
    this.eventSubType = eventSubType;
  }

  public String getStringParam() {
    return stringParam;
  }

  public void setStringParam( String stringParam ) {
    this.stringParam = stringParam;
  }

  public Integer getIntegerParam() {
    return integerParam;
  }

  public void setIntegerParam( Integer integerParam ) {
    this.integerParam = integerParam;
  }

  public Float getFloatParam() {
    return floatParam;
  }

  public void setFloatParam( Float floatParam ) {
    this.floatParam = floatParam;
  }

  public Boolean getBooleanParam() {
    return booleanParam;
  }

  public void setBooleanParam( Boolean booleanParam ) {
    this.booleanParam = booleanParam;
  }

  public Long getTestLongParam() {
    return testLongParam;
  }

  public void setTestLongParam( Long testLongParam ) {
    this.testLongParam = testLongParam;
  }

  public Short getTestShortParam() {
    return testShortParam;
  }

  public void setTestShortParam( Short testShortParam ) {
    this.testShortParam = testShortParam;
  }

  public Double getTestDoubleParam() {
    return testDoubleParam;
  }

  public void setTestDoubleParam( Double testDoubleParam ) {
    this.testDoubleParam = testDoubleParam;
  }
}
