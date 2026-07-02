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

/**
 * Converts between absolute and relative paths. JCR requires absolute paths but clients only know relative paths.
 * 
 * @author mlowery
 */
public interface IPathConversionHelper {

  String absToRel( final String absPath );

  String relToAbs( final String relPath );

}
