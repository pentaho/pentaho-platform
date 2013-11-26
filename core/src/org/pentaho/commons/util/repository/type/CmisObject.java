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
