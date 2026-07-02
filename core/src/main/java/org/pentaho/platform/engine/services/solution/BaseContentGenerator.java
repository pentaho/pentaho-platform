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


package org.pentaho.platform.engine.services.solution;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.engine.core.audit.MDCUtil;
import org.pentaho.platform.engine.core.system.PentahoBase;

import java.util.List;
import java.util.Map;

public abstract class BaseContentGenerator extends PentahoBase implements IContentGenerator {

  private static final long serialVersionUID = 393425104931794680L;

  protected String instanceId;

  protected Map<String, IParameterProvider> parameterProviders;

  protected IPentahoSession userSession;

  protected List<Object> callbacks;

  protected IPentahoUrlFactory urlFactory;

  protected List<String> messages;

  protected IOutputHandler outputHandler;

  protected String itemName;

  public abstract Log getLogger();

  public abstract void createContent() throws Exception;

  public void setCallbacks( List<Object> callbacks ) {
    this.callbacks = callbacks;
  }

  protected Object getCallback( Class<?> clazz ) {
    if ( callbacks == null || callbacks.size() == 0 ) {
      // there are no callbacks
      return null;
    }

    // see if we have a callback of the appropriate type
    for ( Object obj : callbacks ) {
      Class<?>[] interfaces = obj.getClass().getInterfaces();
      if ( interfaces != null && interfaces.length > 0 ) {
        for ( Class<? extends Object> interfaze : interfaces ) {
          if ( interfaze.equals( clazz ) ) {
            // we found it
            return obj;
          }
        }
      }
    }
    // we did not find a callback of the requested type
    return null;
  }

  public void setInstanceId( String instanceId ) {
    this.instanceId = instanceId;
    MDCUtil.setInstanceId( instanceId );
  }

  public void setParameterProviders( Map<String, IParameterProvider> parameterProviders ) {
    this.parameterProviders = parameterProviders;
  }

  public void setSession( IPentahoSession userSession ) {
    this.userSession = userSession;
  }

  public void setUrlFactory( IPentahoUrlFactory urlFactory ) {
    this.urlFactory = urlFactory;
  }

  public void setMessagesList( List<String> messages ) {
    this.messages = messages;
  }

  public void setOutputHandler( IOutputHandler outputHandler ) {
    this.outputHandler = outputHandler;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName( String itemName ) {
    this.itemName = itemName;
  }

}
