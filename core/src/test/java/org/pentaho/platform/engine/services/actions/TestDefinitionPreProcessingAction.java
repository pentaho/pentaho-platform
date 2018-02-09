/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.actions;

import org.apache.commons.collections.map.ListOrderedMap;
import org.pentaho.platform.api.action.ActionPreProcessingException;
import org.pentaho.platform.api.action.IDefinitionAwareAction;
import org.pentaho.platform.api.action.IPreProcessingAction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestDefinitionPreProcessingAction implements IDefinitionAwareAction, IPreProcessingAction {

  private boolean executeWasCalled = false;
  private boolean doPreExecutionWasCalled = false;
  private List<String> inputNames;
  private List<String> outputNames;
  private boolean isReadyToExecute;

  private String message;
  private ArrayList<String> addressees;
  private long count;
  private ListOrderedMap veggieData;
  private ArrayList<String> fruitData;
  private InputStream embeddedXmlResource;
  private OutputStream myContentOutput;
  private String echoMessage;

  public void doPreExecution() throws ActionPreProcessingException {
    doPreExecutionWasCalled = true;
    isReadyToExecute = true;
  }

  public void execute() throws Exception {
    if ( !isReadyToExecute ) {
      throw new IllegalStateException( "doPreExecution was not called before execute!" );
    }
    executeWasCalled = true;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public boolean isDoPreExecutionWasCalled() {
    return doPreExecutionWasCalled;
  }

  public List<String> getInputNames() {
    return inputNames;
  }

  public void setInputNames( List<String> inputNames ) {
    this.inputNames = inputNames;
  }

  public List<String> getOutputNames() {
    return outputNames;
  }

  public void setOutputNames( List<String> outputNames ) {
    this.outputNames = outputNames;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public boolean isReadyToExecute() {
    return isReadyToExecute;
  }

  public void setReadyToExecute( boolean isReadyToExecute ) {
    this.isReadyToExecute = isReadyToExecute;
  }

  public ArrayList<String> getAddressees() {
    return addressees;
  }

  public void setAddressees( ArrayList<String> addressees ) {
    this.addressees = addressees;
  }

  public long getCount() {
    return count;
  }

  public void setCount( long count ) {
    this.count = count;
  }

  public ListOrderedMap getVeggieData() {
    return veggieData;
  }

  public void setVeggieData( ListOrderedMap veggieData ) {
    this.veggieData = veggieData;
  }

  public ArrayList<String> getFruitData() {
    return fruitData;
  }

  public void setFruitData( ArrayList<String> fruitData ) {
    this.fruitData = fruitData;
  }

  public InputStream getEmbeddedXmlResource() {
    return embeddedXmlResource;
  }

  public void setEmbeddedXmlResource( InputStream embeddedXmlResource ) {
    this.embeddedXmlResource = embeddedXmlResource;
  }

  public OutputStream getMyContentOutput() {
    return myContentOutput;
  }

  public void setMyContentOutput( OutputStream myContentOutput ) {
    this.myContentOutput = myContentOutput;
  }

  public String getEchoMessage() {
    return echoMessage;
  }

  public void setEchoMessage( String echoMessage ) {
    this.echoMessage = echoMessage;
  }
}
