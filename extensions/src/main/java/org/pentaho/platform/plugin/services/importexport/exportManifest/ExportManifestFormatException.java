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


package org.pentaho.platform.plugin.services.importexport.exportManifest;

/**
 * Exceptions handled explicitly by this package dealing with errors in XML values that are not caught by JAXB will
 * represented by this object. It should be assumed that if this exception is thrown then the message will contain text
 * that is localized and suitable for an Import Exception log.
 * 
 * @author tkafalas
 */
public class ExportManifestFormatException extends Exception {

  private static final long serialVersionUID = 1L;

  ExportManifestFormatException( String message ) {
    super( message );
  }

  ExportManifestFormatException( String message, Throwable cause ) {
    super( message, cause );
  }
}
