package org.pentaho.mantle.client.admin;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ISysAdminPanel {
  public void activate();
  public void passivate(AsyncCallback<Boolean> passivateCallback);
  public String getId();
}
