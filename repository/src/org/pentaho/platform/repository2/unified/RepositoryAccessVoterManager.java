/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 *
 *   class instead repository.spring.xml changing:
 * 
 *  <bean id="repositoryAccessVoterManager" 
 *  class="org.pentaho.platform.repository2.unified.RepositoryAccessVoterManager">
 *    <constructor-arg ref="authorizationPolicy"/>
 *  </bean>
 * 
 */

package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.springframework.beans.factory.FactoryBean;

import java.util.Collections;
import java.util.List;

public class RepositoryAccessVoterManager implements FactoryBean {

  private List<IRepositoryAccessVoter> voters;
  private IAuthorizationPolicy authorizationPolicy;

  public RepositoryAccessVoterManager( final IAuthorizationPolicy authorizationPolicy,
      final String repositoryAdminUsername ) {
    this( Collections.<IRepositoryAccessVoter>emptyList(), authorizationPolicy, repositoryAdminUsername );
  }


  public RepositoryAccessVoterManager( final List<IRepositoryAccessVoter> voters,
                                       final IAuthorizationPolicy authorizationPolicy, final String repositoryAdminUsername ) {
    this.voters = voters;
    this.authorizationPolicy = authorizationPolicy;
  }

  @Override
  public Object getObject() throws Exception {
    return new RepositoryAccessVoterManagerInst( voters, authorizationPolicy );
  }

  @Override
  public Class<?> getObjectType() {
    return RepositoryAccessVoterManagerInst.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
