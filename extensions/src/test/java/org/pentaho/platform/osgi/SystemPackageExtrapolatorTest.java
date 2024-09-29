/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.osgi;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SystemPackageExtrapolatorTest {

  public static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";

  @Test
  public void testExpandProperties() throws Exception {
    SystemPackageExtrapolator systemPackageExtrapolator = new SystemPackageExtrapolator();
    Properties properties = new Properties();
    properties.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, "org.apache.karaf.branding,"
        + " org.apache.xerces.impl.dv; version=\"2.11.0\","
        + " org.apache.*,org.slf4j.*; version=\"1.7.7\"" );

    Properties outProps = systemPackageExtrapolator.expandProperties( properties );

    String processedPackages = (String) outProps.get( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA );

    assertTrue( "Existing package was deleted", processedPackages
        .contains( "org.apache.xerces.impl.dv; version=\"2.11.0\"" ) );

    assertTrue( "Log4J package should have been added", processedPackages
        .contains( "org.apache.logging.log4j" ) );

    assertTrue( "org.apache package should have been added", processedPackages
        .contains( "org.apache" ) );

    assertTrue( "pentaho package should have been added with versioning", processedPackages
        .contains( "org.slf4j.impl; version=\"1.7.7\"" ) );

  }

  @Test
  public void testJBossProvider() {
    SystemPackageExtrapolator.JBossModulePackageProvider jBossModulePackageProvider
        = new SystemPackageExtrapolator.JBossModulePackageProvider();

    Set<String> packages = jBossModulePackageProvider.getPackages();
    assertThat( packages, is( empty() ) );


    ModuleIdentifier moduleIdentifier = ModuleIdentifier.create( "org.pentaho.test.module" );
    ModuleSpec.Builder builder = ModuleSpec.build( moduleIdentifier );
    builder.addDependency( DependencySpec.createSystemDependencySpec(
            new HashSet<String>( Arrays.asList( "org.apache", "org.apache.logging.log4j" ) ) )
    );

    final ModuleSpec moduleSpec = builder.create();
    ModuleLoader loader = new ModuleLoader( new ModuleFinder[] { new ModuleFinder() {
      @Override public ModuleSpec findModule( ModuleIdentifier moduleIdentifier, ModuleLoader moduleLoader )
          throws ModuleLoadException {
        return moduleSpec;
      }
    } } );

    try {
      Module module = loader.loadModule( moduleIdentifier );
      jBossModulePackageProvider.setModule( module );
    } catch ( ModuleLoadException e ) {
      e.printStackTrace();
    }


    packages = jBossModulePackageProvider.getPackages();
    assertThat( packages, contains( "org.apache.logging.log4j", "org.apache" ) );
    assertThat( packages, not( contains( "org.not.there" ) ) );
  }

  @Test
  public void testGetPackageName() {
    assertEquals( "com.test.name", SystemPackageExtrapolator.getPackageName( new JarEntry( "com/test/name/bob.class" ) ) );
    assertEquals( "com.test.name", SystemPackageExtrapolator.getPackageName( new JarEntry( "com/test/name/" ) ) );
    assertEquals( "", SystemPackageExtrapolator.getPackageName( new JarEntry( "/" ) ) );
    assertEquals( "bob", SystemPackageExtrapolator.getPackageName( new JarEntry( "bob/" ) ) );
    assertEquals( "", SystemPackageExtrapolator.getPackageName( new JarEntry( "bob.class" ) ) );
  }
}