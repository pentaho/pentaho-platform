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


package org.pentaho.platform.api.engine;

public interface IFileInfo {

  public String getTitle();

  public void setTitle( String title );

  public String getDescription();

  public void setDescription( String description );

  public String getAuthor();

  public void setAuthor( String author );

  public String getIcon();

  public void setIcon( String icon );

  public String getDisplayType();

  public void setDisplayType( String displayType );
}
