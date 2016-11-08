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

import java.util.regex.Matcher;

public interface IParameterResolver {

  /**
   * Provides a way for components to inject their own replacements of parameter markers in the provided template.
   * This currently exposes too much of the internals of TemplateUtil IMO, but without serious surgery on the
   * TemplateUtil, this is about the only way to accomplish the task.
   * 
   * @param template
   *          The string containing replacement parameters
   * @param parameterName
   *          The name of the located parameter in the template
   * @param parameterMatcher
   *          The Regex matcher that located the parameter
   * @param copyStart
   *          The current start to copy from the template
   * @param result
   *          The final string with the parameter replacements inlined
   * @return integer indicating the new copyStart to be used in the TemplateUtil in the event that the component
   *         handled the parameter. If negative, then no processing was done in the component. Any value greater
   *         than or equal to zero indicates processing happened in the component.
   * 
   *         TODO: Change this interface to make it easier to do things without exposing the internals of
   *         TemplateUtil.
   */
  public int resolveParameter( String template, String parameterName, Matcher parameterMatcher, int copyStart,
      StringBuffer result );

}
