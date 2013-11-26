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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security;

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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 * @created June 2011 
 * @author mbatchelor
 */

import org.apache.commons.codec.binary.Base64;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.springframework.beans.factory.InitializingBean;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

public class CipherEncryptionService implements IPasswordService, InitializingBean {

  private String saltString = "_CyPh3r_";
  private String algorithm = "PBEWithMD5AndDES";
  private String encryptionKey = "P3ntah0C1ph3r";
  private final int saltLength = 8; // Eight Byte Cipher Salt
  private int iterations = 19;

  public void setSalt( String value ) {
    this.saltString = value;
  }

  public String getSalt() {
    return this.saltString;
  }

  public void setAlgorithm( String value ) {
    this.algorithm = value;
  }

  public String getAlgorithm() {
    return this.algorithm;
  }

  public void setEncryptionKey( String value ) {
    this.encryptionKey = value;
  }

  public String getEncryptionKey() {
    return this.encryptionKey;
  }

  public void setIterations( int value ) {
    this.iterations = value;
  }

  public int getIterations() {
    return this.iterations;
  }

  private AlgorithmParameterSpec paramSpec;
  private SecretKey secretKey;

  public void afterPropertiesSet() throws ObjectFactoryException {
    if ( ( saltString == null ) || ( algorithm == null ) || ( encryptionKey == null ) ) {
      throw new ObjectFactoryException( "Required properties not set - need Salt, algorithm and encryption key" );
    }
    if ( saltString.length() != this.saltLength ) {
      // Make sure that the salt length is 8 bytes - the PBEParameterSpec doesn't anything but
      if ( saltString.length() < saltLength ) {
        saltString = ( saltString + "!@#$%^&*" ).substring( 0, saltLength ); // postfix bytes to pad it out
      } else if ( saltString.length() > saltLength ) {
        saltString = saltString.substring( 0, saltLength ); // Trim off longer than 8-bytes
      }
    }
    byte[] saltBytes = saltString.getBytes();
    paramSpec = new PBEParameterSpec( saltBytes, getIterations() );
    PBEKeySpec skeySpec = new PBEKeySpec( getEncryptionKey().toCharArray(), saltBytes, getIterations() );
    try {
      secretKey = SecretKeyFactory.getInstance( getAlgorithm() ).generateSecret( skeySpec );
    } catch ( Exception ex ) {
      ex.printStackTrace();
      throw new ObjectFactoryException( "Encryption requested not available" );
    }

  }

  // From IPasswordService

  @Override
  public String decrypt( String encryptedPassword ) throws PasswordServiceException {
    try {
      Cipher decCipher = Cipher.getInstance( secretKey.getAlgorithm() );
      decCipher.init( Cipher.DECRYPT_MODE, secretKey, paramSpec );
      byte[] toDecryptBytes = Base64.decodeBase64( encryptedPassword.getBytes() );
      byte[] decryptedBytes = decCipher.doFinal( toDecryptBytes );
      return new String( decryptedBytes, LocaleHelper.getSystemEncoding() );
    } catch ( Exception ex ) {
      throw new PasswordServiceException( ex );
    }
  }

  @Override
  public String encrypt( String clearPassword ) throws PasswordServiceException {
    try {
      Cipher encCipher = Cipher.getInstance( secretKey.getAlgorithm() );
      encCipher.init( Cipher.ENCRYPT_MODE, secretKey, paramSpec );
      byte[] toEncryptBytes = clearPassword.getBytes( LocaleHelper.getSystemEncoding() );
      byte[] encBytes = encCipher.doFinal( toEncryptBytes );
      byte[] base64Bytes = Base64.encodeBase64( encBytes );
      return new String( base64Bytes );
    } catch ( Exception ex ) {
      throw new PasswordServiceException( ex );
    }

  }

  public static void main( String[] args ) {
    CipherEncryptionService service = new CipherEncryptionService();
    try {
      service.afterPropertiesSet();
      if ( args.length != 2 ) {
        throw new IllegalArgumentException( "Usage: CipherEncryptionService encrypt|decrypt password" );
      }
      if ( args[0].equalsIgnoreCase( "encrypt" ) ) {
        System.out.println( service.encrypt( args[1] ) );
      } else if ( args[0].equalsIgnoreCase( "decrypt" ) ) {
        System.out.println( service.decrypt( args[1] ) );
      } else {
        throw new IllegalArgumentException( "Usage: CipherEncryptionService encrypt|decrypt password" );
      }
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }

  }

}
