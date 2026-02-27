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


package org.pentaho.platform.engine.security;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.util.UUIDUtil;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    int threadCount = 30;
    final CipherEncryptionService service = new CipherEncryptionService();
    service.afterPropertiesSet();
    ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>( threadCount );
    ThreadPoolExecutor executor = new ThreadPoolExecutor( 10, 50, 1, TimeUnit.SECONDS, queue );

    for ( int i = 0; i < threadCount; i++ ) {
      executor.execute( new Runnable() {
        @Override
        public void run() {
          try {
            String s = null;
            String enc = null;
            String dec = null;
            for ( int i = 0; i < 50; i++ ) {
              s = UUIDUtil.getUUIDAsString();
              enc = service.encrypt( s );
              dec = service.decrypt( enc );
              Assert.assertEquals( s, dec );
            }
          } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( ex.toString() );
          }
        }
      } );
    }
    executor.shutdown();
    boolean isTerminated = executor.awaitTermination( 200, TimeUnit.SECONDS );
    if ( !isTerminated ) {
      Assert.fail( "It took too long to run the threading test." );
    }

  }
}
