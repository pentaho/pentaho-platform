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

public interface IUITemplater {

  public String processTemplate( String template, String title, String content, IPentahoSession session );

  public String processTemplate( String template, String title, IPentahoSession session );

  public String getTemplate( String templateName, IPentahoSession session );

  public String[] breakTemplate( String templateName, String title, IPentahoSession session );

  public String[] breakTemplateString( String template, String title, IPentahoSession session );

  public void setHeaderContent( String headerContent );
}
