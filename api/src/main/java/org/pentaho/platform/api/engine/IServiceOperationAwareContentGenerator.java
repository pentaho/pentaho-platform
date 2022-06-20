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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.api.engine;

import java.util.Map;

/**
 * The <code>IServiceMappingAwareContentGenerator</code> interface extends the {@link IContentGenerator} interface
 * with the capability to obtain the service operation name of executions,
 * for cases when it is not derived by the normal method:
 * <ul>
 *   <li>
 *     For <b>legacy</b> content generators
 *     (handled by <code>org.pentaho.platform.web.servlet.GenericServlet</code>
 *     and accessible via <code>/content/{content-generator-id}/{...path}</code>),
 *     the operation name is the <code>path</code> parameter of the <code>path</code> parameter provider,
 *     and contains the value of the <code>{path}</code> URL segment.
 *   </li>
 *   <li>
 *     For <b>new</b> content generators
 *     (handled by <code>org.pentaho.platform.web.http.api.resources.RepositoryResource</code>
 *     and accessible via <code>/api/repos/{context-id}/{content-generator-id}/{...cmd}</code>),
 *     the operation name is the <code>cmd</code> parameter of the <code>path</code> parameter provider,
 *     and contains the value of the <code>{cmd}</code> URL segment.
 *   </li>
 * </ul>
 * <p>
 * While not common, for some content generators, the operation name is derived by other means,
 * such as by the value of a query string parameter.
 * <p>
 * The service operation identifier is used by the CSRF protection system to allow enabling/disabling of
 * CSRF validation for specific operations.
 */
public interface IServiceOperationAwareContentGenerator extends IContentGenerator {

  /**
   * Gets the service operation name of the current execution.
   * <p>
   * It is an error to call this method before the {@link IContentGenerator#setParameterProviders(Map)} method
   * has been called (with a non-<code>null</code> value).
   *
   * @return The service operation name.
   */
  String getServiceOperationName();
}
