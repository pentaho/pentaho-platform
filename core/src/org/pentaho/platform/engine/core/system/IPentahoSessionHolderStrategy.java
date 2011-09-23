package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * A strategy for storing an IPentahoSession against a thread.
 * 
 * <p>Inspired by {@code org.springframework.security.context.SecurityContextHolderStrategy}.</p>
 * 
 * @author mlowery
 */
public interface IPentahoSessionHolderStrategy {

  /**
   * Sets the current session.
   * 
   * @param session session to set
   */
  void setSession(IPentahoSession session);
  
  /**
   * Returns the current session.
   * 
   * @return session
   */
  IPentahoSession getSession();

  /**
   * Clears the current session.
   */
  void removeSession();
  
}
