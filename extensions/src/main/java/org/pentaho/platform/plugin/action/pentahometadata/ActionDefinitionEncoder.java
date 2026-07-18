/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
