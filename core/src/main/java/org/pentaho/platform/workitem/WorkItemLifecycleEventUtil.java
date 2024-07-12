/*!
 *
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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.workitem;

import org.pentaho.platform.api.workitem.IWorkItemLifecycleEvent;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.ActionUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * A class for common utility methods related to work item lifecycle events.
 */
public class WorkItemLifecycleEventUtil {

  /**
   * A convenience method for publishing changes to the work item's lifecycles that calls {@link #publish(String, Map,
   * WorkItemLifecyclePhase, String, Date)} with a null {@code lifecycleDetails} and {@code sourceTimestamp}
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link WorkItemLifecycleEvent}
   * @param details                an {@link Map} containing details of the {@link WorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   */
  public static void publish( final String workItemUid,
                              final Map<String, Object> details,
                              final WorkItemLifecyclePhase workItemLifecyclePhase ) {
    publish( workItemUid, details, workItemLifecyclePhase, null, null );
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles that calls {@link #publish(String, Map,
   * WorkItemLifecyclePhase, String, Date)} with a null {@code sourceTimestamp}
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link WorkItemLifecycleEvent}
   * @param details                an {@link Map} containing details of the {@link WorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   * @param lifecycleDetails       a {@link String} containing any additional details about the lifecycle event, such as
   *                               pertinent failure messages
   */
  public static void publish( final String workItemUid,
                              final Map<String, Object> details,
                              final WorkItemLifecyclePhase workItemLifecyclePhase, final String lifecycleDetails ) {
    publish( workItemUid, details, workItemLifecyclePhase, lifecycleDetails, null );
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles that creates an instance of {@link
   * WorkItemLifecycleEvent} and calls the {@link #publish(IWorkItemLifecycleEvent)} method
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link
   *                               IWorkItemLifecycleEvent}
   * @param details                an {@link Map} containing details of the {@link IWorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   * @param lifecycleDetails       a {@link String} containing any additional details about the lifecycle event, such as
   *                               pertinent failure messages
   * @param sourceTimestamp        a {@link Date} representing the time the lifecycle change occurred.
   */
  public static void publish( final String workItemUid,
                              final Map<String, Object> details,
                              final WorkItemLifecyclePhase workItemLifecyclePhase,
                              final String lifecycleDetails,
                              final Date sourceTimestamp ) {
    final IWorkItemLifecycleEvent workItemLifecycleEvent = createEvent( workItemUid, extractDetails( details ),
      workItemLifecyclePhase, lifecycleDetails, sourceTimestamp );
    publish( workItemLifecycleEvent );
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles that creates an instance of {@link
   * WorkItemLifecycleEvent} and calls the {@link #publish(IWorkItemLifecycleEvent)} method
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link
   *                               IWorkItemLifecycleEvent}
   * @param details                a {@link String} containing details of the {@link IWorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   * @param lifecycleDetails       a {@link String} containing lifecycle of the {@link IWorkItemLifecycleEvent}
   */
  public static void publish( final String workItemUid,
                              final String details,
                              final WorkItemLifecyclePhase workItemLifecyclePhase,
                              final String lifecycleDetails ) {
    final IWorkItemLifecycleEvent workItemLifecycleEvent = createEvent( workItemUid, details,
      workItemLifecyclePhase, lifecycleDetails, null );
    publish( workItemLifecycleEvent );
  }

  protected static IWorkItemLifecycleEvent createEvent( final String workItemUid,
                                                        final String workItemDetails,
                                                        final WorkItemLifecyclePhase workItemLifecyclePhase,
                                                        final String lifecycleDetails,
                                                        final Date sourceTimestamp ) {
    return new WorkItemLifecycleEvent( workItemUid, workItemDetails, workItemLifecyclePhase, lifecycleDetails,
      sourceTimestamp );
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles. Fetches the {@link
   * IWorkItemLifecycleEventPublisher} bean, and if available, calls its post method. Otherwise does nothing, as the
   * {@link IWorkItemLifecycleEventPublisher} bean may not be available, which is a perfectly valid scenario, if we do
   * not care about publishing {@link IWorkItemLifecycleEvent}'s.
   *
   * @param workItemLifecycleEvent the {@link IWorkItemLifecycleEvent}
   */
  public static void publish( final IWorkItemLifecycleEvent workItemLifecycleEvent ) {
    final IWorkItemLifecycleEventPublisher publisher = PentahoSystem.get( IWorkItemLifecycleEventPublisher.class );
    if ( publisher != null ) {
      publisher.publish( workItemLifecycleEvent );
    }
  }

  private static String extractDetails( final Map<String, Object> detailsMap ) {
    if ( detailsMap == null ) {
      return "";
    }

    final StringBuilder sb = new StringBuilder();
    if ( detailsMap.containsKey( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE ) ) {
      sb.append( detailsMap.get( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE ).toString() );

      if ( detailsMap.containsKey( ActionUtil.INVOKER_ACTIONUSER ) ) {
        sb.append( "[" );
        sb.append( detailsMap.get( ActionUtil.INVOKER_ACTIONUSER ).toString() );
        sb.append( "]" );
      }
    }

    if ( detailsMap.containsKey( ActionUtil.INVOKER_UUID ) ) {
      sb.append( ActionUtil.INVOKER_UUID ).append( " " ).append( detailsMap.get( ActionUtil.INVOKER_UUID ).toString() );
    }
    if ( detailsMap.containsKey( ActionUtil.INVOKER_STATUS ) ) {
      sb.append( detailsMap.get( ActionUtil.INVOKER_STATUS ).toString() );
    }

    return sb.toString();
  }
}
