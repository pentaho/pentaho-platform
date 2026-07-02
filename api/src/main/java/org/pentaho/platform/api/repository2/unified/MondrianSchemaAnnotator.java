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


package org.pentaho.platform.api.repository2.unified;

import java.io.InputStream;

/**
 * Provides interface for applying model annotations to a Mondrian schema.
 */
public interface MondrianSchemaAnnotator {
  InputStream getInputStream( InputStream schemaInputStream, InputStream annotationsInputStream );
}
