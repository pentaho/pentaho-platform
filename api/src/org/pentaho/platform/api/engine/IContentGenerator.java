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

package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

/**
 * This interface is implemented by bi-platform plugins, and is instantiated for each content request made to the
 * plugin system via the IPluginManager API. Once instantiated, the setter methods are first called and then
 * createContent() is called to generate the expected content.
 */
public interface IContentGenerator extends ILogger {

  /**
   * the createContent() method is called after the content generator has been initialized appropriately, and is
   * responsible for populating the output handler.
   * 
   * @throws Exception
   */
  public void createContent() throws Exception;

  /**
   * called before createContent(), the IOutputHandler is the API that defines how to write content back to the
   * client.
   * 
   * @param outputHandler
   */
  public void setOutputHandler( IOutputHandler outputHandler );

  /**
   * called before createContent(), this method is not used by the content generator system at this time.
   * 
   * @param messages
   *          a list of messages
   */
  public void setMessagesList( List<String> messages );

  /**
   * called before createContent(), the parameterProviders list contain the available parameters.
   * 
   * @param parameterProviders
   */
  public void setParameterProviders( Map<String, IParameterProvider> parameterProviders );

  /**
   * called before createContent(), this is the users session object.
   * 
   * @param userSession
   */
  public void setSession( IPentahoSession userSession );

  /**
   * called before createContent(), this is used to build URLs
   * 
   * @param urlFactory
   */
  public void setUrlFactory( IPentahoUrlFactory urlFactory );

  /**
   * this method is not used at this time
   * 
   * @param callbacks
   */
  public void setCallbacks( List<Object> callbacks );

  /**
   * this method is not used at this time
   * 
   * @param instanceId
   */
  public void setInstanceId( String instanceId );

  /**
   * Get the name of the output content item for this content generator
   */
  public String getItemName();

  /**
   * Set the name of the output content item for the content generator
   * 
   * @param itemName
   */
  public void setItemName( String itemName );

}
