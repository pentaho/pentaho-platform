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
 *
 */
public interface IDocumentLocator extends IRequiresMetaverseBuilder {

  /**
   * Starts the scanning procedure used by this document locator
   * @throws org.pentaho.platform.api.metaverse.MetaverseLocatorException if
   * scan cannot be executed
   */
  void startScan() throws MetaverseLocatorException;

  /**
   * Stops the scanning procedure used by this document locator
   */
  void stopScan();

  /**
   * Adds to the locator a listener for document events (document found, created, deleted, etc.)
   */
  void addDocumentListener( IDocumentListener listener );

  /**
   * Removes the specified listener from this locator
   *
   * @param listener the document listener to remove
   */
  void removeDocumentListener( IDocumentListener listener );

  /**
   * Notify listeners of a document event
   *
   * @param event the document event to report
   */
  void notifyListeners( IDocumentEvent event );
}
