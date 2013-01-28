package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.Map;

import org.pentaho.platform.api.action.IAction;

public interface ISchedulerListener {
  public void jobCompleted(IAction actionBean, String actionUser, Map<String, Serializable> params, IBackgroundExecutionStreamProvider streamProvider);
}
