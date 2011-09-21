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
 * Copyright 2008-2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.api.ui;

/**
 * @deprecated bi platform api now prefers {@link org.pentaho.ui.xul.IMenuCustomization}
 */
public interface IMenuCustomization {

	public enum CustomizationType { INSERT_BEFORE, INSERT_AFTER, FIRST_CHILD, LAST_CHILD, REPLACE, DELETE };
	
	public enum ItemType { MENU_ITEM, SUBMENU };

	public String getLabel();

	public void setLabel(String label);

	public String getAnchorId();

	public void setAnchorId(String anchorId);

	public String getId();

	public void setId(String id);

	public String getCommand();

	public void setCommand(String command);

	public CustomizationType getCustomizationType();

	public void setCustomizationType( CustomizationType customizationType);

	public ItemType getItemType();

	public void setItemType(ItemType itemType);

}
