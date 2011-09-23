/*
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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 15, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.util;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;

public class Base64PasswordService implements IPasswordService {

  public String encrypt(String password) throws PasswordServiceException {
    return StringUtil.isEmpty(password) ? password : new String(Base64.encodeBase64(password.getBytes()));
  }

  public String decrypt(String encryptedPassword)
      throws PasswordServiceException {
    return StringUtil.isEmpty(encryptedPassword) ? encryptedPassword : new String(Base64.decodeBase64(encryptedPassword.getBytes()));
  }
}