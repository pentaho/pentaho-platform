package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Rowell Belen
 */
public interface SolutionFileActionEventHandler extends EventHandler{
  void onFileAction(SolutionFileActionEvent event);
}
