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

import java.util.Map;
import java.util.Set;

/**
 * The interface for a POJO component that wants access to system settings. This is an optional interface.
 * 
 * @author jamesdixon
 * @deprecated Pojo components are deprecated, use {@link org.pentaho.platform.api.action.IAction}
 */
@Deprecated
public interface IConfiguredPojo {

  /**
   * Returns a list of Strings that define system settings that a POJO component would like read for it. The
   * strings are expected to be in the format of : folder/file{setting path} e.g.
   * "smtp-email/email_config.xml{email-smtp/properties/mail.smtp.host}"
   * 
   * @return List of configuration settings paths
   */
  public Set<String> getConfigSettingsPaths();

  /**
   * Sets the configuration settings that were requested via a call to getConfigSettingsPaths(). The keys of the
   * map will be the Strings that were returned by the call to getConfigSettingsPaths. e.g.
   * "smtp-email/email_config.xml{email-smtp/properties/mail.smtp.host}" -> "myhost.com"
   * 
   * @param props
   * @return
   */
  public boolean configure( Map<String, String> props );

}
