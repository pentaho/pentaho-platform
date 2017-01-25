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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import java.util.List;

/**
 * Describes a content type. This class is used to describe content types that users can get to. Implementations of
 * this class are also used as keys to content generators (IContentGenerator)
 * 
 * @author jamesdixon
 * 
 */
public interface IContentInfo {

  /**
   * The extension of files that generate this content type. e.g. 'xaction'
   * 
   * @return file extension
   */
  public String getExtension();

  /**
   * The title of this content type as presented to the user Implementors of this interface should provide
   * localization for the title e.g. 'Executable action'
   * 
   * @return title
   */
  public String getTitle();

  /**
   * The description of this content type as presented to the user Implementors of this interface should provide
   * localization for the description
   * 
   * @return title
   */
  public String getDescription();

  /**
   * The mime-type of the generated content e.g. 'text/html'
   * 
   * @return mime type
   * @deprecated Don't Use - this is way too early to know the mime type
   */
  @Deprecated
  public String getMimeType();

  /**
   * Returns a list of the operations that are available for this content type
   * 
   * @return the available plugin operations
   */
  public List<IPluginOperation> getOperations();

  /**
   * Returns an url to an icon for this content type
   * 
   * @return
   */
  public String getIconUrl();

  /**
   * flags used by import handler to determine if this extension can be used
   * 
   * @return
   */
  public boolean canImport();

  /**
   * flags used by export handler to determine if this extension can be used
   * 
   * @return
   */
  public boolean canExport();
}
