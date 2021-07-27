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

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Locale;

/**
 * Provides an overall representation of the concept of a session. Sessions are not necessarily user-based, but
 * typically wrap the HttpSession object and PortletSession object in a standard framework with methods that session
 * objects typically provide.
 * 
 * @author jdixon
 * 
 */

public interface IPentahoSession extends ILogger, IAuditable {

  /**
   * Key for searching the tenant ID.
   */
  public static final String TENANT_ID_KEY = "org.pentaho.tenantId"; //$NON-NLS-1$

  /**
   * Roles that are authorized in current session.
   */
  public static final String SESSION_ROLES = "roles"; //$NON-NLS-1$

  /**
   * The name of the session attribute where the session locale override is stored.
   *
   * The format of the locale override attribute is Java's legacy {@link Locale#toString()} format,
   * which uses underscores.
   */
  public static final String ATTRIBUTE_LOCALE_OVERRIDE = "locale_override";

  /**
   * Gets the name for this session, for example if this is an authenticated HTTP or Portlet session, the name will be
   * the name of the user.
   * 
   * @return Returns the name for this session.
   */
  public String getName();

  /**
   * Gets the ID for this session. This is typically a GUID or semi-unique string.
   * 
   * @return Returns the ID for this session.
   */
  public String getId();

  /**
   * Sets the name of the action sequence document that the session is currently performing.
   * 
   * @param actionName
   *          The name of the action sequence document.
   */
  public void setActionName( String actionName );

  /**
   * Sets the name of the process for which an action sequence is being performed.
   * 
   * @param processId
   *          The name of the process.
   */
  public void setProcessId( String processId );

  /**
   * Destroys any resources owned by the session object.
   * 
   */
  public void destroy();

  /**
   * Gets the value of a named session attribute.
   * 
   * @param attributeName
   *          The name of the attribute.
   * @return Returns the value of the attribute.
   */
  public Object getAttribute( String attributeName );

  /**
   * Sets the value of the session attribute.
   * 
   * @param attributeName
   *          The name of the attribute.
   * @param value
   *          The value of the attribute.
   */
  public void setAttribute( String attributeName, Object value );

  /**
   * Removes an attribute from the session and returns it.
   * 
   * @param attributeName
   *          The name of the attribute to remove.
   * @return Returns the value of the removed attribute.
   */
  public Object removeAttribute( String attributeName );

  /**
   * Returns an enumeration of the names of the attributes stored in the session.
   * 
   * @return Returns the enumeration of the attributes names.
   */
  @SuppressWarnings( "rawtypes" )
  public Iterator getAttributeNames();

  /**
   * Gets the locale of the session.
   *
   * This is the <i>initial</i> session locale, defined at construction.
   * 
   * @return Returns the locale of the session.
   */
  public Locale getLocale();

  /**
   * Gets the locale override of the session.
   *
   * The format of the locale override attribute should be that returned by {@link Locale#toString()}.
   *
   * If the stored locale is empty, {@code null} is returned instead.
   *
   * The locale override string is stored in the session attribute named {@link #ATTRIBUTE_LOCALE_OVERRIDE}.
   *
   * @return The locale override, if any; {@code null}, otherwise.
   *
   * @see #getAttribute(String)
   * @see #setAttribute
   */
  public default String getAttributeLocaleOverride() {
    return StringUtils.defaultIfEmpty( (String) getAttribute( ATTRIBUTE_LOCALE_OVERRIDE ), null );
  }

  /**
   * Sets the locale override of the session.
   *
   * If the given locale is empty, it is converted to {@code null}.
   *
   * The format of the locale override attribute should be that returned by {@link Locale#toString()}.
   *
   * The locale override string is stored in the session attribute named {@link #ATTRIBUTE_LOCALE_OVERRIDE}.
   *
   * @param locale The locale override.
   *
   * @see #getAttribute(String)
   * @see #setAttribute
   */
  public default void setAttributeLocaleOverride( String locale ) {
    setAttribute( ATTRIBUTE_LOCALE_OVERRIDE, StringUtils.defaultIfEmpty( locale, null ) );
  }

  /**
   * Gets whether the session is known to be authenticated or not.
   * 
   * @return Returns true if the session is authenticated.
   */
  public boolean isAuthenticated();

  /**
   * Sets the name of the session and indicates that the session is authenticated. If this is an HTTP or Portlet session
   * the name should be the name of the user that is logged in (e.g. using <code>request.getRemoteUser()</code> ).
   * 
   * @param name
   *          The name of the session.
   */
  public void setAuthenticated( String name );

  /**
   * Sets the indication that the user is no longer authenticated.
   */
  public void setNotAuthenticated();

  /**
   * Toggles on an alert condition indicating that the background execution of a task has completed during this session.
   * 
   */
  public void setBackgroundExecutionAlert();

  /**
   * Checks the status of a background execution task.
   * 
   * @return Returns true if a background execution has triggered an alert.
   */
  public boolean getBackgroundExecutionAlert();

  /**
   * Toggles off the background execution alert status.
   * 
   */
  public void resetBackgroundExecutionAlert();

}
