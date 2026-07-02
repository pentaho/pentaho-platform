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

package org.pentaho.platform.web.jaxws.spring;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import org.springframework.beans.factory.BeanNameAware;

public class SpringBinding implements BeanNameAware {
  private String beanName;
  private String urlPattern;
  private WSEndpoint<?> endpoint;

  public SpringBinding() {
  }

  public void setBeanName( String name ) {
    this.beanName = name;
  }

  public void create( ServletAdapterList owner ) {
    String name = this.beanName;
    if ( name == null ) {
      name = this.urlPattern;
    }

    owner.createAdapter( name, this.urlPattern, this.endpoint );
  }

  public void setUrl( String urlPattern ) {
    this.urlPattern = urlPattern;
  }

  public void setService( WSEndpoint<?> endpoint ) {
    this.endpoint = endpoint;
  }
}
