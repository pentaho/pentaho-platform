/*
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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Sept 15, 2008 
 * @author jdixon
 */

package org.pentaho.platform.api.ui;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * An interface for menu providers
 * @author jamesdixon
 *
 */
public interface IMenuProvider {

	/**
	 * Returns a menu bar object. The type of the object returned will depend upon the 
	 * implementor.
	 * @param id The id of the menu to be returned. The menu will to be defined
	 * in a XUL file e.g. pentaho-solutions/system/ui/menubar.xul
	 * @param documentPath The path to the file that contains the definition of
	 * the menu e.g. system/ui/menubar.xul
	 * @param session A session object that the IMenuProvider can use to filter 
	 * the menu items returned
	 * @return A menu bar object
	 */
	public Object getMenuBar( String id, String documentPath, IPentahoSession session );
	
	/**
	 * Returns a popup menu object. The type of the object returned will depend upon the 
	 * implementor.
	 * @param id The id of the menu to be returned. The menu will to be defined
	 * in a XUL file e.g. pentaho-solutions/system/ui/menubar.xul
	 * @param documentPath The path to the file that contains the definition of
	 * the menu e.g. system/ui/menubar.xul
	 * @param session A session object that the IMenuProvider can use to filter 
	 * the menu items returned
	 * @return A menu bar object
	 */
	public Object getPopupMenu( String id, String documentPath, IPentahoSession session );
	
}
