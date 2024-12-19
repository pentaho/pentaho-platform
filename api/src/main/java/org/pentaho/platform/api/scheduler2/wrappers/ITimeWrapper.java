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


package org.pentaho.platform.api.scheduler2.wrappers;

import org.pentaho.platform.scheduler2.recur.ITimeRecurrence;
import java.util.ArrayList;
import java.util.List;

public abstract class ITimeWrapper {
  protected List<ITimeRecurrence> recurrences = new ArrayList<ITimeRecurrence>();

  public boolean add( ITimeRecurrence recurrence ) {
    return getRecurrences().add( recurrence );
  }

  public void clear() {
    getRecurrences().clear();
  }

  public int size() {
    return getRecurrences().size();
  }

  public ITimeRecurrence get( int index ) {
    return getRecurrences().get( index );
  }

  public abstract List<ITimeRecurrence> getRecurrences();
}
