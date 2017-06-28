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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.pentaho.platform.util.messages.Messages;

/**
 * An enumeration of the known lifecycle events for the work item.
 */
public enum WorkItemLifecyclePhase {

  /**
   * The work item has been submitted for execution
   */
  SUBMITTED( "LifecyclePhase.SUBMITTED" ),
  /**
   * The work item has been dispatched to the component responsible for its execution
   */
  DISPATCHED( "LifecyclePhase.DISPATCHED" ),
  /**
   * The work item has been received by the component responsible for its execution
   */
  RECEIVED( "LifecyclePhase.RECEIVED" ),
  /**
   * The work item execution has been rejected
   */
  REJECTED( "LifecyclePhase.REJECTED" ),
  /**
   * The work item execution is in progress
   */
  IN_PROGRESS( "LifecyclePhase.IN_PROGRESS" ),
  /**
   * The work item execution has succeeded
   */
  SUCCEEDED( "LifecyclePhase.SUCCEEDED" ),
  /**
   * The work item execution has failed
   */
  FAILED( "LifecyclePhase.FAILED" ),
  /**
   * The work item execution has been restarted
   */
  RESTARTED( "LifecyclePhase.RESTARTED" );

  private String nameMessageKey;

  WorkItemLifecyclePhase( final String nameMessageKey ) {
    this.nameMessageKey = nameMessageKey;
  }

  public String getName() {
    return getMessageBundle().getString( nameMessageKey );
  }

  public String getDescription() {
    return getMessageBundle().getString( nameMessageKey + "_DESC" );
  }

  /**
   * Returns an instance of {@link WorkItemLifecyclePhase} whose name matches the provided {@code name}.
   *
   * @param name the name of the {@link WorkItemLifecyclePhase} being fetched
   * @return an instance of {@link WorkItemLifecyclePhase} whose name matches the provided {@code name} or null, if no
   * match is found
   */
  public static WorkItemLifecyclePhase get( final String name ) {
    final WorkItemLifecyclePhase[] instances = WorkItemLifecyclePhase.class.getEnumConstants();
    for ( final WorkItemLifecyclePhase instance : instances ) {
      if ( instance.getName().equals( name ) ) {
        return instance;
      }
    }
    return null;
  }

  public String toString() {
    return new ToStringBuilder( this )
      .append( "name", this.getName() )
      .append( "description", this.getDescription() )
      .toString();
  }

  protected static Messages getMessageBundle() {
    return Messages.getInstance();
  }
}
