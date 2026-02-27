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


package org.pentaho.platform.api.mimetype;

import java.util.List;

import org.pentaho.platform.api.repository2.unified.Converter;

public interface IMimeType {

  public String getName();

  public void setName( String name );

  public List<String> getExtensions();

  public boolean isHidden();

  public void setHidden( boolean hidden );

  public boolean isLocale();

  public void setLocale( boolean locale );

  public boolean isVersionEnabled();

  public void setVersionEnabled( boolean versionEnabled );

  public boolean isVersionCommentEnabled();

  public void setVersionCommentEnabled( boolean versionCommentEnabled );

  public Converter getConverter();

  public void setConverter( Converter converter );

  public void setExtensions( List<String> extensions );

  public String toString();

}
