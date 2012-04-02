package org.pentaho.platform.repository2.unified.jcr;

import java.security.Principal;

/**
 * Marker interface that denotes principals that are part of internal ACEs that are never exposed to clients.
 * 
 * @author mlowery
 */
public interface IPentahoInternalPrincipal extends Principal {

}
