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

/**
 * 
 */

package org.pentaho.platform.config;

/**
 * The AnalysisView enum facilitates predefined views for charting performance metrics
 * 
 * @author gmoran
 * 
 */
public enum AnalysisView {

  FIFTEEN_MINUTES( .25, 60 ), // 60 buckets, 15 seconds each
  THIRTY_MINUTES( .5, 60 ), // 60 buckets, 30 seconds each
  ONE_HOUR( 1, 60 ), // 60 buckets, 1 minute each
  TWELVE_HOURS( 10, 72 ), // 72 buckets, 10 minutes each
  TWENTY_FOUR_HOURS( 15, 96 ), // 96 buckets, 15 minutes each
  SEVEN_DAYS( 120, 84 ), // 84 buckets, 2 hours each
  THIRTY_DAYS( 720, 60 ), // 60 buckets, 12 hours each
  NINETY_DAYS( 1440, 90 ); // 90 buckets, 1 day each

  /**
   * interval The interval in minutes that we are distributing data by
   */
  private final double interval;

  /**
   * periods The number of periods over which to distribute the data
   */
  private final int periods;

  AnalysisView( double interval, int periods ) {
    this.interval = interval;
    this.periods = periods;
  }

  /**
   * @return the interval
   */
  public final double getInterval() {
    return interval;
  }

  /**
   * @return the periods
   */
  public final int getPeriods() {
    return periods;
  }

}
