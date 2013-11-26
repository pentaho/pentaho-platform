/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.solution;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
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
