/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.security;

import java.util.Comparator;

public class DefaultRoleComparator implements Comparator<String> {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private boolean caseSensitive = true;

  // ~ Constructors
  // ====================================================================================================

  // ~ Methods
  // =========================================================================================================

  public DefaultRoleComparator() {
    this( true );
  }

  public DefaultRoleComparator( final boolean caseSensitive ) {
    super();
    this.caseSensitive = caseSensitive;
  }

  public int compare( final String o1, final String o2 ) {
    return caseSensitive ? o1.compareTo( o2 ) : o1.compareToIgnoreCase( o2 );
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive( final boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

}
