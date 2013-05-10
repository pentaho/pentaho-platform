package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Rowell Belen
 */
public class SolutionFolderActionEvent extends GwtEvent<SolutionFolderActionEventHandler>{

  public static Type<SolutionFolderActionEventHandler> TYPE = new Type<SolutionFolderActionEventHandler>();

  public SolutionFolderActionEvent(){
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
  public Type<SolutionFolderActionEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SolutionFolderActionEventHandler solutionEditEventHandler) {
    solutionEditEventHandler.onEdit(this);
  }
}
