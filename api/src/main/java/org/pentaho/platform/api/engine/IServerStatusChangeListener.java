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
 * Copyright 2015 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.api.engine;

/**
 * This is a listener for the IServerStatusProvider which should call onStatusChange whenever the values of
 * the ServerStatus or status message change value.
 * 
 * @author tkafalas
 */
public interface IServerStatusChangeListener {
  /**
   * Called whenever the IServerStatusProvider changes status, or status messages.
   */
  public void onStatusChange( );
}
