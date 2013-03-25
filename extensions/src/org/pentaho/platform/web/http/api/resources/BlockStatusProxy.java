/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 25, 2013 
 * @author wseyler
 */


package org.pentaho.platform.web.http.api.resources;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author wseyler
 *
 */
@XmlRootElement
public class BlockStatusProxy {
  Boolean totallyBlocked;
  Boolean partiallyBlocked;
  
  public BlockStatusProxy(Boolean totallyBlocked, Boolean partiallyBlocked) {
    super();
    this.totallyBlocked = totallyBlocked;
    this.partiallyBlocked = partiallyBlocked;
  }

  public Boolean getTotallyBlocked() {
    return totallyBlocked;
  }

  public void setTotallyBlocked(Boolean totallyBlocked) {
    this.totallyBlocked = totallyBlocked;
  }

  public Boolean getPartiallyBlocked() {
    return partiallyBlocked;
  }

  public void setPartiallyBlocked(Boolean partiallyBlocked) {
    this.partiallyBlocked = partiallyBlocked;
  }
}
