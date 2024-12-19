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

public class CmisObjectImpl implements CmisObject {

  private CmisProperties properties;

  private AllowableActions allowableActions;

  private List<CmisObject> relationship;

  private List<CmisObject> child;

  public CmisProperties getProperties() {
    return properties;
  }

  public void setProperties( CmisProperties properties ) {
    this.properties = properties;
  }

  public AllowableActions getAllowableActions() {
    return allowableActions;
  }

  public void setAllowableActions( AllowableActions allowableActions ) {
    this.allowableActions = allowableActions;
  }

  public List<CmisObject> getRelationship() {
    return relationship;
  }

  public void setRelationship( List<CmisObject> relationship ) {
    this.relationship = relationship;
  }

  public List<CmisObject> getChild() {
    return child;
  }

  public void setChild( List<CmisObject> child ) {
    this.child = child;
  }

  public String findStringProperty( String name, String defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.STRING ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (String) property.getValue();
  }

  public Boolean findBooleanProperty( String name, boolean defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.BOOLEAN ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (Boolean) property.getValue();
  }

  public Calendar findDateTimeProperty( String name, Calendar defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.DATETIME ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (Calendar) property.getValue();
  }

  public BigDecimal findDecimalProperty( String name, BigDecimal defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.DECIMAL ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (BigDecimal) property.getValue();
  }

  public String findHtmlProperty( String name, String defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.HTML ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (String) property.getValue();
  }

  public String findIdProperty( String name, String defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.ID ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (String) property.getValue();
  }

  public Integer findIntegerProperty( String name, Integer defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.INTEGER ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (Integer) property.getValue();
  }

  public String findUriProperty( String name, String defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.URI ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (String) property.getValue();
  }

  public String findXmlProperty( String name, String defaultValue ) {
    CmisProperty property = findProperty( name, new PropertyType( PropertyType.XML ) );
    if ( property == null ) {
      return defaultValue;
    }
    return (String) property.getValue();
  }

  private CmisProperty findProperty( String name, PropertyType type ) {
    if ( properties != null ) {
      for ( CmisProperty aProperty : properties.getProperties() ) {
        if ( aProperty.getName().equals( name ) && aProperty.getPropertyType().getType().equals( type.getType() ) ) {
          return aProperty;
        }
      }
    }
    return null;
  }

}
