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

package org.pentaho.platform.plugin.action.pentahometadata;

/**
 * This class was created to solve the following jira case.
 * 
 * PDB-1122 Space in the filter name causes an XPATH error in charting. http://jira.pentaho.com/browse/PDB-1122
 * 
 * When creating charts on Dashboards with parameters using blank spaces the ActionSequence DOM generation was failing
 * due to those parameters with blank spaces getting used to create xml elements.
 * 
 * Look for references to this class to see where those blank spaces are encoded/decoded to solve this issue.
 * 
 * @author Ezequiel Cuellar
 */

public class ActionDefinitionEncoder {

  public static final String BLANK_SPACE_STR_REPLACEMENT = "_BLNK_";

  public static String encodeBlankSpaces( String input ) {
    return input.replaceAll( " ", BLANK_SPACE_STR_REPLACEMENT );
  }

  public static String decodeBlankSpaces( String input ) {
    return input.replaceAll( BLANK_SPACE_STR_REPLACEMENT, " " );
  }
}
