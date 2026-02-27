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


package org.pentaho.platform.engine.services.actions;

import org.pentaho.platform.api.action.IStreamingAction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "nls" )
public class TestAllIOAction implements IStreamingAction {

  private OutputStream outputstream;
  private String message;
  private InputStream embeddedXmlResource;
  private List<String> addressees;
  private Long count;
  private Map<String, String> veggieData;
  private List<Map<String, String>> fruitData;
  private boolean executeWasCalled = false;

  public OutputStream getOutputstream() {
    return outputstream;
  }

  public OutputStream getOutputStream() {
    return outputstream;
  }

  @Override
  public void setOutputStream( OutputStream outputStream ) {
    this.outputstream = outputStream;
  }

  public void setOutputstream( OutputStream outputStream ) {
    this.outputstream = outputStream;
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
