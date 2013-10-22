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

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.util.UUIDUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCipherEncryptionService {

  @Test
  public void testCipherDefaultValues() {
    CipherEncryptionService service = new CipherEncryptionService();
    // Test defaults before assuming encryption will be the same
    Assert.assertEquals( "PBEWithMD5AndDES", service.getAlgorithm() );
    Assert.assertEquals( "_CyPh3r_", service.getSalt() );
    Assert.assertEquals( 19, service.getIterations() );
    Assert.assertEquals( "P3ntah0C1ph3r", service.getEncryptionKey() );
  }

  @Test
  public void testCipherEncryptionWithDefaults() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.afterPropertiesSet();
    // Everything should be set up now for encryption testing
    String stringToEncrypt = "String To Encrypt";
    String encryptedStringExpected = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String encryptedStringActual = service.encrypt( stringToEncrypt );
    Assert.assertEquals( encryptedStringExpected, encryptedStringActual );
  }

  @Test
  public void testCipherDecryptionWithDefaults() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.afterPropertiesSet();
    // Everything should be set up now for encryption testing
    String encryptedString = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String decryptedStringExpected = "String To Encrypt";
    String decryptedStringActual = service.decrypt( encryptedString );
    Assert.assertEquals( decryptedStringExpected, decryptedStringActual );
  }

  @Test
  public void testSaltEffectOnEncryption() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.setSalt( "DiffSalt" ); // Still 8 Characters/Bytes
    service.afterPropertiesSet();
    // Everything should be set up now for encryption testing
    String encryptedStringShouldNotBe = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String stringToEncrypt = "String To Encrypt";
    String encryptedStringActual = service.encrypt( stringToEncrypt );
    Assert.assertNotSame( encryptedStringShouldNotBe, encryptedStringActual );
  }

  @Test
  public void testSaltTooLong() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.setSalt( "This Salt Is Too Long And Will Be Truncated" );
    service.afterPropertiesSet();
    Assert.assertEquals( 8, service.getSalt().length() );
  }

  @Test
  public void testSaltTooShort() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.setSalt( "short" );
    service.afterPropertiesSet();
    Assert.assertEquals( 8, service.getSalt().length() );
  }

  @Test
  public void testIterationsEffectOnEncryption() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.setIterations( 25 );
    service.afterPropertiesSet();
    // Everything should be set up now for encryption testing
    String encryptedStringShouldNotBe = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String stringToEncrypt = "String To Encrypt";
    String encryptedStringActual = service.encrypt( stringToEncrypt );
    Assert.assertNotSame( encryptedStringShouldNotBe, encryptedStringActual );
  }

  @Test
  public void testThreadSafetyOfCipherService() throws Exception {
    CipherEncryptionService service = new CipherEncryptionService();
    service.afterPropertiesSet();
    List<TestRunnable> list = Collections.synchronizedList( new ArrayList<TestRunnable>() );
    for ( int i = 0; i < 30; i++ ) {
      TestRunnable runnable = new TestRunnable( service, list ) {
        @Override
        public void run() {
          try {
            String s = null;
            String enc = null;
            String dec = null;
            for ( int i = 0; i < 50; i++ ) {
              s = UUIDUtil.getUUIDAsString();
              enc = svc.encrypt( s );
              dec = svc.decrypt( enc );
              Assert.assertEquals( s, dec );
            }
          } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( ex.toString() );
          } finally {
            notifyList.remove( 0 );
          }
        }
      };
      list.add( runnable );
    }
    for ( int i = 0; i < list.size(); i++ ) {
      Thread th = new Thread( list.get( i ) );
      th.start();
    }
    int maxTimes = 100; // Try
    int i = 0;
    while ( list.size() > 0 ) {
      Thread.sleep( 2000 ); // Sleep for 2 seconds
      i++;
      if ( i > maxTimes ) {
        Assert.fail( "It took too long to run the threading test." );
        break;
      }
    }

  }

  abstract class TestRunnable implements Runnable {
    CipherEncryptionService svc;
    List<TestRunnable> notifyList;

    public TestRunnable( CipherEncryptionService value, List<TestRunnable> removeFrom ) {
      this.svc = value;
      this.notifyList = removeFrom;
    }
  }
}
