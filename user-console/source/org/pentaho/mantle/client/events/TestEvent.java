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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class TestEvent extends GwtEvent<TestEventHandler> {

  public static Type<TestEventHandler> TYPE = new Type<TestEventHandler>();

  private String testString;
  private Integer testInteger;
  private Float testFloat;
  private Boolean testBoolean;
  private Long testLong;
  private Short testShort;
  private Double testDouble;

  public TestEvent() {
  }

  public Integer getTestInteger() {
    return testInteger;
  }

  public void setTestInteger(Integer testInteger) {
    this.testInteger = testInteger;
  }

  public Float getTestFloat() {
    return testFloat;
  }

  public void setTestFloat(Float testFloat) {
    this.testFloat = testFloat;
  }

  public Boolean getTestBoolean() {
    return testBoolean;
  }

  public void setTestBoolean(Boolean testBoolean) {
    this.testBoolean = testBoolean;
  }

  public Long getTestLong() {
    return testLong;
  }

  public void setTestLong(Long testLong) {
    this.testLong = testLong;
  }

  public Short getTestShort() {
    return testShort;
  }

  public void setTestShort(Short testShort) {
    this.testShort = testShort;
  }

  public Double getTestDouble() {
    return testDouble;
  }

  public void setTestDouble(Double testDouble) {
    this.testDouble = testDouble;
  }

  public TestEvent(String testString) {
    this.testString = testString;
  }

  public Type<TestEventHandler> getAssociatedType() {
    return TYPE;
  }

  protected void dispatch(TestEventHandler handler) {
    handler.onTestStringChanged(this);
  }

  public String getTestString() {
    return testString;
  }

  public void setTestString(String testString) {
    this.testString = testString;
  }

}
