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

package org.pentaho.platform.api.engine.security.userroledao;

import org.pentaho.platform.api.mt.ITenant;

import java.io.Serializable;

/**
 * A user of the Pentaho platform. Contains a set of roles for which this user is a member.
 * 
 * @author mlowery
 */
public interface IPentahoUser extends Serializable {

  String getUsername();

  ITenant getTenant();

  String getPassword();

  void setPassword( String password );

  boolean isEnabled();

  void setEnabled( boolean enabled );

  String getDescription();

  void setDescription( String description );
}
