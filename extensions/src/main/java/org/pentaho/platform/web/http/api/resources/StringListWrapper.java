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


package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
public class StringListWrapper {
  List<String> strings = new ArrayList<String>();

  public StringListWrapper() {
  }

  public StringListWrapper( Collection<String> stringList ) {
    this.strings.addAll( stringList );
  }

  public List<String> getStrings() {
    return strings;
  }

  public void setStrings( List<String> stringList ) {
    if ( stringList != this.strings ) {
      this.strings.clear();
      this.strings.addAll( stringList );
    }
  }
}
