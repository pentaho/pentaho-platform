/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import java.security.Principal;

import org.apache.jackrabbit.core.security.authorization.JackrabbitAccessControlList;

/**
 * Extension of {@link JackrabbitAccessControlList} that adds owner and inheriting flag getters and setters. This 
 * interface is required as PentahoJackrabbitAccessControlList is default scoped. Therefore, code outside of Jackrabbit
 * cannot do {@code instanceof} checks on {@code AccessControlPolicy} instances. Instead, use this type with 
 * {@code instanceof}.
 * 
 * <p>
 * <pre>
 * {@code 
 * IPentahoJackrabbitAccessControlList jrPolicy = (IPentahoJackrabbitAccessControlList) acPolicy;
 * jrPolicy.setOwner(jrSession.getPrincipalManager().getPrincipal("jerry"));
 * }
 * </pre>
 * </p>
 * 
 * @author mlowery
 */
public interface IPentahoJackrabbitAccessControlList extends JackrabbitAccessControlList {

  public Principal getOwner();

  public boolean isEntriesInheriting();

  public void setOwner(Principal owner);

  public void setEntriesInheriting(boolean entriesInheriting);

}
