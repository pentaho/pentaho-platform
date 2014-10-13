/*  class instead repository.spring.xml changing:
 * 
 *  <bean id="repositoryFileNativeDao" class="org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileDao">
 *   <constructor-arg ref="transformers"/>
 *    <constructor-arg ref="ILockHelper"/> 
 *    <constructor-arg>
 *      <bean class="org.pentaho.platform.repository2.unified.jcr.DefaultDeleteHelper">
 *        <constructor-arg ref="ILockHelper"/>
 *        <constructor-arg ref="pathConversionHelper"/>
 *      </bean>
 *    </constructor-arg>
 *    <constructor-arg ref="pathConversionHelper"/>
 *    <constructor-arg ref="repositoryFileAclDao"/>
 *    <constructor-arg ref="defaultAclHandler"/>
 *    <constructor-arg ref="repositoryAccessVoterManager"/>
 *    <constructor-arg ref="repositoryAdminUsername"/>    
 *  </bean>
 *
 *  <bean id="repositoryFileDao" class="org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileDaoFacade">
 *      <constructor-arg ref="jcrTemplate"/>
 *      <constructor-arg ref="repositoryFileNativeDao"/>
 *  </bean>
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoterManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;

import java.util.List;

public class JcrRepositoryFileDao implements FactoryBean, ApplicationContextAware {

  private JcrTemplate jcrTemplate;

  private List<ITransformer<IRepositoryFileData>> transformers;

  private ILockHelper lockHelper;

  private IDeleteHelper deleteHelper;

  private IPathConversionHelper pathConversionHelper;

  private IRepositoryFileAclDao aclDao;

  private IRepositoryDefaultAclHandler defaultAclHandler;

  private IRepositoryAccessVoterManager accessVoterManager;

  private String repositoryAdminUsername;

  public JcrRepositoryFileDao( JcrTemplate jcrTemplate, final List<ITransformer<IRepositoryFileData>> transformers,
      final ILockHelper lockHelper, final IDeleteHelper deleteHelper, final IPathConversionHelper pathConversionHelper,
      final IRepositoryFileAclDao aclDao, final IRepositoryDefaultAclHandler defaultAclHandler,
      final IRepositoryAccessVoterManager accessVoterManager ) {
    this.jcrTemplate = jcrTemplate;
    this.transformers = transformers;
    this.lockHelper = lockHelper;
    this.deleteHelper = deleteHelper;
    this.pathConversionHelper = pathConversionHelper;
    this.aclDao = aclDao;
    this.defaultAclHandler = defaultAclHandler;
    this.accessVoterManager = accessVoterManager;
  }

  @Override
  public Object getObject() throws Exception {
    JcrRepositoryFileDaoInst dao =
        new JcrRepositoryFileDaoInst( transformers, lockHelper, deleteHelper, pathConversionHelper, aclDao,
            defaultAclHandler, accessVoterManager, repositoryAdminUsername );
    return new JcrRepositoryFileDaoFacade( jcrTemplate, dao );
  }

  @Override
  public Class<?> getObjectType() {
    return JcrRepositoryFileDaoFacade.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void setApplicationContext( ApplicationContext context ) throws BeansException {
    repositoryAdminUsername = (String) context.getBean( "repositoryAdminUsername" );
  }
}
