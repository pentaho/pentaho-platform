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

package org.pentaho.test.platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class used to collect method call information during unit testing. It can be used with mock objects to
 * track which methods have been invoked and can hold information about the parameters provided. </p> Example:
 * <code>
 *   class MockClass extends ClassUnderTest {
 *      private List<MethodTrackingData> methods = new List<MethodTrackingData>();
 * 
 *      public void method(String arg) {
 *        methods.add(new MethodTrackingData("method").addParameter("arg", arg));
 *        ...
 *      }
 *   }
 * </code>
 */
public class MethodTrackingData {
  /**
   * The method that was invoked
   */
  private String methodName;

  /**
   * The parameters that were provided at the time of method invocation
   */
  private Map<String, Object> parameters = new HashMap<String, Object>();

  /**
   * Creates a new instance to track a method call
   * 
   * @param methodName
   *          the name of the method that was invoked
   */
  public MethodTrackingData( final String methodName ) {
    this.methodName = methodName;
  }

  /**
   * Adds parameter information about the method call. This method should be used once per parameter or other piece
   * of information that should be tracked.
   * 
   * @param paramName
   *          the name of the parameter at method invocation
   * @param paramValue
   *          the value of the parameter at method invocation
   * @return {@code this} so that the methods can be easily chained
   */
  public MethodTrackingData addParameter( final String paramName, final Object paramValue ) {
    parameters.put( paramName, paramValue );
    return this;
  }

  /**
   * @return the name of the method that was invoked
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return the map which contains the information about the parameters that were provided when the method was
   *         invoked
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }
}
