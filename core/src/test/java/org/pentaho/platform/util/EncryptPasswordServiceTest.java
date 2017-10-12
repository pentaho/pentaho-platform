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
 * Copyright 2017 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Base64;
import java.util.Properties;

import static org.junit.Assert.*;


public class EncryptPasswordServiceTest {
    String userHome;

    @Before
    public void setUp() {
        userHome = System.getProperty("user.home");
        System.setProperty("user.home", ".");

        System.out.println("clean the encryption key file");
        cleanKeyFile();
    }

    @Test
    public void testEncryptAndDecrypt() {
        System.out.println("testEncryptDecrypt");

        String plainText = "This is plain text";
        try {
            EncryptPasswordService service = new EncryptPasswordService();

            System.out.println("clear text: " + plainText);
            String encrypted = service.encrypt(plainText);
            System.out.println("encrypted : " + encrypted);
            String decrypted = service.decrypt(encrypted);
            System.out.println("decrypted : " + decrypted);

            Assert.assertEquals(plainText, decrypted);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testSaveAndLoadKey() {
        System.out.println("testSaveLoadKey");

        String plainText = "This is plain text";
        try {
            System.out.println("clear text: " + plainText);

            EncryptPasswordService encrypt = new EncryptPasswordService();
            String encrypted = encrypt.encrypt(plainText);
            System.out.println("encrypted : " + encrypted);

            EncryptPasswordService decrypt = new EncryptPasswordService();
            String decrypted = decrypt.decrypt(encrypted);
            System.out.println("decrypted : " + decrypted);

            Assert.assertEquals(plainText, decrypted);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
        //clean the encryption key file
        System.out.println("clean the encryption key file");
        cleanKeyFile();

        System.setProperty("user.home", userHome);
    }

    private void cleanKeyFile() {
        File f = new File(System.getProperty("user.home"), EncryptPasswordService.SECURITY_DATA_FILE);
        if (f.exists()) {
            f.delete();
        }
    }
}