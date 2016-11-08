/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
