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


package org.pentaho.platform.util.bean;

import org.pentaho.platform.api.action.IAction;

@SuppressWarnings( "nls" )
public class TestAction implements IAction {

  private String message;
  private boolean executeWasCalled = false;
  private String messageBoard;
  private CustomType custom;
  private Long count;

  public TestAction() {
    System.out.println( "new action created " + this.hashCode() );
  }

  public CustomType getCustom() {
    return custom;
  }

  public void setCustom( CustomType custom ) {
    this.custom = custom;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public Long getCount() {
    return count;
  }

  public void setCount( Long count ) {
    this.count = count;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
  }

  public void setMessageBoard( String messageBoard ) {
    System.err.println( "setMessageBoard " + this.hashCode() );
    this.messageBoard = messageBoard;
  }

  public String getMessageBoard() {
    System.err.println( "getMessageBoard " + this.hashCode() );
    return messageBoard;
  }

  static class CustomType {
  }
}
