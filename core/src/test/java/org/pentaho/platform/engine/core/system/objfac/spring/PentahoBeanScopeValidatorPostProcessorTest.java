/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.objfac.spring;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class PentahoBeanScopeValidatorPostProcessorTest extends BaseTest {

  /*
   * The following are bean ids for beans that have no explicit scope declared; these are in files scattered through
   * core/src/test/resources/solution/system ( pentahoObjects.spring.xml, adminPlugins.xml, pentahoSystemConfig.xml, ..
   * )
   */
  private static final String[] BEAN_IDS_WITH_NO_EXPLICIT_SCOPE_DEFINED =
      new String[] { "mimeListenerCollection", "pentahoSystemProxy", "administrationPluginsList" };

  @Override
  public void setUp() {
    /* do nothing */ }

  @Test
  public void testBeanScopeValidation() throws Exception {

    super.setUp(); // to start appCtx work

    // the unit test checking is made in the beanPostProcessor that got registered ( below )
  }

  @Override
  protected ApplicationContext getSpringApplicationContext() {

    GenericApplicationContext appCtx = (GenericApplicationContext) super.getSpringApplicationContext();

    if ( appCtx != null ) {
      appCtx.addBeanFactoryPostProcessor( new PentahoBeanScopeValidatorPostProcessorTester() ); // add our postProcessor
    }

    return appCtx;
  }

  @Override
  public void tearDown() {
    super.finishTest();
    super.tearDown();
  }

  public class PentahoBeanScopeValidatorPostProcessorTester extends PentahoBeanScopeValidatorPostProcessor {

    @Override
    protected void validateBeanScope( String beanName, BeanDefinition bd ) {

      info( "Entered PostProcessor.validateBeanScope() for bean " + beanName );

      boolean validateScopeChange = false;

      if ( Arrays.asList( BEAN_IDS_WITH_NO_EXPLICIT_SCOPE_DEFINED ).contains( beanName ) && bd != null ) {

        info( beanName + " belongs to the list of beans we know to have no explicit scope defined" );
        validateScopeChange = true;
      }

      if ( validateScopeChange ) {
        info( "Before the validation, " + beanName + "'s scope is : '" + bd.getScope() + "'" );
        assertTrue( StringUtils.isEmpty( bd.getScope() ) ); // it should have an empty scope *prior* to the check
      }

      super.validateBeanScope( beanName, bd ); // scope check

      if ( validateScopeChange ) {
        info( "After the validation, " + beanName + "'s scope is : '" + bd.getScope() + "'" );
        assertTrue( BeanDefinition.SCOPE_SINGLETON.equals( bd.getScope() ) ); // it should have been changed
      }
    }
  }
}
