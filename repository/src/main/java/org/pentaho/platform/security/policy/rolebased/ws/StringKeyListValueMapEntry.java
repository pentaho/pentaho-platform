/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
