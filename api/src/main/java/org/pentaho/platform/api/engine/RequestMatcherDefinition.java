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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.api.engine;

import java.util.Collection;

/**
 * This class represents a request matcher that identifies one or more endpoints.
 */
public class RequestMatcherDefinition {

  private String type;
  private String pattern;
  private Collection<String> methods;

  public RequestMatcherDefinition() {
  }

  public RequestMatcherDefinition( String type, String pattern ) {
    this( type, pattern, null );
  }

  public RequestMatcherDefinition( String type, String pattern, Collection<String> methods ) {
    this.type = type;
    this.pattern = pattern;
    this.methods = methods;
  }

  /**
   * Returns the type of request matcher.
   *
   * The only currently supported value is "regex", which performs a case-sensitive regular expression match.
   *
   * The default value is "regex".
   *
   * @return the type of request matcher.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Sets the request matcher type.
   *
   * @param type the new request matcher type.
   */
  public void setType( String type ) {
    this.type = type;
  }

  /**
   * Returns the request pattern.
   *
   * @return the request pattern.
   */
  public String getPattern() {
    return this.pattern;
  }

  /**
   * Sets the request pattern.
   *
   * @param pattern the new pattern.
   */
  public void setPattern( String pattern ) {
    this.pattern = pattern;
  }

  /**
   * Returns the request methods.
   *
   * @return the request methods.
   */
  public Collection<String> getMethods() {
    return this.methods;
  }

  /**
   * Sets the request methods.
   *
   * @param methods the new request methods.
   */
  public void setMethods( Collection<String> methods ) {
    this.methods = methods;
  }
}
