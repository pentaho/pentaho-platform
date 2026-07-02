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


package org.pentaho.platform.repository2.unified.jcr;

import java.security.Principal;
import java.util.Enumeration;

/**
 * Marker interface that denotes principals that are part of internal ACEs that are never exposed to clients.
 * 
 * @author mlowery
 */
public interface IPentahoInternalPrincipal extends Principal {

}
