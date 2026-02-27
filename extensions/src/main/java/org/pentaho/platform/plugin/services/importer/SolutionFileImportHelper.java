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


package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SolutionFileImportHelper {

  IPlatformMimeResolver mimeResolver;
  static IPlatformMimeResolver testMimeResolver; // Allows Injection for unit tests

  SolutionFileImportHelper() {
    mimeResolver = testMimeResolver == null ? PentahoSystem.get( IPlatformMimeResolver.class ) : testMimeResolver;
  }

  public String getMime( String fileName ) {
    return mimeResolver.resolveMimeForFileName( fileName );
  }

  public IMimeType getMimeType( String fileName ) {
    return mimeResolver.resolveMimeTypeForFileName( fileName );
  }

  public boolean isInApprovedExtensionList( String fileName ) {
    boolean isInTheApprovedExtensionList = false;
    IMimeType mimeType = getMimeType( fileName );
    if ( mimeType != null ) {
      isInTheApprovedExtensionList = ( mimeType.getConverter() != null );
    }
    return isInTheApprovedExtensionList;
  }

  public boolean isInHiddenList( String fileName ) {
    boolean isInTheHiddenList = false;
    IMimeType mimeType = getMimeType( fileName );
    if ( mimeType != null ) {
      isInTheHiddenList = mimeType.isHidden();
    }
    return isInTheHiddenList;

  }
}
