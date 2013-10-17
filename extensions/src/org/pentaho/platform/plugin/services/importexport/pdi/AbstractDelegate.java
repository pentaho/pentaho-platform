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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport.pdi;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.Date;

public abstract class AbstractDelegate {

  protected static final String PROP_NAME = "NAME"; //$NON-NLS-1$

  protected static final String PROP_DESCRIPTION = "DESCRIPTION"; //$NON-NLS-1$

  protected LogChannelInterface log;

  public AbstractDelegate() {
    log = LogChannel.GENERAL;
  }

  protected String getString( DataNode node, String name ) {
    if ( node.hasProperty( name ) ) {
      return node.getProperty( name ).getString();
    } else {
      return null;
    }
  }

  protected long getLong( DataNode node, String name ) {
    if ( node.hasProperty( name ) ) {
      return node.getProperty( name ).getLong();
    } else {
      return 0L;
    }
  }

  protected Date getDate( DataNode node, String name ) {
    if ( node.hasProperty( name ) ) {
      return node.getProperty( name ).getDate();
    } else {
      return null;
    }
  }
}
