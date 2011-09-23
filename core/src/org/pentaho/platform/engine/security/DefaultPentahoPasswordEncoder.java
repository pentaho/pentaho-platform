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
 *
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;
import org.pentaho.platform.engine.security.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.security.providers.encoding.PasswordEncoder;

/**
 * Default password encoder for the BI Server.
 * 
 * <p>This encoder Base64-encodes the raw password.</p>
 * 
 * <p>This class is instantiated by Pentaho Admin Console so there should not be a dependency on classes to which PAC
 * will not have access.</p>
 * 
 * <p>This implementation of password encoding is completely independent of any datasource connection password 
 * encoding.</p>
 * 
 * @author mlowery
 */
public class DefaultPentahoPasswordEncoder implements PasswordEncoder {

  public String encodePassword(final String rawPass, final Object salt) throws DataAccessException {
    Validate.notNull(rawPass, Messages.getInstance().getString("DefaultPentahoPasswordEncoder.ERROR_0001_RAWPASS_CANNOT_BE_NULL")); //$NON-NLS-1$
    // same code as org.pentaho.platform.util.Base64PasswordService.encrypt()
    return StringUtil.isEmpty(rawPass) ? rawPass : new String(Base64.encodeBase64(rawPass.getBytes()));
  }

  public boolean isPasswordValid(final String encPass, final String rawPass, final Object salt)
      throws DataAccessException {
    Validate.notNull(encPass, Messages.getInstance().getString("DefaultPentahoPasswordEncoder.ERROR_0002_ENCPASS_CANNOT_BE_NULL")); //$NON-NLS-1$
    Validate.notNull(rawPass, Messages.getInstance().getString("DefaultPentahoPasswordEncoder.ERROR_0001_RAWPASS_CANNOT_BE_NULL")); //$NON-NLS-1$
    String encodedRawPass = encodePassword(rawPass, salt);
    return encPass.equals(encodedRawPass);
  }

}
