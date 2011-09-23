package org.pentaho.test.platform.engine.services.actions;

import org.pentaho.platform.api.action.IAction;

public class TestAction implements IAction {

  private String message;
  private boolean executeWasCalled = false;
  private String messageBoard;
  private CustomType custom;
  private String embeddedMessage;
  private Integer embeddedNumber;
  private Integer badEmbeddedNumber;
  private String complexInputWithSubEelements;
  
  @SuppressWarnings("nls")
  public TestAction() {
    System.out.println("new action created "+this);
  }

  public CustomType getCustom() {
    return custom;
  }

  public void setCustom(CustomType custom) {
    this.custom = custom;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
  }

  public void setMessageBoard(String messageBoard) {
    System.out.println("setMessageBoard "+this+": "+messageBoard);
    this.messageBoard = messageBoard;
  }

  public String getMessageBoard() {
    System.out.println("getMessageBoard "+this+": "+messageBoard);
    return messageBoard;
  }
  
  static class CustomType {
  }
  

  public void setEmbeddedMessage(String embeddedMessage) {
    this.embeddedMessage = embeddedMessage;
  }

  public String getEmbeddedMessage() {
    return embeddedMessage;
  }
  
  public Integer getEmbeddedNumber() {
    return embeddedNumber;
  }

  public void setEmbeddedNumber(Integer embeddedNumber) {
    this.embeddedNumber = embeddedNumber;
  }

  public void setBadEmbeddedNumber(Integer badEmbeddedNumber) {
    this.badEmbeddedNumber = badEmbeddedNumber;
  }

  public Integer getBadEmbeddedNumber() {
    return badEmbeddedNumber;
  }

  public void setComplexInputWithSubEelements(String complexInputWithSubEelements) {
    this.complexInputWithSubEelements = complexInputWithSubEelements;
  }

  public String getComplexInputWithSubEelements() {
    return complexInputWithSubEelements;
  }
}
