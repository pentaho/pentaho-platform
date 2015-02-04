package org.pentaho.platform.osgi;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class SystemPackageExtrapolatorTest {

  public static final String ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA = "org.osgi.framework.system.packages.extra";

  @Test
  public void testExpandProperties() throws Exception {
    SystemPackageExtrapolator systemPackageExtrapolator = new SystemPackageExtrapolator();
    Properties properties = new Properties(  );
    properties.setProperty( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA, "org.apache.karaf.branding,"
        + " org.apache.xerces.impl.dv; version=\"2.11.0\","
        + " org.apache.*,org.slf4j.*; version=\"1.7.7\"" );

    Properties outProps = systemPackageExtrapolator.expandProperties( properties );

    String processedPackages = (String) outProps.get( ORG_OSGI_FRAMEWORK_SYSTEM_PACKAGES_EXTRA );

    assertTrue( "Existing package was deleted", processedPackages
        .contains( "org.apache.xerces.impl.dv; version=\"2.11.0\"" ) );

    assertTrue( "Log4J package should have been added", processedPackages
        .contains( "org.apache.log4j" ) );

    assertTrue( "pentaho package should have been added with versioning", processedPackages
        .contains( "org.slf4j.impl; version=\"1.7.7\"" ) );

  }
}