package org.pentaho.mantle.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Rowell Belen
 */
public interface SolutionFolderActionEventHandler extends EventHandler{
  void onEdit(SolutionFolderActionEvent event);
}
