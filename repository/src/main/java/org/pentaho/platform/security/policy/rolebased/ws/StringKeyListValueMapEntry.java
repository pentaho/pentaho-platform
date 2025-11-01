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


package org.pentaho.platform.security.policy.rolebased.ws;

import java.util.List;

/**
 * JAXB-safe map entry. (Goes in a List.)
 * 
 * @author mlowery
 */
public class StringKeyListValueMapEntry {
  public String key;

  public List<String> value;
}
