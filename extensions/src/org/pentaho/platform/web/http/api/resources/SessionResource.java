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
 * @created May 25, 2011 
 * @author wseyler
 */


package org.pentaho.platform.web.http.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author wseyler
 *
 */
@Path("/session/")
public class SessionResource extends AbstractJaxRSResource {

  @GET
  @Path("/userWorkspaceDir")
  @Produces(TEXT_PLAIN)
  public String doGetCurrentUserDir() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    String value = ClientRepositoryPaths.getUserHomeFolderPath(pentahoSession.getName()) + "/workspace";
    return value;
  }
}
