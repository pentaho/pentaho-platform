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
 * @created Apr 27, 2012 
 * @author rmansoor
 */
package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;

/**
 * A tenant of Pentaho platform. Contains name, path and id of a tenant
 * 
 * @author rmansoor
 */

public interface ITenant extends Serializable{

  public Serializable getId();
  
  public String getName();
  
  public String getPath();
  
  public void setId(Serializable id);
  
  public void setName(String name);
  
  public void setPath(String path);
}
