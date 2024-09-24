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
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import junit.framework.TestCase;
import org.pentaho.platform.api.util.PasswordServiceException;

import javax.mail.PasswordAuthentication;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncryptedPasswordAuthenticatorTest extends TestCase {

  public void testGetPasswordAuthentication() {
    // SETUP
    String testUserId = "userId1";
    String clearTextPassword = "MyPassword1234";
    String prefix = EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX;
    String encryptedPassword = prefix + base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( clearTextPassword, result.getPassword() );
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_NoTrimPassword() {
    // SETUP
    String testUserId = "userId8";
    String clearTextPassword = "    " + "HorriblePassword2468" + "       "; // adding whitespace
    String prefix = EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX;
    String encryptedPassword = prefix + base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( clearTextPassword, result.getPassword() );
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_noPrefix() {
    // SETUP
    String testUserId = "userId2";
    String clearTextPassword = "Another_Password!7581";
    String encryptedPassword = base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( clearTextPassword, result.getPassword() ); // if no prefix, then should decrypt the input password
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_oneOffPrefix() {
    // SETUP
    String testUserId = "userId4";
    String clearTextPassword = "WHO_KNOWS_ABOUT_THIS_ONE_OFF_PREFIX";
    // testing the code doesn't automatically assume 3 digits + ":" is the correct prefix.
    String oneOffPrefix = incrementChar( EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX );
    String encryptedPassword = oneOffPrefix + base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( "sanity check to verify prefixes aren't the same", false,
            oneOffPrefix.equals( EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX  ) );

    // checking that prefix wasn't removed
    assertEquals( false, clearTextPassword.equals( result.getPassword() ) );
    assertEquals( false, encryptedPassword.equals( result.getPassword() ) );
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_longPrefix() {
    // SETUP
    String testUserId = "userId5";
    String clearTextPassword = "WHO_KNOWS_ABOUT_THIS_LONG_PREFIX";
    // testing the code doesn't automatically assume ":" is the end of prefix
    String longPrefix = "LONGPREFIX:";
    String encryptedPassword = longPrefix + base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( "sanity check to ensure lengths aren't the same", false,
            longPrefix.length() == EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX.length() );

    // checking that prefix wasn't removed
    assertEquals( false, clearTextPassword.equals( result.getPassword() ) );
    assertEquals( false, encryptedPassword.equals( result.getPassword() ) );
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_paddingBeforePrefix() {
    // SETUP
    String testUserId = "userId6";
    String clearTextPassword = "WHO_KNOWS_ABOUT_THIS_PADDING_BEFORE_PREFIX";
    // testing the code doesn't automatically assume ":" is the end of prefix
    String paddingBeforePrefix = "  " + EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX;
    String encryptedPassword = paddingBeforePrefix + base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( "sanity check to verify prefixes aren't the same", false,
            paddingBeforePrefix.equals( EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX  ) );

    // checking that prefix wasn't removed
    assertEquals( false, clearTextPassword.equals( result.getPassword() ) );
    assertEquals( false, encryptedPassword.equals( result.getPassword() ) );
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_paddingAfterPrefix() {
    // SETUP
    String testUserId = "userId7";
    String clearTextPassword = "WHO_KNOWS_ABOUT_THIS_PADDING_AFTER_PREFIX";
    // testing the code doesn't automatically assume ":" is the end of prefix
    String paddingAfterPrefix = "  " + EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX;
    String encryptedPassword = paddingAfterPrefix + base64Encode( clearTextPassword );

    EncryptedPasswordAuthenticator testInstance = new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( "sanity check to verify prefixes aren't the same", false,
            paddingAfterPrefix.equals( EncryptedPasswordAuthenticator.ENCRYPTED_PREFIX  ) );

    // checking that prefix wasn't removed
    assertEquals( false, clearTextPassword.equals( result.getPassword() ) );
    assertEquals( false, encryptedPassword.equals( result.getPassword() ) );
    assertEquals( testUserId, result.getUserName() );
  }

  public void testGetPasswordAuthentication_exceptionThrown() throws Exception {
    // SETUP
    String testUserId = "userId3";
    String clearTextPassword = "Should_Never_See_On_The_Other_Side";
    String encryptedPassword = base64Encode( clearTextPassword );

    Base64PasswordService mockBase64PasswordService = mock( Base64PasswordService.class );
    when( mockBase64PasswordService.decrypt( anyString() ) ).thenThrow( PasswordServiceException.class );

    EncryptedPasswordAuthenticator testInstance =
            new EncryptedPasswordAuthenticator( testUserId,  encryptedPassword, mockBase64PasswordService );

    // EXECUTE
    PasswordAuthentication result = testInstance.getPasswordAuthentication();

    // VERIFY
    assertEquals( encryptedPassword, result.getPassword() ); // when decryption fails, just return encrypted password
    assertEquals( testUserId, result.getUserName() );
  }


  public String base64Encode( String input ) {
    return Base64.getEncoder().encodeToString( input.getBytes() );
  }

  public String incrementChar( String prefix ) {
    char[] charPrefix = prefix.toCharArray();
    charPrefix[0] = ++charPrefix[0];
    return new String( charPrefix );
  }
}
