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

import org.pentaho.platform.api.action.IStreamingAction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "nls" )
public class TestAllIOAction implements IStreamingAction {

  private OutputStream myContentOutput;
  private String message;
  private InputStream embeddedXmlResource;
  private List<String> addressees;
  private Long count;
  private Map<String, String> veggieData;
  private List<Map<String, String>> fruitData;
  private boolean executeWasCalled = false;

  public OutputStream getMyContentOutput() {
    return myContentOutput;
  }

  public void setOutputStream( OutputStream outputStream ) {
    setMyContentOutputStream( outputStream );
  }

  public void setMyContentOutputStream( OutputStream outputStream ) {
    this.myContentOutput = outputStream;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public InputStream getEmbeddedXmlResource() {
    return embeddedXmlResource;
  }

  public void setEmbeddedXmlResource( InputStream embeddedXmlResource ) {
    this.embeddedXmlResource = embeddedXmlResource;
  }

  public List<String> getAddressees() {
    return addressees;
  }

  public void setAddressees( List<String> addressees ) {
    this.addressees = addressees;
  }

  public Long getCount() {
    return count;
  }

  public void setCount( Long count ) {
    this.count = count;
  }

  public Map<String, String> getVeggieData() {
    return veggieData;
  }

  public void setVeggieData( Map<String, String> veggieData ) {
    this.veggieData = veggieData;
  }

  public List<Map<String, String>> getFruitData() {
    return fruitData;
  }

  public void setFruitData( List<Map<String, String>> fruitData ) {
    this.fruitData = fruitData;
  }

  public String getEchoMessage() {
    return "Test String Output";
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
  }

  public String getMimeType( String streamPropertyName ) {
    return "text/html";
  }

}
