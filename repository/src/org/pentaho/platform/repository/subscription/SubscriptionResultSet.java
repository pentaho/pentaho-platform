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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Nov 1, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.repository.subscription;

import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISubscribeContent;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SubscriptionResultSet extends MemoryResultSet {

  IPentahoSession userSession;

  ISubscriptionRepository subscriptionRepository;

  String parameterNames[];

  String actionRef = null;

  public SubscriptionResultSet(final String scheduleId, final IPentahoSession userSession,
      final String parameterNames[], final String solution, final String path, final String action) {

    this.userSession = userSession;
    this.parameterNames = parameterNames;
    if ((solution != null) && (path != null) && (action != null)) {
      actionRef = solution + "/" + path + "/" + action; //$NON-NLS-1$ //$NON-NLS-2$
    }
    setMetaData(Subscription.getMetadata(parameterNames));
    subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, userSession);

    setRows(subscriptionRepository.getSubscriptionsForSchedule(scheduleId));
  }

  @Override
  public Object[] next() {

    if (iterator == null) {
      iterator = rows.iterator();
    }
    while (iterator.hasNext()) {
      Subscription subscr = (Subscription) iterator.next();
      if (actionRef != null) {
        ISubscribeContent content = subscr.getContent();
        if (isBursterFor(actionRef, content.getActionReference())) {
          return subscr.toResultRow(parameterNames);
        } else {
          continue;
        }
      } else {
        return subscr.toResultRow(parameterNames);
      }
    }
    return null;
  }

  private boolean isBursterFor(String bursterRef, String contentRef) {
    try {
      contentRef = contentRef.substring(contentRef.lastIndexOf('/'), contentRef.lastIndexOf(".xaction")); //$NON-NLS-1$
      bursterRef = bursterRef.substring(bursterRef.lastIndexOf('/'), bursterRef.lastIndexOf("Burst.xaction")); //$NON-NLS-1$
      return (bursterRef.equals(contentRef));
    } catch (Throwable t) {
      // if this blows up the compare would have falied anyway
    }
    return (false);
  }

  @Override
  public Object getValueAt(final int row, final int column) {
    Object[] theRow = (Object[]) rows.get(row);
    return theRow[column];
  }

}
