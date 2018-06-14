/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.engine;

public interface IContentGeneratorInvoker {

  /**
   * Returns a IContentGenerator
   *
   * @return IContentGenerator
   */
  IContentGenerator getContentGenerator();

  /**
   * Predicate that answers whether the related IContentGenerator is able to support the content identified
   * by a contentTypeId
   *
   * contentTypeId is usually the aggregation of a content type ( such as ktr, prpt, ... ) with an action/perspective
   * ( such as viewer, edit, generatedContent, .. )
   *
   * @param contentTypeId the content type Id of the item; examples: ktr.viewer, prpt.edit, ...

   * @return true if the contentType + perspectiveName pair is supported/handled by the related IContentGenerator
   */
  boolean isSupportedContent( String contentTypeId );
}
