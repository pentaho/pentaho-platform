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
