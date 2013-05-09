package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Rowell Belen
 */
public class SolutionFileActionEvent extends GwtEvent<SolutionFileActionEventHandler>{

  public static Type<SolutionFileActionEventHandler> TYPE = new Type<SolutionFileActionEventHandler>();

  public SolutionFileActionEvent(){
  }

  private String action;
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public Type<SolutionFileActionEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SolutionFileActionEventHandler solutionFileActionEventHandler) {
    solutionFileActionEventHandler.onFileAction(this);
  }
}
