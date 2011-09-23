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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.test.platform.plugin.pluginmgr;

import java.util.HashMap;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.plugin.services.pluginmgr.BaseMenuProvider;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.html.HtmlXulLoader;
import org.pentaho.ui.xul.html.IHtmlElement;

public class MenuProvider extends BaseMenuProvider {

	@Override
	protected XulLoader getXulLoader( ) {
		  try {
		      return new HtmlXulLoader();
		  } catch (Exception e) {
			  Logger.error( this.getClass().toString(), "Xul loader could not be created", e); //$NON-NLS-1$
		  }
		  return null;
	}

	public String getMenuBar( String id, String documentPath, IPentahoSession session ) {
		XulMenubar menubar = getXulMenubar( id, documentPath, session );
	    StringBuilder sb = new StringBuilder();
		if( menubar instanceof IHtmlElement ) {
			((IHtmlElement) menubar).getHtml(sb);
		}
		return sb.toString();

	}
	
	public String getPopupMenu( String id, String documentPath, IPentahoSession session ) {
		XulMenupopup popup = getXulPopupMenu( id, documentPath, session );
	    StringBuilder sb = new StringBuilder();
		if( popup instanceof IHtmlElement ) {
			((IHtmlElement) popup).getHtml(sb);
			((IHtmlElement) popup).getScript( new HashMap<String,String>(), sb);
		}
		return sb.toString();
	}

	
}
