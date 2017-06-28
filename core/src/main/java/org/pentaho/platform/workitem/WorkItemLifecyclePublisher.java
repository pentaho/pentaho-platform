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
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.workitem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * A class for common utility methods related to work item lifecycles.
 */
@Configuration
public class WorkItemLifecyclePublisher {

  private static String PUBLISHER_BEAN_NAME = "workItemLifecyclePublisher";
  private static final Log log = LogFactory.getLog( WorkItemLifecyclePublisher.class );

  @Autowired
  private ApplicationEventPublisher publisher = null;

  public void setApplicationEventPublisher( final ApplicationEventPublisher publisher ) {
    this.publisher = publisher;
  }

  public ApplicationEventPublisher getApplicationEventPublisher() {
    return this.publisher;
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles that calls
   * {@link #publish(String, String, WorkItemLifecyclePhase, String, Date)} with a null {@code lifecycleDetails} and
   * {@code sourceTimestamp}
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link WorkItemLifecycleEvent}
   * @param workItemDetails        a {@link String} containing details of the {@link WorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   */
  public static void publish( final String workItemUid, final String workItemDetails, final WorkItemLifecyclePhase
    workItemLifecyclePhase ) {
    publish( workItemUid, workItemDetails, workItemLifecyclePhase, null, null );
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles that calls
   * {@link #publish(String, String, WorkItemLifecyclePhase, String, Date)} with a null {@code sourceTimestamp}
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link WorkItemLifecycleEvent}
   * @param workItemDetails        a {@link String} containing details of the {@link WorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   * @param lifecycleDetails       a {@link String} containing any additional details about the lifecycle event, such as
   *                               pertinent failure messages
   */
  public static void publish( final String workItemUid, final String workItemDetails, final WorkItemLifecyclePhase
    workItemLifecyclePhase, final String lifecycleDetails ) {
    publish( workItemUid, workItemDetails, workItemLifecyclePhase, lifecycleDetails, null );
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles that creates an instance of
   * {@link WorkItemLifecycleEvent} and calls the {@link #publish(WorkItemLifecycleEvent)} method
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link WorkItemLifecycleEvent}
   * @param workItemDetails        a {@link String} containing details of the {@link WorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   * @param lifecycleDetails       a {@link String} containing any additional details about the lifecycle event, such as
   *                               pertinent failure messages
   * @param sourceTimestamp        a {@link Date} representing the time the lifecycle change occurred.
   */
  public static void publish( final String workItemUid, final String workItemDetails, final WorkItemLifecyclePhase
    workItemLifecyclePhase, final String lifecycleDetails, final Date sourceTimestamp ) {
    final WorkItemLifecycleEvent workItemLifecycleEvent = createEvent( workItemUid, workItemDetails,
      workItemLifecyclePhase, lifecycleDetails, sourceTimestamp );
    publish( workItemLifecycleEvent );
  }

  protected static WorkItemLifecycleEvent createEvent( final String workItemUid, final String workItemDetails, final
    WorkItemLifecyclePhase workItemLifecyclePhase, final String lifecycleDetails, final Date sourceTimestamp ) {
    return  new WorkItemLifecycleEvent( workItemUid, workItemDetails,
      workItemLifecyclePhase, lifecycleDetails, sourceTimestamp );
  }
  /**
   * A convenience method for publishing changes to the work item's lifecycles. Fetches the
   * {@link WorkItemLifecyclePublisher} bean, and if available, calls its {@link #publishImpl(WorkItemLifecycleEvent)}
   * method. Otherwise does nothing, as the {@link WorkItemLifecyclePublisher} bean may not be available, which is a
   * perfectly valid scenario, if we do not care about publishing {@link WorkItemLifecycleEvent}'s.
   *
   * @param workItemLifecycleEvent the {@link WorkItemLifecycleEvent}
   */
  public static void publish( final WorkItemLifecycleEvent workItemLifecycleEvent ) {
    final WorkItemLifecyclePublisher publisher = getInstance();
    if ( publisher != null ) {
      publisher.publishImpl( workItemLifecycleEvent );
    } else {
      log.debug( String.format( "'%s' publisher bean is not available, unable to publish work item  lifecycle: %s",
        PUBLISHER_BEAN_NAME, workItemLifecycleEvent.toString() ) );
    }
  }

  private static WorkItemLifecyclePublisher instance;
  public static synchronized WorkItemLifecyclePublisher getInstance() {
    if ( instance == null ) {
      synchronized ( WorkItemLifecyclePublisher.class ) {
        if ( instance == null ) {
          instance = PentahoSystem.get( WorkItemLifecyclePublisher.class, PUBLISHER_BEAN_NAME, PentahoSessionHolder
            .getSession() );
        }
      }
    }
    return instance;
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles. Fetches the available
   * {@link ApplicationEventPublisher}, and if available, calls its
   * {@link ApplicationEventPublisher#publishEvent(Object)} method, where the Object passed to the method is the
   * {@link WorkItemLifecycleEvent} representing the {@link WorkItemLifecycleEvent}. Otherwise does nothing, as the
   * {@link ApplicationEventPublisher} may not be available, which is a perfectly valid scenario, if we do not care
   * about publishing {@link WorkItemLifecycleEvent}'s.
   *
   * @param workItemLifecycleEvent the {@link WorkItemLifecycleEvent}
   */
  protected void publishImpl( final WorkItemLifecycleEvent workItemLifecycleEvent ) {

    if ( getApplicationEventPublisher() != null ) {
      getApplicationEventPublisher().publishEvent( workItemLifecycleEvent );
    } else {
      log.debug( String.format( "Publisher in bean '%s' is not available, unable to publish work item lifecycle: "
        + "%s", PUBLISHER_BEAN_NAME, workItemLifecycleEvent.toString() ) );
    }
  }
}
