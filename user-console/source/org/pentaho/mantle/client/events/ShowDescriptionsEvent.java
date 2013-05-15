package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Diogo Mariano
 */
public class ShowDescriptionsEvent extends GwtEvent<ShowDescriptionsEventHandler>{

  public static Type<ShowDescriptionsEventHandler> TYPE = new Type<ShowDescriptionsEventHandler>();

  public ShowDescriptionsEvent(){
  }

  private boolean value;

  public boolean getValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }

  @Override
  public Type<ShowDescriptionsEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ShowDescriptionsEventHandler showDescriptionsEventHandler) {
    showDescriptionsEventHandler.onEdit(this);
  }
}
