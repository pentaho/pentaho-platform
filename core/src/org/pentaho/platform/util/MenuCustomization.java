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
 * Copyright 2008-2009 Pentaho Corporation. All rights reserved.
 *
*/
package org.pentaho.platform.util;

import org.pentaho.platform.api.ui.IMenuCustomization;

public class MenuCustomization implements IMenuCustomization {

	private String anchorId;
	
	private String id;
	
	private String command;
	
	private CustomizationType customizationType = CustomizationType.INSERT_BEFORE;
	
	private ItemType itemType = ItemType.MENU_ITEM;
	
	private String label = ""; //$NON-NLS-1$

	public MenuCustomization() {
		
	}
	
	public MenuCustomization( String id, String anchorId, String label, String command, ItemType itemType, CustomizationType customizationType ) {
		this.id = id;
		this.anchorId = anchorId;
		this.label = label;
		this.command = command;
		this.itemType = itemType;
		this.customizationType = customizationType;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAnchorId() {
		return anchorId;
	}

	public void setAnchorId(String anchorId) {
		this.anchorId = anchorId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public CustomizationType getCustomizationType() {
		return customizationType;
	}

	public void setCustomizationType(CustomizationType customizationType) {
		this.customizationType = customizationType;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}
}
