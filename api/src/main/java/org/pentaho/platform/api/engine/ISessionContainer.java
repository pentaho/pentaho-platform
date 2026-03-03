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
 * A "something" that refers to a session.
 * 
 * This is mainly used to ensure that sessions don't leak.
 * 
 * @author <a href="mailto:andreas.kohn@fredhopper.com">Andreas Kohn</a>
 * @see BISERVER-2639
 */
/*
 * TODO: should provide a getSession(), or ideally a 'clearSessionIfCurrent(IPentahoSession s)' to facilitate easy
 * cleaning.
 */
public interface ISessionContainer {
  /**
   * Set the session for this session container.
   * 
   * @param sess
   *          The IPentahoSession to set
   */
  public void setSession( IPentahoSession sess );
}
