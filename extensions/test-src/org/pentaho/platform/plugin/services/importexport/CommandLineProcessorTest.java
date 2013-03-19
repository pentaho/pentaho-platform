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
 * @author dkincade
 */
package org.pentaho.platform.plugin.services.importexport;

import junit.framework.TestCase;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CommandLineProcessorTest extends TestCase {
  private static String VALID_URL_OPTION = "--url=http://localhost:8080/pentaho";
  private static String FILE_SYSTEM_SOURCE_OPTION = "--source=file-system";
  private static String VALID_IMPORT_COMMAND_LINE = "--import --type=files --source=file-system --username=admin " +
      "--password=password --charset=UTF-8 --path=/public " +
      "--file-path=/home/dkincade/pentaho/platform/trunk/biserver-ee/pentaho-solutions " + VALID_URL_OPTION;

  private static String VALID_LEGACY_DB_CHARSET_OPTION = "--legacy-db-charset=ISO-8859-1";
  private static String VALID_IMPORT_LEGACY_COMMAND_LINE =
      "--import --url=http://localhost:8080/pentaho --username=admin --password=password " +
          "--source=legacy-db --charset=UTF-8 --path=/public --legacy-db-driver=com.mysql.jdbc.Driver " +
          "--legacy-db-url=jdbc:mysql://localhost/hibernate --legacy-db-username=hibuser " +
          "--legacy-db-password=password " + VALID_LEGACY_DB_CHARSET_OPTION;

  private static final String[] toStringArray(final String s) {
    return StringUtils.split(s, ' ');
  }

  private static final String[] toStringArray(final String s1, final String s2) {
    return StringUtils.split(s1 + " " + s2, ' ');
  }

  public void testInvalidCommandLineParameters() throws Exception {
    CommandLineProcessor.main(new String[]{});
    assertEquals(ParseException.class, CommandLineProcessor.getException().getClass());

    CommandLineProcessor.main(toStringArray(VALID_IMPORT_COMMAND_LINE, "--export"));
    assertEquals(ParseException.class, CommandLineProcessor.getException().getClass());

    CommandLineProcessor.main(toStringArray("--help"));
    assertNull(CommandLineProcessor.getException());

    CommandLineProcessor.main(toStringArray(VALID_IMPORT_COMMAND_LINE.replace(VALID_URL_OPTION, "")));
    assertEquals(ParseException.class, CommandLineProcessor.getException().getClass());

    CommandLineProcessor.main(toStringArray(VALID_IMPORT_LEGACY_COMMAND_LINE.replace(VALID_LEGACY_DB_CHARSET_OPTION, "")));
    assertEquals(ParseException.class, CommandLineProcessor.getException().getClass());
  }

  public void testGetProperty() throws Exception {

  }

  public void testGetOptionValue() throws Exception {

  }

  public void testGetImportProcessor() throws Exception {

  }

  public void testGetImportSource() throws Exception {

  }

  public void testAddImportHandlers() throws Exception {

  }
}
