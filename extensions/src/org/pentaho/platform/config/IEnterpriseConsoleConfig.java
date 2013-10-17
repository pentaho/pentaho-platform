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

package org.pentaho.platform.config;

public interface IEnterpriseConsoleConfig extends IConsoleConfig {
  public Integer getMetricsChartHeight();

  public void setMetricsChartHeight( Integer height );

  public Integer getMetricsChartWidth();

  public void setMetricsChartWidth( Integer width );

  public Integer getMetricsSmallChartHeight();

  public void setMetricsSmallChartHeight( Integer height );

  public Integer getMetricsSmallChartWidth();

  public void setMetricsSmallChartWidth( Integer height );

  public Double getMetricsInterval();

  public void setMetricsInterval( Double height );

  public AnalysisView getMetricsView();

  public void setMetricsView( AnalysisView view );

  public Double getMetricsExecutionLimit();

  public void setMetricsExecutionLimit( Double limit );

  public Boolean getPdiOnly();

  public void setPdiOnly( Boolean pdiOnly );
}
