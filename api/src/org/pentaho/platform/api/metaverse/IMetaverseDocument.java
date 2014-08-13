/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.metaverse;

/**
 * The IMetaverseDocument interface represents a document in the metaverse.
 */
public interface IMetaverseDocument extends IMetaverseComponentDescriptor, IIdentifierModifiable {

  /**
   * Gets the object representing the content of this document
   * 
   * @return the content of this object
   */
  public Object getContent();

  /**
   * Sets the content object for this document.
   * 
   * @param content
   *          the new content
   */
  public void setContent( Object content );

  /**
   * Gets the file extension for this document;
   * characters only, the dot (.) is excluded
   *
   * @return the extension associated with this document
   */
  public String getExtension();

  /**
   *  Set the extension for this document
   *
   * @param extension
   */
  public void setExtension( String extension );

  /**
   * Returns the RFC compliant string version of the MIME content type,
   * if it can be determined for this document.
   *
   * @return MIME content type if available; otherwise null
   */
  public String getMimeType();

  /**
   *  Set the MIME content type for this document.
   *
   * @param type
   */
  public void setMimeType( String type );


}
