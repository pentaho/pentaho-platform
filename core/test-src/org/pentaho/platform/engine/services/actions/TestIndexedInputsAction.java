package org.pentaho.platform.engine.services.actions;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.action.IAction;

@SuppressWarnings("nls")
public class TestIndexedInputsAction implements IAction {

  private List<String> messages = new ArrayList<String>();
  private String scalarMessage;
  private List<String> otherMessages = new ArrayList<String>();
  
  {
    otherMessages.add("dummy value");
    otherMessages.add("dummy value");
    otherMessages.add("dummy value");
    otherMessages.add("dummy value");
  }

  //
  // The "messages" property
  //
  public String getMessages(int index) {
    return messages.get(index);
  }
  
  public List<String> getAllMessages() {
    return messages;
  }

  public void setMessages(int index, String message) {
    messages.add(message);
  }
  
  /**
   * We must specify a getter method for the indexed "message" property
   * so it will be an conformant JavaBean property.  BeanUtils requires indexed
   * properties to also be JavaBean spec.
   */
  public String getMessages() {
    throw new UnsupportedOperationException("This should never be called");
  }
  
  //
  // The "otherMessage" property
  //
  public List<String> getOtherMessages() {
    return otherMessages;
  }
  public void setOtherMessage(String s) {
    throw new UnsupportedOperationException("This should not be called");
  }
  
  public void execute() throws Exception {
  }

  /**
   * We have only a setter for this property to show that a getter is not required
   */
  public void setScalarMessage(String s) {
    scalarMessage = s;
  }
  public String getTextOfScalarMessage() {
    return scalarMessage;
  }
}
