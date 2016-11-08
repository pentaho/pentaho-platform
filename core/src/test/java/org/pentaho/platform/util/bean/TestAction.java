/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
