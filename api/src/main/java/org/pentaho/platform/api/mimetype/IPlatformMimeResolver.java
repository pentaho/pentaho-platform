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


package org.pentaho.platform.api.mimetype;

import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;

/**
 * Implementations of this class compute mime-types based on the given IPlatformImportBundle.
 * <p/>
 * Author: tkafalas 4/1/2015
 */
public interface IPlatformMimeResolver {
  String resolveMimeForBundle( IPlatformImportBundle bundle );

  String resolveMimeForFileName( String fileName );

  IMimeType resolveMimeTypeForFileName( String fileName );

  void addMimeType( IMimeType mimeType );
}
