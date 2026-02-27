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


package org.pentaho.commons.util.repository.type;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

public interface CmisObject {

  public static final String OBJECT_TYPE_FOLDER = "Folder"; //$NON-NLS-1$

  public static final String LOCALIZEDNAME = "LocalizedName"; //$NON-NLS-1$

  public static final String NAME = "Name"; //$NON-NLS-1$

  public static final String VISIBLE = "Visible"; //$NON-NLS-1$

  public CmisProperties getProperties();

  public void setProperties( CmisProperties properties );

  public AllowableActions getAllowableActions();

  public void setAllowableActions( AllowableActions allowableActions );

  public List<CmisObject> getRelationship();

  public void setRelationship( List<CmisObject> relationship );

  public List<CmisObject> getChild();

  public void setChild( List<CmisObject> child );

  public String findStringProperty( String name, String defaultValue );

  public Boolean findBooleanProperty( String name, boolean defaultValue );

  public Calendar findDateTimeProperty( String name, Calendar defaultValue );

  public BigDecimal findDecimalProperty( String name, BigDecimal defaultValue );

  public String findHtmlProperty( String name, String defaultValue );

  public String findIdProperty( String name, String defaultValue );

  public Integer findIntegerProperty( String name, Integer defaultValue );

  public String findUriProperty( String name, String defaultValue );

  public String findXmlProperty( String name, String defaultValue );

}
