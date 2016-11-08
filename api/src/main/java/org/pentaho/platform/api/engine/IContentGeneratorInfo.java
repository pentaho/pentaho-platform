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

/**
 * This should only be used internally by the plugin manager
 */
public interface IContentGeneratorInfo {

  public String getDescription();

  public String getId();

  /**
   * @deprecated URL is determined by the system
   */
  @Deprecated
  public String getUrl();

  public String getTitle();

  public String getType();

  /**
   * @deprecated file info generators or solution file meta providers are now associated with content types, not
   *             content generators. This method is not called from within the platform.
   */
  @Deprecated
  public String getFileInfoGeneratorClassname();

  public String getClassname();
}
