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

package org.pentaho.platform.repository2.unified.jcr;

import org.springframework.util.Assert;

/**
 * @author Rowell Belen Encapsulate the logic for retrieving the proper title/description keys
 */
public class LocalePropertyResolver {

  private final String FILE_URL = "url";

  private final String DEFAULT_NAME_KEY = "name";
  private final String DEFAULT_TITLE_KEY = "file.title";
  private final String DEFAULT_DESCRIPTION_KEY = "file.description";

  private final String DEFAULT_TITLE = "title";
  private final String DEFAULT_DESCRIPTION = "description";

  private final String URL_TITLE = "url_name";
  private final String URL_DESCRIPTION = "url_description";

  private String fileName;

  public LocalePropertyResolver( String fileName ) {
    Assert.notNull( fileName );
    this.fileName = fileName;
  }

  public String resolveNameKey() {
    return DEFAULT_NAME_KEY;
  }

  public String resolveDefaultTitleKey() {
    return DEFAULT_TITLE_KEY;
  }

  public String resolveDefaultDescriptionKey() {
    return DEFAULT_DESCRIPTION_KEY;
  }

  public String resolveTitleKey() {
    if ( this.fileName.endsWith( FILE_URL ) ) {
      return URL_TITLE;
    }
    return DEFAULT_TITLE;
  }

  public String resolveDescriptionKey() {

    if ( this.fileName.endsWith( FILE_URL ) ) {
      return URL_DESCRIPTION;
    }
    return DEFAULT_DESCRIPTION;
  }
}
