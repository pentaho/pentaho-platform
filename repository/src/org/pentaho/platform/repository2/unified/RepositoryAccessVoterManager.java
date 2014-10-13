/*  class instead repository.spring.xml changing:
 * 
 *  <bean id="repositoryAccessVoterManager" 
 *  class="org.pentaho.platform.repository2.unified.RepositoryAccessVoterManager">
 *    <constructor-arg ref="authorizationPolicy"/>
 *  </bean>
 * 
 */

package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.springframework.beans.factory.FactoryBean;

public class RepositoryAccessVoterManager implements FactoryBean {

  private IAuthorizationPolicy authorizationPolicy;

  public RepositoryAccessVoterManager( final IAuthorizationPolicy authorizationPolicy,
      final String repositoryAdminUsername ) {
    this.authorizationPolicy = authorizationPolicy;
  }

  @Override
  public Object getObject() throws Exception {
    return new RepositoryAccessVoterManagerInst( authorizationPolicy );
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
