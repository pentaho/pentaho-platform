/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
