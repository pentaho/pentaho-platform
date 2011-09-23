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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 14, 2008
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import java.io.File;

import org.pentaho.platform.engine.core.messages.Messages;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * This factory implementation creates and uses a self-contained Spring
 * {@link ApplicationContext} which is not tied to or accesible by any
 * other parts of the application.
 * 
 * @author Aaron Phillips
 * @see AbstractSpringPentahoObjectFactory
 *
 */
public class StandaloneSpringPentahoObjectFactory extends AbstractSpringPentahoObjectFactory {

  /**
   * Initializes this object factory by creating a self-contained Spring
   * {@link ApplicationContext} if one is not passed in.
   * 
   * @param configFile  the Spring bean definition XML file
   * @param context   the {@link ApplicationContext} object, if null, then this method
   *                  will create one
   */
  public void init(String configFile, Object context) {

    
    if (context == null) {
      //      beanFactory = new FileSystemXmlApplicationContext(configFile);
      File f = new File(configFile);
      FileSystemResource fsr = new FileSystemResource(f);
      GenericApplicationContext appCtx = new GenericApplicationContext();
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);
      xmlReader.loadBeanDefinitions(fsr);

      beanFactory = appCtx;
    } else {
      if (!(context instanceof ApplicationContext)) {
        String msg = Messages.getInstance().getErrorString("StandalonePentahoObjectFactory.ERROR_0001_CONTEXT_NOT_SUPPORTED", //$NON-NLS-1$
            getClass().getSimpleName(), "ApplicationContext", context.getClass().getName()); //$NON-NLS-1$
        throw new IllegalArgumentException(msg);
      }
      
      beanFactory = (ApplicationContext) context;
    }
  }
}