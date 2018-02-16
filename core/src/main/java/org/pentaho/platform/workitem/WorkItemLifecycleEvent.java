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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.workitem;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEvent;
import org.pentaho.platform.util.StringUtil;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

/**
 * This class encapsulates all information pertaining to a "work item" at a specific point in its lifecycle.
 */
@XmlRootElement
public class WorkItemLifecycleEvent implements IWorkItemLifecycleEvent {

  private static final Log logger = LogFactory.getLog( WorkItemLifecycleEvent.class );

  private String workItemUid;
  private String workItemDetails;
  private WorkItemLifecyclePhase workItemLifecyclePhase;
  private String lifecycleDetails;
  private Date sourceTimestamp;
  private String sourceHostName;
  private String sourceHostIp;
  private static String HOST_NAME;
  private static String HOST_IP;

  static {
    try {
      HOST_NAME = InetAddress.getLocalHost().getCanonicalHostName();
      HOST_IP = InetAddress.getLocalHost().getHostAddress();
    } catch ( final UnknownHostException uhe ) {
      logger.error( uhe.getLocalizedMessage() );
    }
  }

  /**
   * Default constructor, needed for serialization purposes.
   */
  public WorkItemLifecycleEvent() { }

  /**
   * Creates the {@link WorkItemLifecycleEvent} with all the required parameters.
   *
   * @param workItemUid            a {@link String} containing unique identifier for the {@link WorkItemLifecycleEvent}
   * @param workItemDetails        an {@link String} containing details of the {@link WorkItemLifecycleEvent}
   * @param workItemLifecyclePhase a {@link WorkItemLifecyclePhase} representing the lifecycle event
   * @param lifecycleDetails       a {@link String} containing any additional details about the lifecycle event, such as
   *                               pertinent failure messages
   * @param sourceTimestamp        a {@link Date} representing the time the lifecycle change occurred.
   */

  public WorkItemLifecycleEvent( final String workItemUid, final String workItemDetails, final WorkItemLifecyclePhase
    workItemLifecyclePhase, final String lifecycleDetails, final Date sourceTimestamp ) {

    this.workItemUid = workItemUid;
    this.workItemDetails = workItemDetails;
    this.workItemLifecyclePhase = workItemLifecyclePhase;
    this.lifecycleDetails = lifecycleDetails;
    this.sourceTimestamp = sourceTimestamp;

    // if the workItemUid is null, generate it
    if ( StringUtil.isEmpty( this.workItemUid ) ) {
      this.workItemUid = UUID.randomUUID().toString();
    }
    // Set sourceTimestamp to current date only if not already provided
    if ( this.sourceTimestamp == null ) {
      this.sourceTimestamp = new Date();
    }

    // set the default values for host name and ip, they can be changed directly if needed
    this.sourceHostName = HOST_NAME;
    this.sourceHostIp = HOST_IP;
  }

  public String getWorkItemUid() {
    return workItemUid;
  }

  public void setWorkItemUid( final String workItemUid ) {
    this.workItemUid = workItemUid;
  }

  public String getWorkItemDetails() {
    return workItemDetails;
  }

  public void setWorkItemDetails( final String workItemDetails ) {
    this.workItemDetails = workItemDetails;
  }

  public WorkItemLifecyclePhase getWorkItemLifecyclePhase() {
    return workItemLifecyclePhase;
  }

  public void setWorkItemLifecyclePhase( final WorkItemLifecyclePhase workItemLifecyclePhase ) {
    this.workItemLifecyclePhase = workItemLifecyclePhase;
  }

  public String getLifecycleDetails() {
    return lifecycleDetails;
  }

  public void setLifecycleDetails( final String lifecycleDetails ) {
    this.lifecycleDetails = lifecycleDetails;
  }

  @XmlJavaTypeAdapter( DateAdapter.class )
  public Date getSourceTimestamp() {
    return sourceTimestamp;
  }

  public void setSourceTimestamp( final Date sourceTimestamp ) {
    this.sourceTimestamp = sourceTimestamp;
  }

  public String getSourceHostName() {
    return sourceHostName;
  }

  public void setSourceHostName( final String sourceHostName ) {
    this.sourceHostName = sourceHostName;
  }

  public String getSourceHostIp() {
    return sourceHostIp;
  }

  public void setSourceHostIp( final String sourceHostIp ) {
    this.sourceHostIp = sourceHostIp;
  }

  public String toString() {
    return new ToStringBuilder( this )
      .append( "workItemUid", this.workItemUid )
      .append( "workItemDetails", this.workItemDetails )
      .append( "workItemLifecyclePhase", this.workItemLifecyclePhase )
      .append( "lifecycleDetails", this.lifecycleDetails )
      .append( "sourceTimestamp", this.sourceTimestamp )
      .append( "sourceHostName", this.sourceHostName )
      .append( "sourceHostIp", this.sourceHostIp )
      .toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getWorkItemUid() )
      .append( this.getWorkItemLifecyclePhase() )
      .toHashCode();
  }

  @Override
  public boolean equals( final Object other ) {
    final WorkItemLifecycleEvent otherCast = (WorkItemLifecycleEvent) other;
    if ( this == otherCast ) {
      return true;
    } else if ( otherCast == null ) {
      return false;
    } else {
      return new EqualsBuilder()
        .append( this.getWorkItemUid(), otherCast.getWorkItemUid() )
        .append( this.getWorkItemLifecyclePhase(), otherCast.getWorkItemLifecyclePhase() )
        .isEquals();
    }
  }

  /**
   * An implementation of {@link XmlAdapter} that allows us to [de]serialize Dates as longs (milliseconds).
   */
  public static class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal( final Date date ) throws Exception {
      return date.getTime() + "";
    }

    @Override
    public Date unmarshal( final String dateStr ) throws Exception {
      return new Date( Long.parseLong( dateStr ) );
    }
  }
}
