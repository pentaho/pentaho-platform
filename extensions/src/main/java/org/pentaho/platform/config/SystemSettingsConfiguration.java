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


package org.pentaho.platform.config;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemSettings;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * User: nbaker Date: 4/2/13
 */
public class SystemSettingsConfiguration implements IConfiguration {
  private final String id;
  private final ISystemSettings settings;
  private Properties properties;

  public SystemSettingsConfiguration( String id, ISystemSettings settings ) {
    if ( id == null ) {
      throw new IllegalArgumentException( "id cannot be null" );
    }
    if ( settings == null ) {
      throw new IllegalArgumentException( "SystemSettings is null" );
    }
    this.id = id;
    this.settings = settings;
  }

  @Override
  public String getId() {
    return "system";
  }

  @Override
  public synchronized Properties getProperties() {
    if ( properties == null ) {
      Properties props = new Properties();

      @SuppressWarnings( "rawtypes" )
      List elements = settings.getSystemSettings( "pentaho-system" );
      if ( elements == null ) {
        return null;
      }

      List<Node> nodes = ( (Element) elements.get( 0 ) ).content();
      addNodesToProperties( nodes, props, "" );

      properties = props;
    }

    return properties;
  }

  private void addNodesToProperties( List<Node> nodes, Properties props, String parentPath ) {

    for ( Node node : nodes ) {
      if ( !( node instanceof Element ) ) {
        // e.g. text
        continue;
      }

      Element ele = (Element) node;

      String contents = ele.getText().trim();

      String newParentPath = "";
      if ( !StringUtils.isEmpty( parentPath ) ) {
        newParentPath = parentPath + ".";
      }

      newParentPath += ele.getName();

      if ( !StringUtils.isEmpty( contents ) ) {
        props.setProperty( newParentPath, contents );
      }

      List<Node> children = ele.content();
      addNodesToProperties( children, props, newParentPath );
    }
  }

  @Override
  public void update( Properties addProperties ) throws IOException {
    throw new UnsupportedOperationException( "SystemSettings does not support write-back" );
  }
}
