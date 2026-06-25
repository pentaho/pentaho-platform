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



package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.MapAdapter;

import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

/**
 * User: nbaker Date: 7/15/13
 */
@XmlJavaTypeAdapter( MapAdapter.class )
public class Parameters extends HashMap<String, String> {

}
