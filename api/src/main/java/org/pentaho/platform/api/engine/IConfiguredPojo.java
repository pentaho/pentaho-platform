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
