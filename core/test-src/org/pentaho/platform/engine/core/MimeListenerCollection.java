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

package org.pentaho.platform.engine.core;

import java.util.List;

/**
 * User: nbaker Date: 3/2/13
 */
public class MimeListenerCollection {
  private List<MimeTypeListener> listeners;
  private MimeTypeListener highestListener;
  private MimeTypeListener queriedBean;
  private List<MimeTypeListener> queriedList;

  public List<MimeTypeListener> getListeners() {
    return listeners;
  }

  public void setListeners( List<MimeTypeListener> listeners ) {
    this.listeners = listeners;
  }

  public MimeTypeListener getHighestListener() {
    return highestListener;
  }

  public void setHighestListener( MimeTypeListener highestListener ) {
    this.highestListener = highestListener;
  }

  public MimeTypeListener getQueriedBean() {
    return queriedBean;
  }

  public void setQueriedBean( MimeTypeListener queriedBean ) {
    this.queriedBean = queriedBean;
  }

  public List<MimeTypeListener> getQueriedList() {
    return queriedList;
  }

  public void setQueriedList( List<MimeTypeListener> queriedList ) {
    this.queriedList = queriedList;
  }
}
