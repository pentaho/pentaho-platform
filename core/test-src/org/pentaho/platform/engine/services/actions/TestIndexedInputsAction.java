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

package org.pentaho.platform.engine.services.actions;

import org.pentaho.platform.api.action.IAction;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( "nls" )
public class TestIndexedInputsAction implements IAction {

  private List<String> messages = new ArrayList<String>();
  private String scalarMessage;
  private List<String> otherMessages = new ArrayList<String>();

  {
    otherMessages.add( "dummy value" );
    otherMessages.add( "dummy value" );
    otherMessages.add( "dummy value" );
    otherMessages.add( "dummy value" );
  }

  //
  // The "messages" property
  //
  public String getMessages( int index ) {
    return messages.get( index );
  }

  public List<String> getAllMessages() {
    return messages;
  }

  public void setMessages( int index, String message ) {
    messages.add( message );
  }

  /**
   * We must specify a getter method for the indexed "message" property so it will be an conformant JavaBean
   * property. BeanUtils requires indexed properties to also be JavaBean spec.
   */
  public String getMessages() {
    throw new UnsupportedOperationException( "This should never be called" );
  }

  //
  // The "otherMessage" property
  //
  public List<String> getOtherMessages() {
    return otherMessages;
  }

  public void setOtherMessage( String s ) {
    throw new UnsupportedOperationException( "This should not be called" );
  }

  public void execute() throws Exception {
  }

  /**
   * We have only a setter for this property to show that a getter is not required
   */
  public void setScalarMessage( String s ) {
    scalarMessage = s;
  }

  public String getTextOfScalarMessage() {
    return scalarMessage;
  }
}
