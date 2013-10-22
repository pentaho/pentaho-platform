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

import org.pentaho.platform.api.action.ActionPreProcessingException;
import org.pentaho.platform.api.action.IDefinitionAwareAction;
import org.pentaho.platform.api.action.IPreProcessingAction;

import java.util.List;

@SuppressWarnings( "nls" )
public class TestDefinitionPreProcessingAction implements IDefinitionAwareAction, IPreProcessingAction {

  private String message;
  private boolean executeWasCalled = false;
  private boolean doPreExecutionWasCalled = false;
  private List<String> inputNames;
  private List<String> outputNames;
  private boolean isReadyToExecute;

  public boolean isDoPreExecutionWasCalled() {
    return doPreExecutionWasCalled;
  }

  public List<String> getInputNames() {
    return inputNames;
  }

  public List<String> getOutputNames() {
    return outputNames;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    if ( !isReadyToExecute ) {
      throw new IllegalStateException( "doPreExecution was not called before execute!" );
    }
    executeWasCalled = true;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setInputNames( List<String> inputNames ) {
    this.inputNames = inputNames;
  }

  public void setOutputNames( List<String> outputNames ) {
    this.outputNames = outputNames;
  }

  public void doPreExecution() throws ActionPreProcessingException {
    doPreExecutionWasCalled = true;
    isReadyToExecute = true;
  }
}
