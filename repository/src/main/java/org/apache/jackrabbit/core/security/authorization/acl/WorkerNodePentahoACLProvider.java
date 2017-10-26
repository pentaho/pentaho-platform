/*!
 * Copyright 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.core.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * An implementation of {@link PentahoACLProvider} that does not update the root node acl. Meant for use by the worker
 * nodes, which do not need to update the root node acl, as the update is performed by the main pentaho server.
 */
public class WorkerNodePentahoACLProvider extends PentahoACLProvider {
  private Logger logger = LoggerFactory.getLogger( getClass().getName() );

  @Override
  protected void updateRootAcl( SessionImpl systemSession, ACLEditor editor ) throws RepositoryException {
    logger.debug( "Skipping root object acl update" );
  }
}
