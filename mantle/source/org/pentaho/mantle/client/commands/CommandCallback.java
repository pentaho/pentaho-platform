package org.pentaho.mantle.client.commands;

/**
 * Receives notifications of Command execution. This allows code to perform actions dependent
 * upon the completion.
 * 
 * @author nbaker
 */
public interface CommandCallback {
  void afterExecute();
}
