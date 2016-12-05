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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Test class for the {@link RepositoryFilenameUtils} class
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class RepositoryFilenameUtilsTest extends TestCase {
  // -----------------------------------------------------------------------
  public void testNormalize() throws Exception {
    assertEquals( null, RepositoryFilenameUtils.normalize( null ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "1:\\a\\b\\c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "\\\\\\a\\b\\c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "\\\\a" ) );

    assertEquals( "a/b/c.txt", RepositoryFilenameUtils.normalize( "a\\b/c.txt" ) );
    assertEquals( "/a/b/c.txt", RepositoryFilenameUtils.normalize( "\\a\\b/c.txt" ) );
    assertEquals( "C:/a/b/c.txt", RepositoryFilenameUtils.normalize( "C:\\a\\b/c.txt" ) );
    assertEquals( "//server/a/b/c.txt", RepositoryFilenameUtils.normalize( "\\\\server\\a\\b/c.txt" ) );
    assertEquals( "~/a/b/c.txt", RepositoryFilenameUtils.normalize( "~\\a\\b/c.txt" ) );
    assertEquals( "~user/a/b/c.txt", RepositoryFilenameUtils.normalize( "~user\\a\\b/c.txt" ) );

    assertEquals( "a/c", RepositoryFilenameUtils.normalize( "a/b/../c" ) );
    assertEquals( "c", RepositoryFilenameUtils.normalize( "a/b/../../c" ) );
    assertEquals( "c/", RepositoryFilenameUtils.normalize( "a/b/../../c/" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "a/b/../../../c" ) );
    assertEquals( "a/", RepositoryFilenameUtils.normalize( "a/b/.." ) );
    assertEquals( "a/", RepositoryFilenameUtils.normalize( "a/b/../" ) );
    assertEquals( "", RepositoryFilenameUtils.normalize( "a/b/../.." ) );
    assertEquals( "", RepositoryFilenameUtils.normalize( "a/b/../../" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "a/b/../../.." ) );
    assertEquals( "a/d", RepositoryFilenameUtils.normalize( "a/b/../c/../d" ) );
    assertEquals( "a/d/", RepositoryFilenameUtils.normalize( "a/b/../c/../d/" ) );
    assertEquals( "a/b/d", RepositoryFilenameUtils.normalize( "a/b//d" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.normalize( "a/b/././." ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.normalize( "a/b/./././" ) );
    assertEquals( "a/", RepositoryFilenameUtils.normalize( "./a/" ) );
    assertEquals( "a", RepositoryFilenameUtils.normalize( "./a" ) );
    assertEquals( "", RepositoryFilenameUtils.normalize( "./" ) );
    assertEquals( "", RepositoryFilenameUtils.normalize( "." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( ".." ) );
    assertEquals( "", RepositoryFilenameUtils.normalize( "" ) );

    assertEquals( "/a", RepositoryFilenameUtils.normalize( "/a" ) );
    assertEquals( "/a/", RepositoryFilenameUtils.normalize( "/a/" ) );
    assertEquals( "/a/c", RepositoryFilenameUtils.normalize( "/a/b/../c" ) );
    assertEquals( "/c", RepositoryFilenameUtils.normalize( "/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "/a/b/../../../c" ) );
    assertEquals( "/a/", RepositoryFilenameUtils.normalize( "/a/b/.." ) );
    assertEquals( "/", RepositoryFilenameUtils.normalize( "/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "/a/b/../../.." ) );
    assertEquals( "/a/d", RepositoryFilenameUtils.normalize( "/a/b/../c/../d" ) );
    assertEquals( "/a/b/d", RepositoryFilenameUtils.normalize( "/a/b//d" ) );
    assertEquals( "/a/b/", RepositoryFilenameUtils.normalize( "/a/b/././." ) );
    assertEquals( "/a", RepositoryFilenameUtils.normalize( "/./a" ) );
    assertEquals( "/", RepositoryFilenameUtils.normalize( "/./" ) );
    assertEquals( "/", RepositoryFilenameUtils.normalize( "/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "/.." ) );
    assertEquals( "/", RepositoryFilenameUtils.normalize( "/" ) );

    assertEquals( "~/a", RepositoryFilenameUtils.normalize( "~/a" ) );
    assertEquals( "~/a/", RepositoryFilenameUtils.normalize( "~/a/" ) );
    assertEquals( "~/a/c", RepositoryFilenameUtils.normalize( "~/a/b/../c" ) );
    assertEquals( "~/c", RepositoryFilenameUtils.normalize( "~/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~/a/b/../../../c" ) );
    assertEquals( "~/a/", RepositoryFilenameUtils.normalize( "~/a/b/.." ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalize( "~/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~/a/b/../../.." ) );
    assertEquals( "~/a/d", RepositoryFilenameUtils.normalize( "~/a/b/../c/../d" ) );
    assertEquals( "~/a/b/d", RepositoryFilenameUtils.normalize( "~/a/b//d" ) );
    assertEquals( "~/a/b/", RepositoryFilenameUtils.normalize( "~/a/b/././." ) );
    assertEquals( "~/a", RepositoryFilenameUtils.normalize( "~/./a" ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalize( "~/./" ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalize( "~/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~/.." ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalize( "~/" ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalize( "~" ) );

    assertEquals( "~user/a", RepositoryFilenameUtils.normalize( "~user/a" ) );
    assertEquals( "~user/a/", RepositoryFilenameUtils.normalize( "~user/a/" ) );
    assertEquals( "~user/a/c", RepositoryFilenameUtils.normalize( "~user/a/b/../c" ) );
    assertEquals( "~user/c", RepositoryFilenameUtils.normalize( "~user/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~user/a/b/../../../c" ) );
    assertEquals( "~user/a/", RepositoryFilenameUtils.normalize( "~user/a/b/.." ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalize( "~user/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~user/a/b/../../.." ) );
    assertEquals( "~user/a/d", RepositoryFilenameUtils.normalize( "~user/a/b/../c/../d" ) );
    assertEquals( "~user/a/b/d", RepositoryFilenameUtils.normalize( "~user/a/b//d" ) );
    assertEquals( "~user/a/b/", RepositoryFilenameUtils.normalize( "~user/a/b/././." ) );
    assertEquals( "~user/a", RepositoryFilenameUtils.normalize( "~user/./a" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalize( "~user/./" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalize( "~user/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~user/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "~user/.." ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalize( "~user/" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalize( "~user" ) );

    assertEquals( "C:/a", RepositoryFilenameUtils.normalize( "C:/a" ) );
    assertEquals( "C:/a/", RepositoryFilenameUtils.normalize( "C:/a/" ) );
    assertEquals( "C:/a/c", RepositoryFilenameUtils.normalize( "C:/a/b/../c" ) );
    assertEquals( "C:/c", RepositoryFilenameUtils.normalize( "C:/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:/a/b/../../../c" ) );
    assertEquals( "C:/a/", RepositoryFilenameUtils.normalize( "C:/a/b/.." ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalize( "C:/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:/a/b/../../.." ) );
    assertEquals( "C:/a/d", RepositoryFilenameUtils.normalize( "C:/a/b/../c/../d" ) );
    assertEquals( "C:/a/b/d", RepositoryFilenameUtils.normalize( "C:/a/b//d" ) );
    assertEquals( "C:/a/b/", RepositoryFilenameUtils.normalize( "C:/a/b/././." ) );
    assertEquals( "C:/a", RepositoryFilenameUtils.normalize( "C:/./a" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalize( "C:/./" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalize( "C:/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:/.." ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalize( "C:/" ) );

    assertEquals( "C:a", RepositoryFilenameUtils.normalize( "C:a" ) );
    assertEquals( "C:a/", RepositoryFilenameUtils.normalize( "C:a/" ) );
    assertEquals( "C:a/c", RepositoryFilenameUtils.normalize( "C:a/b/../c" ) );
    assertEquals( "C:c", RepositoryFilenameUtils.normalize( "C:a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:a/b/../../../c" ) );
    assertEquals( "C:a/", RepositoryFilenameUtils.normalize( "C:a/b/.." ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalize( "C:a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:a/b/../../.." ) );
    assertEquals( "C:a/d", RepositoryFilenameUtils.normalize( "C:a/b/../c/../d" ) );
    assertEquals( "C:a/b/d", RepositoryFilenameUtils.normalize( "C:a/b//d" ) );
    assertEquals( "C:a/b/", RepositoryFilenameUtils.normalize( "C:a/b/././." ) );
    assertEquals( "C:a", RepositoryFilenameUtils.normalize( "C:./a" ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalize( "C:./" ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalize( "C:." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "C:.." ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalize( "C:" ) );

    assertEquals( "//server/a", RepositoryFilenameUtils.normalize( "//server/a" ) );
    assertEquals( "//server/a/", RepositoryFilenameUtils.normalize( "//server/a/" ) );
    assertEquals( "//server/a/c", RepositoryFilenameUtils.normalize( "//server/a/b/../c" ) );
    assertEquals( "//server/c", RepositoryFilenameUtils.normalize( "//server/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "//server/a/b/../../../c" ) );
    assertEquals( "//server/a/", RepositoryFilenameUtils.normalize( "//server/a/b/.." ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalize( "//server/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "//server/a/b/../../.." ) );
    assertEquals( "//server/a/d", RepositoryFilenameUtils.normalize( "//server/a/b/../c/../d" ) );
    assertEquals( "//server/a/b/d", RepositoryFilenameUtils.normalize( "//server/a/b//d" ) );
    assertEquals( "//server/a/b/", RepositoryFilenameUtils.normalize( "//server/a/b/././." ) );
    assertEquals( "//server/a", RepositoryFilenameUtils.normalize( "//server/./a" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalize( "//server/./" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalize( "//server/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "//server/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalize( "//server/.." ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalize( "//server/" ) );
  }

  // -----------------------------------------------------------------------
  public void testNormalizeNoEndSeparator() throws Exception {
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( null ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "1:\\a\\b\\c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "\\\\\\a\\b\\c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "\\\\a" ) );

    assertEquals( "a/b/c.txt", RepositoryFilenameUtils.normalizeNoEndSeparator( "a\\b/c.txt" ) );
    assertEquals( "/a/b/c.txt", RepositoryFilenameUtils.normalizeNoEndSeparator( "\\a\\b/c.txt" ) );
    assertEquals( "C:/a/b/c.txt", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:\\a\\b/c.txt" ) );
    assertEquals( "//server/a/b/c.txt", RepositoryFilenameUtils.normalizeNoEndSeparator( "\\\\server\\a\\b/c.txt" ) );
    assertEquals( "~/a/b/c.txt", RepositoryFilenameUtils.normalizeNoEndSeparator( "~\\a\\b/c.txt" ) );
    assertEquals( "~user/a/b/c.txt", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user\\a\\b/c.txt" ) );

    assertEquals( "a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../c" ) );
    assertEquals( "c", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../../c" ) );
    assertEquals( "c", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../../c/" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../../../c" ) );
    assertEquals( "a", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/.." ) );
    assertEquals( "a", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../" ) );
    assertEquals( "", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../.." ) );
    assertEquals( "", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../../" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../../.." ) );
    assertEquals( "a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../c/../d" ) );
    assertEquals( "a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/../c/../d/" ) );
    assertEquals( "a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b//d" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/././." ) );
    assertEquals( "a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "a/b/./././" ) );
    assertEquals( "a", RepositoryFilenameUtils.normalizeNoEndSeparator( "./a/" ) );
    assertEquals( "a", RepositoryFilenameUtils.normalizeNoEndSeparator( "./a" ) );
    assertEquals( "", RepositoryFilenameUtils.normalizeNoEndSeparator( "./" ) );
    assertEquals( "", RepositoryFilenameUtils.normalizeNoEndSeparator( "." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( ".." ) );
    assertEquals( "", RepositoryFilenameUtils.normalizeNoEndSeparator( "" ) );

    assertEquals( "/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a" ) );
    assertEquals( "/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/" ) );
    assertEquals( "/a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/../c" ) );
    assertEquals( "/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/../../../c" ) );
    assertEquals( "/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/.." ) );
    assertEquals( "/", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/../../.." ) );
    assertEquals( "/a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/../c/../d" ) );
    assertEquals( "/a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b//d" ) );
    assertEquals( "/a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "/a/b/././." ) );
    assertEquals( "/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "/./a" ) );
    assertEquals( "/", RepositoryFilenameUtils.normalizeNoEndSeparator( "/./" ) );
    assertEquals( "/", RepositoryFilenameUtils.normalizeNoEndSeparator( "/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "/.." ) );
    assertEquals( "/", RepositoryFilenameUtils.normalizeNoEndSeparator( "/" ) );

    assertEquals( "~/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a" ) );
    assertEquals( "~/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/" ) );
    assertEquals( "~/a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/../c" ) );
    assertEquals( "~/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/../../../c" ) );
    assertEquals( "~/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/.." ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/../../.." ) );
    assertEquals( "~/a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/../c/../d" ) );
    assertEquals( "~/a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b//d" ) );
    assertEquals( "~/a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/a/b/././." ) );
    assertEquals( "~/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/./a" ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/./" ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~/.." ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~/" ) );
    assertEquals( "~/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~" ) );

    assertEquals( "~user/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a" ) );
    assertEquals( "~user/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/" ) );
    assertEquals( "~user/a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/../c" ) );
    assertEquals( "~user/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/../../../c" ) );
    assertEquals( "~user/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/.." ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/../../.." ) );
    assertEquals( "~user/a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/../c/../d" ) );
    assertEquals( "~user/a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b//d" ) );
    assertEquals( "~user/a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/a/b/././." ) );
    assertEquals( "~user/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/./a" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/./" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/.." ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user/" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.normalizeNoEndSeparator( "~user" ) );

    assertEquals( "C:/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a" ) );
    assertEquals( "C:/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/" ) );
    assertEquals( "C:/a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/../c" ) );
    assertEquals( "C:/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/../../../c" ) );
    assertEquals( "C:/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/.." ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/../../.." ) );
    assertEquals( "C:/a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/../c/../d" ) );
    assertEquals( "C:/a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b//d" ) );
    assertEquals( "C:/a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/a/b/././." ) );
    assertEquals( "C:/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/./a" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/./" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/.." ) );
    assertEquals( "C:/", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:/" ) );

    assertEquals( "C:a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a" ) );
    assertEquals( "C:a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/" ) );
    assertEquals( "C:a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/../c" ) );
    assertEquals( "C:c", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/../../../c" ) );
    assertEquals( "C:a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/.." ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/../../.." ) );
    assertEquals( "C:a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/../c/../d" ) );
    assertEquals( "C:a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b//d" ) );
    assertEquals( "C:a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:a/b/././." ) );
    assertEquals( "C:a", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:./a" ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:./" ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "C:.." ) );
    assertEquals( "C:", RepositoryFilenameUtils.normalizeNoEndSeparator( "C:" ) );

    assertEquals( "//server/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a" ) );
    assertEquals( "//server/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/" ) );
    assertEquals( "//server/a/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/../c" ) );
    assertEquals( "//server/c", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/../../c" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/../../../c" ) );
    assertEquals( "//server/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/.." ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/../.." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/../../.." ) );
    assertEquals( "//server/a/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/../c/../d" ) );
    assertEquals( "//server/a/b/d", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b//d" ) );
    assertEquals( "//server/a/b", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/a/b/././." ) );
    assertEquals( "//server/a", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/./a" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/./" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/." ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/../a" ) );
    assertEquals( null, RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/.." ) );
    assertEquals( "//server/", RepositoryFilenameUtils.normalizeNoEndSeparator( "//server/" ) );
  }

  // -----------------------------------------------------------------------
  public void testConcat() {
    assertEquals( null, RepositoryFilenameUtils.concat( "", null ) );
    assertEquals( null, RepositoryFilenameUtils.concat( null, null ) );
    assertEquals( null, RepositoryFilenameUtils.concat( null, "" ) );
    assertEquals( null, RepositoryFilenameUtils.concat( null, "a" ) );
    assertEquals( "/a", RepositoryFilenameUtils.concat( null, "/a" ) );

    assertEquals( null, RepositoryFilenameUtils.concat( "", ":" ) ); // invalid prefix
    assertEquals( null, RepositoryFilenameUtils.concat( ":", "" ) ); // invalid prefix

    assertEquals( "f/", RepositoryFilenameUtils.concat( "", "f/" ) );
    assertEquals( "f", RepositoryFilenameUtils.concat( "", "f" ) );
    assertEquals( "a/f/", RepositoryFilenameUtils.concat( "a/", "f/" ) );
    assertEquals( "a/f", RepositoryFilenameUtils.concat( "a", "f" ) );
    assertEquals( "a/b/f/", RepositoryFilenameUtils.concat( "a/b/", "f/" ) );
    assertEquals( "a/b/f", RepositoryFilenameUtils.concat( "a/b", "f" ) );

    assertEquals( "a/f/", RepositoryFilenameUtils.concat( "a/b/", "../f/" ) );
    assertEquals( "a/f", RepositoryFilenameUtils.concat( "a/b", "../f" ) );
    assertEquals( "a/c/g/", RepositoryFilenameUtils.concat( "a/b/../c/", "f/../g/" ) );
    assertEquals( "a/c/g", RepositoryFilenameUtils.concat( "a/b/../c", "f/../g" ) );

    assertEquals( "a/c.txt/f", RepositoryFilenameUtils.concat( "a/c.txt", "f" ) );

    assertEquals( "/f/", RepositoryFilenameUtils.concat( "", "/f/" ) );
    assertEquals( "/f", RepositoryFilenameUtils.concat( "", "/f" ) );
    assertEquals( "/f/", RepositoryFilenameUtils.concat( "a/", "/f/" ) );
    assertEquals( "/f", RepositoryFilenameUtils.concat( "a", "/f" ) );

    assertEquals( "/c/d", RepositoryFilenameUtils.concat( "a/b/", "/c/d" ) );
    assertEquals( "a/b/C:c/d", RepositoryFilenameUtils.concat( "a/b/", "C:c/d" ) );
    assertEquals( "a/b/C:/c/d", RepositoryFilenameUtils.concat( "a/b/", "C:/c/d" ) );
    assertEquals( "~/c/d", RepositoryFilenameUtils.concat( "a/b/", "~/c/d" ) );
    assertEquals( "~user/c/d", RepositoryFilenameUtils.concat( "a/b/", "~user/c/d" ) );
    assertEquals( "~/", RepositoryFilenameUtils.concat( "a/b/", "~" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.concat( "a/b/", "~user" ) );
  }

  // -----------------------------------------------------------------------
  public void testSeparatorsToUnix() {
    assertEquals( null, RepositoryFilenameUtils.separatorsToRepository( null ) );
    assertEquals( "/a/b/c", RepositoryFilenameUtils.separatorsToRepository( "/a/b/c" ) );
    assertEquals( "/a/b/c.txt", RepositoryFilenameUtils.separatorsToRepository( "/a/b/c.txt" ) );
    assertEquals( "/a/b/c", RepositoryFilenameUtils.separatorsToRepository( "/a/b\\c" ) );
    assertEquals( "/a/b/c", RepositoryFilenameUtils.separatorsToRepository( "\\a\\b\\c" ) );
    assertEquals( "D:/a/b/c", RepositoryFilenameUtils.separatorsToRepository( "D:\\a\\b\\c" ) );
  }

  // -----------------------------------------------------------------------
  public void testGetPrefixLength() {
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( null ) );
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( ":" ) );
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( "1:\\a\\b\\c.txt" ) );
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( "1:" ) );
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( "1:a" ) );
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( "\\\\\\a\\b\\c.txt" ) );
    assertEquals( -1, RepositoryFilenameUtils.getPrefixLength( "\\\\a" ) );

    assertEquals( 0, RepositoryFilenameUtils.getPrefixLength( "" ) );
    assertEquals( 1, RepositoryFilenameUtils.getPrefixLength( "\\" ) );
    assertEquals( 2, RepositoryFilenameUtils.getPrefixLength( "C:" ) );
    assertEquals( 3, RepositoryFilenameUtils.getPrefixLength( "C:\\" ) );
    assertEquals( 9, RepositoryFilenameUtils.getPrefixLength( "//server/" ) );
    assertEquals( 2, RepositoryFilenameUtils.getPrefixLength( "~" ) );
    assertEquals( 2, RepositoryFilenameUtils.getPrefixLength( "~/" ) );
    assertEquals( 6, RepositoryFilenameUtils.getPrefixLength( "~user" ) );
    assertEquals( 6, RepositoryFilenameUtils.getPrefixLength( "~user/" ) );

    assertEquals( 0, RepositoryFilenameUtils.getPrefixLength( "a\\b\\c.txt" ) );
    assertEquals( 1, RepositoryFilenameUtils.getPrefixLength( "\\a\\b\\c.txt" ) );
    assertEquals( 2, RepositoryFilenameUtils.getPrefixLength( "C:a\\b\\c.txt" ) );
    assertEquals( 3, RepositoryFilenameUtils.getPrefixLength( "C:\\a\\b\\c.txt" ) );
    assertEquals( 9, RepositoryFilenameUtils.getPrefixLength( "\\\\server\\a\\b\\c.txt" ) );

    assertEquals( 0, RepositoryFilenameUtils.getPrefixLength( "a/b/c.txt" ) );
    assertEquals( 1, RepositoryFilenameUtils.getPrefixLength( "/a/b/c.txt" ) );
    assertEquals( 3, RepositoryFilenameUtils.getPrefixLength( "C:/a/b/c.txt" ) );
    assertEquals( 9, RepositoryFilenameUtils.getPrefixLength( "//server/a/b/c.txt" ) );
    assertEquals( 2, RepositoryFilenameUtils.getPrefixLength( "~/a/b/c.txt" ) );
    assertEquals( 6, RepositoryFilenameUtils.getPrefixLength( "~user/a/b/c.txt" ) );

    assertEquals( 0, RepositoryFilenameUtils.getPrefixLength( "a\\b\\c.txt" ) );
    assertEquals( 1, RepositoryFilenameUtils.getPrefixLength( "\\a\\b\\c.txt" ) );
    assertEquals( 2, RepositoryFilenameUtils.getPrefixLength( "~\\a\\b\\c.txt" ) );
    assertEquals( 6, RepositoryFilenameUtils.getPrefixLength( "~user\\a\\b\\c.txt" ) );
  }

  public void testIndexOfLastSeparator() {
    assertEquals( -1, RepositoryFilenameUtils.indexOfLastSeparator( null ) );
    assertEquals( -1, RepositoryFilenameUtils.indexOfLastSeparator( "noseperator.inthispath" ) );
    assertEquals( 3, RepositoryFilenameUtils.indexOfLastSeparator( "a/b/c" ) );
    assertEquals( 3, RepositoryFilenameUtils.indexOfLastSeparator( "a\\b\\c" ) );
  }

  public void testIndexOfExtension() {
    assertEquals( -1, RepositoryFilenameUtils.indexOfExtension( null ) );
    assertEquals( -1, RepositoryFilenameUtils.indexOfExtension( "file" ) );
    assertEquals( 4, RepositoryFilenameUtils.indexOfExtension( "file.txt" ) );
    assertEquals( 13, RepositoryFilenameUtils.indexOfExtension( "a.txt/b.txt/c.txt" ) );
    assertEquals( -1, RepositoryFilenameUtils.indexOfExtension( "a/b/c" ) );
    assertEquals( -1, RepositoryFilenameUtils.indexOfExtension( "a\\b\\c" ) );
    assertEquals( -1, RepositoryFilenameUtils.indexOfExtension( "a/b.notextension/c" ) );
    assertEquals( -1, RepositoryFilenameUtils.indexOfExtension( "a\\b.notextension\\c" ) );
  }

  // -----------------------------------------------------------------------
  public void testGetPrefix() {
    assertEquals( null, RepositoryFilenameUtils.getPrefix( null ) );
    assertEquals( null, RepositoryFilenameUtils.getPrefix( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.getPrefix( "1:\\a\\b\\c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getPrefix( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.getPrefix( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.getPrefix( "\\\\\\a\\b\\c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getPrefix( "\\\\a" ) );

    assertEquals( "", RepositoryFilenameUtils.getPrefix( "" ) );
    assertEquals( "\\", RepositoryFilenameUtils.getPrefix( "\\" ) );
    assertEquals( "C:", RepositoryFilenameUtils.getPrefix( "C:" ) );
    assertEquals( "C:\\", RepositoryFilenameUtils.getPrefix( "C:\\" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.getPrefix( "//server/" ) );
    assertEquals( "~/", RepositoryFilenameUtils.getPrefix( "~" ) );
    assertEquals( "~/", RepositoryFilenameUtils.getPrefix( "~/" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.getPrefix( "~user" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.getPrefix( "~user/" ) );

    assertEquals( "", RepositoryFilenameUtils.getPrefix( "a\\b\\c.txt" ) );
    assertEquals( "\\", RepositoryFilenameUtils.getPrefix( "\\a\\b\\c.txt" ) );
    assertEquals( "C:\\", RepositoryFilenameUtils.getPrefix( "C:\\a\\b\\c.txt" ) );
    assertEquals( "\\\\server\\", RepositoryFilenameUtils.getPrefix( "\\\\server\\a\\b\\c.txt" ) );

    assertEquals( "", RepositoryFilenameUtils.getPrefix( "a/b/c.txt" ) );
    assertEquals( "/", RepositoryFilenameUtils.getPrefix( "/a/b/c.txt" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.getPrefix( "C:/a/b/c.txt" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.getPrefix( "//server/a/b/c.txt" ) );
    assertEquals( "~/", RepositoryFilenameUtils.getPrefix( "~/a/b/c.txt" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.getPrefix( "~user/a/b/c.txt" ) );

    assertEquals( "", RepositoryFilenameUtils.getPrefix( "a\\b\\c.txt" ) );
    assertEquals( "\\", RepositoryFilenameUtils.getPrefix( "\\a\\b\\c.txt" ) );
    assertEquals( "~\\", RepositoryFilenameUtils.getPrefix( "~\\a\\b\\c.txt" ) );
    assertEquals( "~user\\", RepositoryFilenameUtils.getPrefix( "~user\\a\\b\\c.txt" ) );
  }

  public void testGetPath() {
    assertEquals( null, RepositoryFilenameUtils.getPath( null ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "noseperator.inthispath" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "a.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "a/b/c" ) );
    assertEquals( "a/b/c/", RepositoryFilenameUtils.getPath( "a/b/c/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "/a.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "/a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "/a/b/c" ) );
    assertEquals( "a/b/c/", RepositoryFilenameUtils.getPath( "/a/b/c/" ) );
    assertEquals( "a\\b\\", RepositoryFilenameUtils.getPath( "a\\b\\c" ) );

    assertEquals( null, RepositoryFilenameUtils.getPath( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.getPath( "1:/a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getPath( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.getPath( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.getPath( "///a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getPath( "//a" ) );

    assertEquals( "", RepositoryFilenameUtils.getPath( "" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "C:" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "C:/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "//server/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "~" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "~/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "~user" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "~user/" ) );

    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "/a/b/c.txt" ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "C:a" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "C:a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "C:/a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "//server/a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "~/a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getPath( "~user/a/b/c.txt" ) );
  }

  public void testGetPathNoEndSeparator() {
    assertEquals( null, RepositoryFilenameUtils.getPath( null ) );
    assertEquals( "", RepositoryFilenameUtils.getPath( "noseperator.inthispath" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "a.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "a/b/c" ) );
    assertEquals( "a/b/c", RepositoryFilenameUtils.getPathNoEndSeparator( "a/b/c/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "/a.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "/a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "/a/b/c" ) );
    assertEquals( "a/b/c", RepositoryFilenameUtils.getPathNoEndSeparator( "/a/b/c/" ) );
    assertEquals( "a\\b", RepositoryFilenameUtils.getPathNoEndSeparator( "a\\b\\c" ) );

    assertEquals( null, RepositoryFilenameUtils.getPathNoEndSeparator( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.getPathNoEndSeparator( "1:/a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getPathNoEndSeparator( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.getPathNoEndSeparator( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.getPathNoEndSeparator( "///a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getPathNoEndSeparator( "//a" ) );

    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "C:" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "C:/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "//server/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "~" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "~/" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "~user" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "~user/" ) );

    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "/a/b/c.txt" ) );
    assertEquals( "", RepositoryFilenameUtils.getPathNoEndSeparator( "C:a" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "C:a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "C:/a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "//server/a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "~/a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getPathNoEndSeparator( "~user/a/b/c.txt" ) );
  }

  public void testGetFullPath() {
    assertEquals( null, RepositoryFilenameUtils.getFullPath( null ) );
    assertEquals( "", RepositoryFilenameUtils.getFullPath( "noseperator.inthispath" ) );
    assertEquals( "", RepositoryFilenameUtils.getFullPath( "a.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getFullPath( "a/b/c.txt" ) );
    assertEquals( "a/b/", RepositoryFilenameUtils.getFullPath( "a/b/c" ) );
    assertEquals( "a/b/c/", RepositoryFilenameUtils.getFullPath( "a/b/c/" ) );
    assertEquals( "/", RepositoryFilenameUtils.getFullPath( "/a.txt" ) );
    assertEquals( "/a/b/", RepositoryFilenameUtils.getFullPath( "/a/b/c.txt" ) );
    assertEquals( "/a/b/", RepositoryFilenameUtils.getFullPath( "/a/b/c" ) );
    assertEquals( "/a/b/c/", RepositoryFilenameUtils.getFullPath( "/a/b/c/" ) );
    assertEquals( "a\\b\\", RepositoryFilenameUtils.getFullPath( "a\\b\\c" ) );

    assertEquals( null, RepositoryFilenameUtils.getFullPath( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPath( "1:/a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPath( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPath( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPath( "///a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPath( "//a" ) );

    assertEquals( "", RepositoryFilenameUtils.getFullPath( "" ) );
    assertEquals( "C:", RepositoryFilenameUtils.getFullPath( "C:" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.getFullPath( "C:/" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.getFullPath( "//server/" ) );
    assertEquals( "~/", RepositoryFilenameUtils.getFullPath( "~" ) );
    assertEquals( "~/", RepositoryFilenameUtils.getFullPath( "~/" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.getFullPath( "~user" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.getFullPath( "~user/" ) );

    assertEquals( "a/b/", RepositoryFilenameUtils.getFullPath( "a/b/c.txt" ) );
    assertEquals( "/a/b/", RepositoryFilenameUtils.getFullPath( "/a/b/c.txt" ) );
    assertEquals( "C:", RepositoryFilenameUtils.getFullPath( "C:a" ) );
    assertEquals( "C:a/b/", RepositoryFilenameUtils.getFullPath( "C:a/b/c.txt" ) );
    assertEquals( "C:/a/b/", RepositoryFilenameUtils.getFullPath( "C:/a/b/c.txt" ) );
    assertEquals( "//server/a/b/", RepositoryFilenameUtils.getFullPath( "//server/a/b/c.txt" ) );
    assertEquals( "~/a/b/", RepositoryFilenameUtils.getFullPath( "~/a/b/c.txt" ) );
    assertEquals( "~user/a/b/", RepositoryFilenameUtils.getFullPath( "~user/a/b/c.txt" ) );
  }

  public void testGetFullPathNoEndSeparator() {
    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( null ) );
    assertEquals( "", RepositoryFilenameUtils.getFullPathNoEndSeparator( "noseperator.inthispath" ) );
    assertEquals( "", RepositoryFilenameUtils.getFullPath( "a.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "a/b/c.txt" ) );
    assertEquals( "a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "a/b/c" ) );
    assertEquals( "a/b/c", RepositoryFilenameUtils.getFullPathNoEndSeparator( "a/b/c/" ) );
    assertEquals( "/", RepositoryFilenameUtils.getFullPath( "/a.txt" ) );
    assertEquals( "/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "/a/b/c.txt" ) );
    assertEquals( "/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "/a/b/c" ) );
    assertEquals( "/a/b/c", RepositoryFilenameUtils.getFullPathNoEndSeparator( "/a/b/c/" ) );
    assertEquals( "a\\b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "a\\b\\c" ) );

    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( ":" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( "1:/a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( "1:" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( "1:a" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( "///a/b/c.txt" ) );
    assertEquals( null, RepositoryFilenameUtils.getFullPathNoEndSeparator( "//a" ) );

    assertEquals( "", RepositoryFilenameUtils.getFullPathNoEndSeparator( "" ) );
    assertEquals( "C:", RepositoryFilenameUtils.getFullPathNoEndSeparator( "C:" ) );
    assertEquals( "C:/", RepositoryFilenameUtils.getFullPathNoEndSeparator( "C:/" ) );
    assertEquals( "//server/", RepositoryFilenameUtils.getFullPathNoEndSeparator( "//server/" ) );
    assertEquals( "~", RepositoryFilenameUtils.getFullPathNoEndSeparator( "~" ) );
    assertEquals( "~/", RepositoryFilenameUtils.getFullPathNoEndSeparator( "~/" ) );
    assertEquals( "~user", RepositoryFilenameUtils.getFullPathNoEndSeparator( "~user" ) );
    assertEquals( "~user/", RepositoryFilenameUtils.getFullPathNoEndSeparator( "~user/" ) );

    assertEquals( "a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "a/b/c.txt" ) );
    assertEquals( "/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "/a/b/c.txt" ) );
    assertEquals( "C:", RepositoryFilenameUtils.getFullPathNoEndSeparator( "C:a" ) );
    assertEquals( "C:a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "C:a/b/c.txt" ) );
    assertEquals( "C:/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "C:/a/b/c.txt" ) );
    assertEquals( "//server/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "//server/a/b/c.txt" ) );
    assertEquals( "~/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "~/a/b/c.txt" ) );
    assertEquals( "~user/a/b", RepositoryFilenameUtils.getFullPathNoEndSeparator( "~user/a/b/c.txt" ) );
  }

  public void testGetName() {
    assertEquals( null, RepositoryFilenameUtils.getName( null ) );
    assertEquals( "noseperator.inthispath", RepositoryFilenameUtils.getName( "noseperator.inthispath" ) );
    assertEquals( "c.txt", RepositoryFilenameUtils.getName( "a/b/c.txt" ) );
    assertEquals( "c", RepositoryFilenameUtils.getName( "a/b/c" ) );
    assertEquals( "", RepositoryFilenameUtils.getName( "a/b/c/" ) );
    assertEquals( "c", RepositoryFilenameUtils.getName( "a\\b\\c" ) );
  }

  public void testGetBaseName() {
    assertEquals( null, RepositoryFilenameUtils.getBaseName( null ) );
    assertEquals( "noseperator", RepositoryFilenameUtils.getBaseName( "noseperator.inthispath" ) );
    assertEquals( "c", RepositoryFilenameUtils.getBaseName( "a/b/c.txt" ) );
    assertEquals( "c", RepositoryFilenameUtils.getBaseName( "a/b/c" ) );
    assertEquals( "", RepositoryFilenameUtils.getBaseName( "a/b/c/" ) );
    assertEquals( "c", RepositoryFilenameUtils.getBaseName( "a\\b\\c" ) );
    assertEquals( "file.txt", RepositoryFilenameUtils.getBaseName( "file.txt.bak" ) );
  }

  public void testGetExtension() {
    assertEquals( null, RepositoryFilenameUtils.getExtension( null ) );
    assertEquals( "ext", RepositoryFilenameUtils.getExtension( "file.ext" ) );
    assertEquals( "", RepositoryFilenameUtils.getExtension( "README" ) );
    assertEquals( "com", RepositoryFilenameUtils.getExtension( "domain.dot.com" ) );
    assertEquals( "jpeg", RepositoryFilenameUtils.getExtension( "image.jpeg" ) );
    assertEquals( "", RepositoryFilenameUtils.getExtension( "a.b/c" ) );
    assertEquals( "txt", RepositoryFilenameUtils.getExtension( "a.b/c.txt" ) );
    assertEquals( "", RepositoryFilenameUtils.getExtension( "a/b/c" ) );
    assertEquals( "", RepositoryFilenameUtils.getExtension( "a.b\\c" ) );
    assertEquals( "txt", RepositoryFilenameUtils.getExtension( "a.b\\c.txt" ) );
    assertEquals( "", RepositoryFilenameUtils.getExtension( "a\\b\\c" ) );
    assertEquals( "", RepositoryFilenameUtils.getExtension( "C:\\temp\\foo.bar\\README" ) );
    assertEquals( "ext", RepositoryFilenameUtils.getExtension( "../filename.ext" ) );
  }

  public void testRemoveExtension() {
    assertEquals( null, RepositoryFilenameUtils.removeExtension( null ) );
    assertEquals( "file", RepositoryFilenameUtils.removeExtension( "file.ext" ) );
    assertEquals( "README", RepositoryFilenameUtils.removeExtension( "README" ) );
    assertEquals( "domain.dot", RepositoryFilenameUtils.removeExtension( "domain.dot.com" ) );
    assertEquals( "image", RepositoryFilenameUtils.removeExtension( "image.jpeg" ) );
    assertEquals( "a.b/c", RepositoryFilenameUtils.removeExtension( "a.b/c" ) );
    assertEquals( "a.b/c", RepositoryFilenameUtils.removeExtension( "a.b/c.txt" ) );
    assertEquals( "a/b/c", RepositoryFilenameUtils.removeExtension( "a/b/c" ) );
    assertEquals( "a.b\\c", RepositoryFilenameUtils.removeExtension( "a.b\\c" ) );
    assertEquals( "a.b\\c", RepositoryFilenameUtils.removeExtension( "a.b\\c.txt" ) );
    assertEquals( "a\\b\\c", RepositoryFilenameUtils.removeExtension( "a\\b\\c" ) );
    assertEquals( "C:\\temp\\foo.bar\\README", RepositoryFilenameUtils.removeExtension( "C:\\temp\\foo.bar\\README" ) );
    assertEquals( "../filename", RepositoryFilenameUtils.removeExtension( "../filename.ext" ) );
  }

  // -----------------------------------------------------------------------
  public void testEquals() {
    assertEquals( true, RepositoryFilenameUtils.equals( null, null ) );
    assertEquals( false, RepositoryFilenameUtils.equals( null, "" ) );
    assertEquals( false, RepositoryFilenameUtils.equals( "", null ) );
    assertEquals( true, RepositoryFilenameUtils.equals( "", "" ) );
    assertEquals( true, RepositoryFilenameUtils.equals( "file.txt", "file.txt" ) );
    assertEquals( false, RepositoryFilenameUtils.equals( "file.txt", "FILE.TXT" ) );
    assertEquals( false, RepositoryFilenameUtils.equals( "a\\b\\file.txt", "a/b/file.txt" ) );
  }

  // -----------------------------------------------------------------------
  public void testEqualsNormalized() {
    assertEquals( true, RepositoryFilenameUtils.equalsNormalized( null, null ) );
    assertEquals( false, RepositoryFilenameUtils.equalsNormalized( null, "" ) );
    assertEquals( false, RepositoryFilenameUtils.equalsNormalized( "", null ) );
    assertEquals( true, RepositoryFilenameUtils.equalsNormalized( "", "" ) );
    assertEquals( true, RepositoryFilenameUtils.equalsNormalized( "file.txt", "file.txt" ) );
    assertEquals( false, RepositoryFilenameUtils.equalsNormalized( "file.txt", "FILE.TXT" ) );
    assertEquals( true, RepositoryFilenameUtils.equalsNormalized( "a\\b\\file.txt", "a/b/file.txt" ) );
    assertEquals( false, RepositoryFilenameUtils.equalsNormalized( "a/b/", "a/b" ) );
  }

  // -----------------------------------------------------------------------
  public void testIsExtension() {
    assertEquals( false, RepositoryFilenameUtils.isExtension( null, (String) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", (String) null ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file", (String) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", "" ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file", "" ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file.txt", "txt" ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", "rtf" ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", (String) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", "" ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a/b/file.txt", "txt" ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", "rtf" ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", (String) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", "" ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b/file.txt", "txt" ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", "rtf" ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", (String) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", "" ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", "txt" ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", "rtf" ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", (String) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", "" ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", "txt" ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", "rtf" ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", "TXT" ) );
  }

  public void testIsExtensionArray() {
    assertEquals( false, RepositoryFilenameUtils.isExtension( null, (String[]) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", (String[]) null ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file", (String[]) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", new String[0] ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file.txt", new String[] { "txt" } ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", new String[] { "rtf" } ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file", new String[] { "rtf", "" } ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file.txt", new String[] { "rtf", "txt" } ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", (String[]) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new String[0] ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new String[] { "txt" } ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new String[] { "rtf" } ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new String[] { "rtf", "txt" } ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", (String[]) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new String[0] ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new String[] { "txt" } ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new String[] { "rtf" } ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new String[] { "rtf", "txt" } ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", (String[]) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new String[0] ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new String[] { "txt" } ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new String[] { "rtf" } ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new String[] { "rtf", "txt" } ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", (String[]) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new String[0] ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new String[] { "txt" } ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new String[] { "rtf" } ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new String[] { "rtf", "txt" } ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new String[] { "TXT" } ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new String[] { "TXT", "RTF" } ) );
  }

  public void testIsExtensionCollection() {
    assertEquals( false, RepositoryFilenameUtils.isExtension( null, (Collection) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", (Collection) null ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file", (Collection) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", new ArrayList() ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file.txt", new ArrayList( Arrays
        .asList( new String[] { "txt" } ) ) ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf" } ) ) ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file", new ArrayList( Arrays.asList( new String[] {
      "rtf", "" } ) ) ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "file.txt", new ArrayList( Arrays.asList( new String[] {
      "rtf", "txt" } ) ) ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", (Collection) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new ArrayList() ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new ArrayList( Arrays
        .asList( new String[] { "txt" } ) ) ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf" } ) ) ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a/b/file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf", "txt" } ) ) ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", (Collection) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new ArrayList() ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new ArrayList( Arrays
        .asList( new String[] { "txt" } ) ) ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf" } ) ) ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b/file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf", "txt" } ) ) ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", (Collection) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new ArrayList() ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "txt" } ) ) ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf" } ) ) ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a\\b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf", "txt" } ) ) ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", (Collection) null ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new ArrayList() ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "txt" } ) ) ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf" } ) ) ) );
    assertEquals( true, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "rtf", "txt" } ) ) ) );

    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "TXT" } ) ) ) );
    assertEquals( false, RepositoryFilenameUtils.isExtension( "a.b\\file.txt", new ArrayList( Arrays
        .asList( new String[] { "TXT", "RTF" } ) ) ) );
  }

  public void testMatch() {
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( null, "Foo" ) );
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( "Foo", null ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( null, null ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo", "Foo" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "", "" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo", "Fo*" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo", "Fo?" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo Bar and Catflap", "Fo*" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "New Bookmarks", "N?w ?o?k??r?s" ) );
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( "Foo", "Bar" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo Bar Foo", "F*o Bar*" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Adobe Acrobat Installer", "Ad*er" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo", "*Foo" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "BarFoo", "*Foo" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "Foo", "Foo*" ) );
    assertEquals( true, RepositoryFilenameUtils.wildcardMatch( "FooBar", "Foo*" ) );
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( "FOO", "*Foo" ) );
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( "BARFOO", "*Foo" ) );
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( "FOO", "Foo*" ) );
    assertEquals( false, RepositoryFilenameUtils.wildcardMatch( "FOOBAR", "Foo*" ) );
  }

  private void assertMatch( String text, String wildcard, boolean expected ) {
    assertEquals( text + " " + wildcard, expected, RepositoryFilenameUtils.wildcardMatch( text, wildcard ) );
  }

  // A separate set of tests, added to this batch
  public void testMatch2() {
    assertMatch( "log.txt", "log.txt", true );
    assertMatch( "log.txt1", "log.txt", false );

    assertMatch( "log.txt", "log.txt*", true );
    assertMatch( "log.txt", "log.txt*1", false );
    assertMatch( "log.txt", "*log.txt*", true );

    assertMatch( "log.txt", "*.txt", true );
    assertMatch( "txt.log", "*.txt", false );
    assertMatch( "config.ini", "*.ini", true );

    assertMatch( "config.txt.bak", "con*.txt", false );

    assertMatch( "log.txt9", "*.txt?", true );
    assertMatch( "log.txt", "*.txt?", false );

    assertMatch( "progtestcase.java~5~", "*test*.java~*~", true );
    assertMatch( "progtestcase.java;5~", "*test*.java~*~", false );
    assertMatch( "progtestcase.java~5", "*test*.java~*~", false );

    assertMatch( "log.txt", "log.*", true );

    assertMatch( "log.txt", "log?*", true );

    assertMatch( "log.txt12", "log.txt??", true );

    assertMatch( "log.log", "log**log", true );
    assertMatch( "log.log", "log**", true );
    assertMatch( "log.log", "log.**", true );
    assertMatch( "log.log", "**.log", true );
    assertMatch( "log.log", "**log", true );

    assertMatch( "log.log", "log*log", true );
    assertMatch( "log.log", "log*", true );
    assertMatch( "log.log", "log.*", true );
    assertMatch( "log.log", "*.log", true );
    assertMatch( "log.log", "*log", true );

    assertMatch( "log.log", "*log?", false );
    assertMatch( "log.log", "*log?*", true );
    assertMatch( "log.log.abc", "*log?abc", true );
    assertMatch( "log.log.abc.log.abc", "*log?abc", true );
    assertMatch( "log.log.abc.log.abc.d", "*log?abc?d", true );
  }

  @Test
  public void testEscape() {
    List<Character> emptyList = Collections.emptyList();

    // null name
    try {
      RepositoryFilenameUtils.escape( null, emptyList );
      fail();
    } catch ( IllegalArgumentException e ) {
      // passed
    }

    // null reservedChars
    try {
      RepositoryFilenameUtils.escape( "hello", null );
      fail();
    } catch ( IllegalArgumentException e ) {
      // passed
    }

    // empty list
    assertEquals( "hello", RepositoryFilenameUtils.escape( "hello", emptyList ) );

    // nothing to escape
    assertEquals( "hello", RepositoryFilenameUtils.escape( "hello", Arrays.asList( new Character[] { '/' } ) ) );

    // something to escape
    assertEquals( "h%65llo", RepositoryFilenameUtils.escape( "hello", Arrays.asList( new Character[] { 'e' } ) ) );

    // % in name
    assertEquals( "hel%25lo", RepositoryFilenameUtils.escape( "hel%lo", emptyList ) );

    // ignore non-ascii
    assertEquals( "helloン", RepositoryFilenameUtils.escape( "helloン", emptyList ) );

  }

  @Test
  public void testUnescape() {
    // null name
    try {
      RepositoryFilenameUtils.unescape( null );
      fail();
    } catch ( IllegalArgumentException e ) {
      // passed
    }

    // nothing to unescape
    assertEquals( "hello", RepositoryFilenameUtils.unescape( "hello" ) );

    // something to unescape
    assertEquals( "hello", RepositoryFilenameUtils.unescape( "h%65llo" ) );

    // % in name
    assertEquals( "hel%lo", RepositoryFilenameUtils.unescape( "hel%25lo" ) );

    // ignore non-ascii
    assertEquals( "helloン", RepositoryFilenameUtils.unescape( "helloン" ) );

  }
}
