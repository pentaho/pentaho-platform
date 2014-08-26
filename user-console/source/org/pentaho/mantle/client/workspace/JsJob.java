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

package org.pentaho.mantle.client.workspace;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import java.util.Date;

public class JsJob extends JavaScriptObject {

  // Overlay types always have protected, zero argument constructors.
  protected JsJob() {
  }

  // JSNI methods to get job data.
  public final native String getJobId() /*-{ return this.jobId; }-*/; //

  public final native String getJobName() /*-{ return this.jobName; }-*/; //

  public final native String getUserName() /*-{ return this.userName; }-*/; //

  private final native String getNativeNextRun() /*-{ return this.nextRun; }-*/; //

  private final native String getNativeLastRun() /*-{ return this.lastRun; }-*/; //

  public final native JsArray<JsJobParam> getJobParams() /*-{ return this.jobParams.jobParams; }-*/; //

  public final native JsJobTrigger getJobTrigger() /*-{ return this.jobTrigger; }-*/; //

  public final native String getState() /*-{ return this.state; }-*/; //

  public final native void setState( String newState ) /*-{ this.state = newState; }-*/; //

  public final String getJobParamValue( String name ) {
    if ( hasJobParams() ) {
      JsArray<JsJobParam> params = getJobParams();
      for ( int i = 0; i < params.length(); i++ ) {
        JsJobParam param = params.get( i );
        if ( param.getName().equals( name ) ) {
          return param.getValue();
        }
      }
    }
    return null;
  }

  public final JsJobParam getJobParam( String name ) {
    if ( hasJobParams() ) {
      JsArray<JsJobParam> params = getJobParams();
      for ( int i = 0; i < params.length(); i++ ) {
        JsJobParam param = params.get( i );
        if ( param.getName().equals( name ) ) {
          return param;
        }
      }
    }
    return null;
  }

  private final native boolean hasJobParams() /*-{ return this.jobParams != null; }-*/; //

  public final boolean hasResourceName() {
    String resource = getJobParamValue( "ActionAdapterQuartzJob-StreamProvider" );
    return ( resource != null && !"".equals( resource ) );
  }

  public final String getFullResourceName() {
    String resource = getJobParamValue( "ActionAdapterQuartzJob-StreamProvider" );
    if ( resource == null || "".equals( resource ) ) {
      return getJobName();
    }
    if ( getJobName().contains( ":" ) ) {
      resource = resource.substring( resource.indexOf( "/" ), resource.indexOf( ":outputFile = /" ) );
    }
    else {
      resource = resource.substring( resource.indexOf( "/" ), resource.indexOf( ":" ) );
    }
    return resource;
  }

  public final String getOutputPath() {
    String resource = getJobParamValue( "ActionAdapterQuartzJob-StreamProvider" );
    if ( resource == null || "".equals( resource ) ) {
      return "";
    }
    resource = resource.substring( resource.indexOf( ":" ) );
    resource = resource.substring( resource.indexOf( "/" ), resource.lastIndexOf( "/" ) );
    return resource;
  }

  public final void setOutputPath( String outputPath, String outputFileName ) {
    JsJobParam resource = getJobParam( "ActionAdapterQuartzJob-StreamProvider" );
    // input file = /public/Inventory.prpt:outputFile = /public/TEST.*
    resource.setValue( "input file = " + getFullResourceName() + ":outputFile = " + outputPath + "/" + outputFileName
        + ".*" );
  }

  public final String getShortResourceName() {
    String resource = getFullResourceName();
    if ( resource.indexOf( "/" ) != -1 ) {
      resource = resource.substring( resource.lastIndexOf( "/" ) + 1 );
    }
    return resource;
  }

  public final Date getLastRun() {
    return formatDate( getNativeLastRun() );
  }

  public final Date getNextRun() {
    return formatDate( getNativeNextRun() );
  }

  public static Date formatDate( String dateStr ) {
    try {
      DateTimeFormat format = DateTimeFormat.getFormat( PredefinedFormat.ISO_8601 );
      return format.parse( dateStr );
    } catch ( Throwable t ) {
      //ignored
    }

    try {
      DateTimeFormat format = DateTimeFormat.getFormat( "yyyy-MM-dd'T'HH:mm:ssZZZ" );
      return format.parse( dateStr );
    } catch ( Throwable t ) {
      //ignored
    }

    return null;
  }

  public final native void setJobTrigger( JsJobTrigger trigger ) /*-{ this.jobTrigger = trigger; }-*/;

  public final native String setJobName( String name ) /*-{ this.jobName = name; }-*/; //

}
