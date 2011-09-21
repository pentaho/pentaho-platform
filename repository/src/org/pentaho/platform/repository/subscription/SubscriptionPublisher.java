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
 * @created Jun 25, 2008 
 * @author wseyler
 * 
 */

package org.pentaho.platform.repository.subscription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * @author wseyler
 *
 */
public class SubscriptionPublisher extends BasePublisher {

  private static final long serialVersionUID = -2578938864526146490L;
  private static final Log logger = LogFactory.getLog(SubscriptionPublisher.class);

   public Log getLogger() {
    return logger;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.engine.core.system.BasePublisher#publish(org.pentaho.platform.api.engine.IPentahoSession)
   */
  @Override
  public String publish(IPentahoSession session) {
    String publishSrcPath = PentahoSystem.getApplicationContext().getSolutionPath("") + "system/ScheduleAndContentImport.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    Document document = DocumentHelper.createDocument();
    Element root = DocumentHelper.createElement("importContentResults"); //$NON-NLS-1$
    document.add(root);
    try {
      ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, session);
      File file = new File(publishSrcPath);
      if ( !file.canRead() ) {
        throw new FileNotFoundException( "SubscriptionPublisher.publish() requires the file \""
            + publishSrcPath
            + "\" to exist. The file does not exist." );
      }
      Document importDoc = XmlDom4JHelper.getDocFromFile(file, null);
      
      root.add(subscriptionRepository.importSchedules(importDoc));
      root.add(subscriptionRepository.importContent(importDoc));
    } catch (FileNotFoundException e) {
      getLogger().error(Messages.getInstance().getString("SubscriptionPublisher.ERROR_0001", publishSrcPath), e); //$NON-NLS-1$
      return Messages.getInstance().getString("SubscriptionPublisher.ERROR_0002", publishSrcPath); //$NON-NLS-1$
    } catch (DocumentException e) {
      getLogger().error(Messages.getInstance().getString("SubscriptionPublisher.ERROR_0003", publishSrcPath), e); //$NON-NLS-1$
      return Messages.getInstance().getString("SubscriptionPublisher.ERROR_0004") + publishSrcPath; //$NON-NLS-1$
    } catch (IOException e) {
      getLogger().error(Messages.getInstance().getString("SubscriptionPublisher.ERROR_0005", publishSrcPath), e); //$NON-NLS-1$
      return Messages.getInstance().getString("SubscriptionPublisher.ERROR_0006", publishSrcPath); //$NON-NLS-1$
    }
    
    List resultNodes = document.selectNodes("//@result"); //$NON-NLS-1$
    for (Iterator iter = resultNodes.iterator(); iter.hasNext(); ) {
      Attribute attribute = (Attribute) iter.next();
      if ("ERROR".equalsIgnoreCase(attribute.getValue())) { //$NON-NLS-1$
        return Messages.getInstance().getString("SubscriptionPublisher.ERROR_0007"); //$NON-NLS-1$
      }
    }
    
    return Messages.getInstance().getString("SubscriptionPublisher.INFO_0001"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IPentahoPublisher#getDescription()
   */
  public String getDescription() {
    return Messages.getInstance().getString("SubscriptionPublisher.INFO_0002"); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.IPentahoPublisher#getName()
   */
  public String getName() {
    return Messages.getInstance().getString("SubscriptionPublisher.INFO_0003"); //$NON-NLS-1$
  }

}
