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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.util;

import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.Random;

public class EncryptPasswordService implements IPasswordService {
  static String SECURITY_DATA_FILE = "security_data.properties";

  private final String ALGORITHM = "PBKDF2WithHmacSHA1";
  private final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
  private final String DICT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*(),./;'<>?:[]{}|";
  private final int PASSWORD_LENGTH = 16;
  private final int SALT_LENGTH = 8;
  private final int ITERATION_COUNT = 65536;
  private final int KEY_LENGTH = 256;

  private EncryptionKey key;

  public static void main( String[] args ) throws Exception {
    if ( args.length < 1 ) {
      return;
    }

    EncryptPasswordService sps = new EncryptPasswordService();
    sps.pbe( args[ 0 ] );
  }

  private void pbe( String ldapPassword ) throws Exception {
    String encrypted = encrypt( ldapPassword );
    String decrypted = decrypt( encrypted );

    if ( ldapPassword.equals( decrypted ) ) {
      System.out.println( "Encrypted LDAP password: " + encrypted );
      System.out.println( "Encryption key generated successfully." );
    }
  }

  private String generatePassword() {
    StringBuilder buf = new StringBuilder();
    Random random = new Random();
    int dictLength = DICT.length();
    for ( int i = 0; i < PASSWORD_LENGTH; i++ ) {
      int n = random.nextInt( dictLength );
      buf.append( DICT.charAt( n ) );
    }
    return buf.toString();
  }

  private byte[] generateSalt() {
    byte[] saltBytes = new byte[ SALT_LENGTH ];
    new SecureRandom().nextBytes( saltBytes );
    return saltBytes;
  }

  private SecretKeySpec createSecret( EncryptionKey key ) throws Exception {
    SecretKeyFactory factory = SecretKeyFactory.getInstance( ALGORITHM );
    PBEKeySpec spec = new PBEKeySpec( key.password.toCharArray(), key.saltBytes, ITERATION_COUNT, KEY_LENGTH );

    SecretKey secretKey = factory.generateSecret( spec );
    return new SecretKeySpec( secretKey.getEncoded(), "AES" );
  }

  private File getSecurityDataFile() {
    return new File( System.getProperty( "user.home" ), SECURITY_DATA_FILE );
  }

  public String encrypt( String plainText ) throws PasswordServiceException {
    try {
      File keyFile = getSecurityDataFile();
      if ( keyFile.exists() ) {
        readKeyFile();
      } else {
        key = new EncryptionKey();
        key.password = generatePassword();
        key.saltBytes = generateSalt();
      }

      String encrypted = _encrypt( key, plainText );

      if ( !keyFile.exists() ) {
        try ( FileWriter writer = new FileWriter( keyFile ) ) {
          Properties prop = new Properties();
          prop.setProperty( "password", key.password );
          prop.setProperty( "salt", Base64.getEncoder().encodeToString( key.saltBytes ) );
          prop.setProperty( "iv", Base64.getEncoder().encodeToString( key.ivBytes ) );

          prop.store( writer, "" );
          writer.close();
          System.out.println( "Encryption key saved successfully." );
        }
      }

      return encrypted;
    } catch ( Exception ex ) {
      throw new PasswordServiceException( ex );
    }
  }

  public String decrypt( String encryptedPassword ) throws PasswordServiceException {
    try {
      if ( key == null ) {
        readKeyFile();
      }

      return _decrypt( key, encryptedPassword );
    } catch ( Exception ex ) {
      throw new PasswordServiceException( ex.getMessage() );
    }
  }

  private void readKeyFile() throws Exception {
    try ( FileReader reader = new FileReader( getSecurityDataFile() ) ) {
      Properties prop = new Properties();
      prop.load( reader );

      key = new EncryptionKey();
      key.password = (String) prop.get( "password" );
      key.saltBytes = Base64.getDecoder().decode( (String) prop.get( "salt" ) );
      key.ivBytes = Base64.getDecoder().decode( (String) prop.get( "iv" ) );

      if ( key.password == null || key.saltBytes == null || key.ivBytes == null ) {
        throw new PasswordServiceException( "Could not find encryption key" );
      }
    }
  }

  private String _encrypt( EncryptionKey key, String plainText ) throws Exception {
    SecretKeySpec secret = createSecret( key );
    Cipher cipher = Cipher.getInstance( TRANSFORMATION );
    cipher.init( Cipher.ENCRYPT_MODE, secret );

    AlgorithmParameters params = cipher.getParameters();
    key.ivBytes = params.getParameterSpec( IvParameterSpec.class ).getIV();

    byte[] encryptedTextBytes = cipher.doFinal( plainText.getBytes( StandardCharsets.UTF_8 ) );
    return Base64.getEncoder().encodeToString( encryptedTextBytes );
  }

  private String _decrypt( EncryptionKey key, String encrypted ) throws Exception {
    SecretKeySpec secret = createSecret( key );
    Cipher cipher = Cipher.getInstance( TRANSFORMATION );
    cipher.init( Cipher.DECRYPT_MODE, secret, new IvParameterSpec( key.ivBytes ) );

    byte[] decryptedTextBytes = cipher.doFinal( Base64.getDecoder().decode( encrypted ) );
    return new String( decryptedTextBytes, StandardCharsets.UTF_8 );
  }
}

class EncryptionKey {
  String password;
  byte[] saltBytes, ivBytes;

  public String toString() {
    return password + "|" +
      Base64.getEncoder().encodeToString( saltBytes ) + "|" +
      Base64.getEncoder().encodeToString( ivBytes );
  }
}