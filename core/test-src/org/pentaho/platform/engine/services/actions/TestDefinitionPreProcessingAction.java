package org.pentaho.test.platform.engine.services.actions;

import java.util.List;

import org.pentaho.platform.api.action.ActionPreProcessingException;
import org.pentaho.platform.api.action.IDefinitionAwareAction;
import org.pentaho.platform.api.action.IPreProcessingAction;

@SuppressWarnings("nls")
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
    if(!isReadyToExecute) {
      throw new IllegalStateException("doPreExecution was not called before execute!");
    }
    executeWasCalled = true;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setInputNames(List<String> inputNames) {
    this.inputNames = inputNames;
  }

  public void setOutputNames(List<String> outputNames) {
    this.outputNames = outputNames;
  }

  public void doPreExecution() throws ActionPreProcessingException {
    doPreExecutionWasCalled = true;
    isReadyToExecute = true;
  }
}
