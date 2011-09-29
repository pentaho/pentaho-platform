package org.pentaho.mantle.client.commands;


import com.google.gwt.user.client.Command;

public interface CommandExec {
  public void execute(String commandName);
  public Command lookupCommand(String commandName);
}
