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
    Assert.notNull( fileName, "The file name must not be null. Ensure a valid file name is provided." );
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
