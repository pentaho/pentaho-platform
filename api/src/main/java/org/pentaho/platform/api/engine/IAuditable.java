/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.api.engine;

/**
 * Any subsystem that implements the IAuditable interface is candidate for auditing within the platform. This
 * interface outlines the necessary pieces of information that an object must be able to return in order to be
 * auditable.
 */
public interface IAuditable {

  /**
   * Returns the Java class name for this object.
   * 
   * @return the name of the object (the Java class name)
   */
  public String getObjectName();

  /**
   * Return the id for the execution of a given action sequence document.
   * 
   * @return the process id
   */
  public String getProcessId();

  /**
   * Return the name of the action sequence. Today, that name is synonymous with the name of the action sequence
   * document in the solution repository.
   * 
   * @return the name of the action sequence
   */
  public String getActionName();

  /**
   * Returns a unique id (across classes and instances) for this auditable object.
   * 
   * @return the auditable's id
   */
  public String getId();

}
