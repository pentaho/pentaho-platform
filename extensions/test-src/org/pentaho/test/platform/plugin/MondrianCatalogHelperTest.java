/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import mondrian.xmla.DataSourcesConfig.DataSource;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IAclPublisher;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.acls.AclPublisher;
import org.pentaho.platform.engine.security.acls.voter.PentahoBasicAclVoter;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class MondrianCatalogHelperTest {

  private MicroPlatform microPlatform;

  /**
   * Makes a copy of the test-datasources.xml so the test can write to it and muck it up.
   */
  public File setUpTempFile() {
    InputStream src = null;
    OutputStream dest = null;
    File destFile = null;
    try {
      src = new FileInputStream(PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/test-helper-datasources.xml"));
      destFile = File.createTempFile("test-helper-datasources", ".xml");
      dest = new FileOutputStream(destFile);
      IOUtils.copy(src, dest);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    IOUtils.closeQuietly(src);
    IOUtils.closeQuietly(dest);
    return destFile;
  }
  
  /**
   * Makes a copy of the test-empty-datasources.xml so the test can write to it and muck it up.
   */
  public File setUpEmptyTempFile() {
    InputStream src = null;
    OutputStream dest = null;
    File destFile = null;
    try {
      src = new FileInputStream(PentahoSystem.getApplicationContext().getSolutionPath("test/analysis/test-empty-datasources.xml"));
      destFile = File.createTempFile("test-empty-datasources", ".xml");
      dest = new FileOutputStream(destFile);
      IOUtils.copy(src, dest);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    IOUtils.closeQuietly(src);
    IOUtils.closeQuietly(dest);
    return destFile;
  }
  
  @Before
  public void init0() {
    microPlatform = new MicroPlatform("test-src/solution");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL);
    microPlatform.define("connection-SQL", SQLConnection.class);
    microPlatform.define("connection-MDX", MDXConnection.class);
    microPlatform.define(IDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
    microPlatform.define(IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL);
    microPlatform.setSettingsProvider(new SystemSettings());
    try {
      microPlatform.start();
    } catch (PlatformInitializationException ex) {
      Assert.fail();
    }
    File destFile = setUpTempFile();
    MondrianCatalogHelper catalogService = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    catalogService.setDataSourcesConfig("file:" + destFile.getAbsolutePath());
    catalogService.setUseSchemaNameAsCatalogName(false);
    // JNDI
    System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory");
    System.setProperty("org.osjava.sj.root", "test-src/solution/system/simple-jndi");
    System.setProperty("org.osjava.sj.delimiter", "/");
    
    // Clear up the cache
    final ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
    cacheMgr.clearRegionCache(MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION);
  }
  
  @Test
  public void testEmptydatasources() throws Exception {
    File destFile = setUpEmptyTempFile();
    MondrianCatalogHelper catalogService = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    catalogService.setDataSourcesConfig("file:" + destFile.getAbsolutePath());
    catalogService.setUseSchemaNameAsCatalogName(false);
    final MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    List<MondrianCatalog> list = SecurityHelper.runAsUser("suzy", new Callable<List<MondrianCatalog>>() {
      @Override
      public List<MondrianCatalog> call() throws Exception {
        return helper.listCatalogs(PentahoSessionHolder.getSession(), false);
      }
    });
    
    
    Assert.assertEquals(0, list.size());
  }
  
  @Test
  public void testGetCatalog() throws Exception {
    final MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);;
    MondrianCatalog mc = SecurityHelper.runAsUser("joe", new Callable<MondrianCatalog>() {
      @Override
      public MondrianCatalog call() throws Exception {
        return helper.getCatalog("SteelWheels3", PentahoSessionHolder.getSession());
      }
    });
    Assert.assertNotNull(mc);
  }
  
  @Test
  public void testRemoveCatalog() throws Exception {
    SecurityHelper.runAsUser("joe", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        IPentahoSession session = PentahoSessionHolder.getSession();
      final MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);

    // add a new catalog with a new schema file
    MondrianSchema schema = new MondrianSchema("ToRemoveCatalog", null);
    MondrianDataSource ds = new MondrianDataSource(
        "Provider=Mondrian;DataSource=Pentaho",
        "Pentaho BI Platform Datasources",
        "http://localhost:8080/pentaho/Xmla?userid=joe&amp;password=password", 
        "Provider=Mondrian", // no default jndi datasource should be specified
        "PentahoXMLA", 
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
      );

    FileInputStream fis = new FileInputStream(PentahoSystem.getApplicationContext().getSolutionPath("test/charts/steelwheels.mondrian.xml"));
    File file = new File(PentahoSystem.getApplicationContext().getSolutionPath("test/charts/steelwheels2.mondrian.xml"));
    FileOutputStream fos = new FileOutputStream(file);
    IOUtils.copy(fis, fos);
    
    Assert.assertTrue(file.exists());
    
    MondrianCatalog cat = new MondrianCatalog("ToRemoveCatalog", "Provider=mondrian;DataSource=SampleDataTest",
        "solution:test/charts/steelwheels2.mondrian.xml", ds, schema);



        helper.addCatalog(cat, true, session);
        List<MondrianCatalog> cats = helper.listCatalogs(session, false);
        Assert.assertEquals(5, cats.size());
        
        helper.removeCatalog("ToRemoveCatalog", session);

        cats = helper.listCatalogs(session, false);

        Assert.assertEquals(4, cats.size());
        
        Assert.assertTrue(!file.exists());
        return null;
      }
      
    });
    
  }

  @Test
  public void testListCatalogs() throws Exception {
    final MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    List<MondrianCatalog> cats = SecurityHelper.runAsUser("joe", new Callable<List<MondrianCatalog>>() {

      @Override
      public List<MondrianCatalog> call() throws Exception {
        return helper.listCatalogs(PentahoSessionHolder.getSession(), false);
      }
      
    });
    Assert.assertEquals(4, cats.size());
  }

  @Test
  public void testJndiOnly() throws Exception {
    final MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    List<MondrianCatalog> cats = SecurityHelper.runAsUser("joe", new Callable<List<MondrianCatalog>>() {

      @Override
      public List<MondrianCatalog> call() throws Exception {
        return helper.listCatalogs(PentahoSessionHolder.getSession(), true);
      }
      
    });
     
    Assert.assertEquals(2, cats.size());
  }

  @Test
  public void testListRestrictedCatalogs() throws Exception {

    // Init the micro platform for Db based repo.
    microPlatform.stop();
    microPlatform = new MicroPlatform("test-src/solution");
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(ISolutionRepository.class, DbBasedSolutionRepository.class);
    microPlatform.define(IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL);
    microPlatform.define(IAclPublisher.class, AclPublisher.class);
    microPlatform.define(IAclVoter.class, PentahoBasicAclVoter.class);
    microPlatform.define("connection-SQL", SQLConnection.class);
    microPlatform.define("connection-MDX", MDXConnection.class);
    microPlatform.define(IDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
    microPlatform.define(IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL);
    microPlatform.setSettingsProvider(new SystemSettings());
    try {
      microPlatform.start();
    } catch (PlatformInitializationException ex) {
      Assert.fail("Failed to start the micro platform.");
    }
    File destFile = setUpTempFile();
    MondrianCatalogHelper catalogService = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);
    catalogService.setDataSourcesConfig("file:" + destFile.getAbsolutePath());
    catalogService.setUseSchemaNameAsCatalogName(false);
    
    SecurityHelper.becomeUser("joe");
    final ISolutionRepository repo = 
      PentahoSystem.get(ISolutionRepository.class);
    final MondrianCatalogHelper helper = 
      (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);

    // Add an entry to the datasources.
    final MondrianSchema schema = 
      new MondrianSchema("testListRestrictedCatalogs-schema", null);
    final MondrianDataSource ds = 
      new MondrianDataSource(
        "Provider=Mondrian;DataSource=Pentaho",
        "Pentaho BI Platform Datasources",
        "http://localhost:8080/pentaho/Xmla?userid=joe&amp;password=password", 
        "Provider=Mondrian",
        "PentahoXMLA",
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
      );
    final MondrianCatalog cat =
      new MondrianCatalog(
        "testListRestrictedCatalogs-catalog", 
        "Provider=mondrian;DataSource=SampleDataTest",
        "solution:security/steelwheels.mondrian.xml",
        ds, 
        schema);
    helper.addCatalog(
      cat, 
      true, 
      PentahoSessionHolder.getSession());
    Assert.assertEquals(
      5,
      helper.listCatalogs(PentahoSessionHolder.getSession(), false).size());
    
    // Now share the catalog with Suzy, but not Tiffany
    final IPermissionRecipient recipientSuzy = 
      new SimpleUser("suzy"); //$NON-NLS-1$
    final IPermissionMask maskSuzy = 
      new SimplePermissionMask(ISolutionRepository.ACTION_EXECUTE);
    final Map<IPermissionRecipient, IPermissionMask> acl = 
      new HashMap<IPermissionRecipient, IPermissionMask>();
    acl.put(recipientSuzy, maskSuzy);
    repo.setPermissions(
      repo.getSolutionFile(
        "security",
        ISolutionRepository.ACTION_ADMIN), 
      acl);    
    
    // Validate the access rights
    Map<IPermissionRecipient, IPermissionMask> effectivePermissions = 
      repo.getEffectivePermissions(
        repo.getSolutionFile(
          "security",
          ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$)
    Assert.assertEquals(
      "{SimpleUser[userName=suzy]=SimplePermissionMask[permissionMask=1]}",
      effectivePermissions.toString());
    
    // Try to read it with Suzy's account
    SecurityHelper.becomeUser("suzy");
    final MondrianCatalog suzyCatalog =
      helper.getCatalog(
        "testListRestrictedCatalogs-catalog",
        PentahoSessionHolder.getSession());
    Assert.assertNotNull(suzyCatalog);
    Assert.assertEquals(
      "testListRestrictedCatalogs-catalog",
      suzyCatalog.getName());
    Assert.assertEquals(
        5,
        helper.listCatalogs(PentahoSessionHolder.getSession(), false).size());
    
    // Now try with Tiffany's. It has to fail.
    SecurityHelper.becomeUser("tiffany");
    Assert.assertNull(
      helper.getCatalog(
        "testListRestrictedCatalogs-catalog",
        PentahoSessionHolder.getSession()));
    Assert.assertEquals(
      4,
      helper.listCatalogs(PentahoSessionHolder.getSession(), false).size());
  }

  @Test
  public void testAddCatalogNoOverwrite() throws Exception {
    SecurityHelper.runAsUser("joe", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);

        MondrianSchema schema = new MondrianSchema("SteelWheels3", null);
        MondrianDataSource ds = new MondrianDataSource("SteelWheels3", "", "", "Provider=mondrian;DataSource=SampleData;",
            "", "", "", null);

        MondrianCatalog cat = new MondrianCatalog("SteelWheels3", null,
            "solution:test/charts/steelwheels.mondrian.xml", ds, schema);

        try {
          helper.addCatalog(cat, false, PentahoSessionHolder.getSession());
          Assert.fail("expected exception");
        } catch (MondrianCatalogServiceException e) {
          Assert.assertEquals(MondrianCatalogServiceException.Reason.ALREADY_EXISTS, e.getReason());
        }
        List<MondrianCatalog> cats = helper.listCatalogs(PentahoSessionHolder.getSession(), false);
        Assert.assertEquals(4, cats.size());
        return null;  
      }
    });
  }
  
  @Test
  public void testAddCatalogOverwrite() throws Exception {
    SecurityHelper.runAsUser("joe", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
    MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);

    MondrianSchema schema = new MondrianSchema("SteelWheels3", null);
    MondrianDataSource ds = new MondrianDataSource(
        "Provider=Mondrian;DataSource=Pentaho",
        "Pentaho BI Platform Datasources",
        "http://localhost:8080/pentaho/Xmla?userid=joe&amp;password=password", 
        "Provider=Mondrian", // no default jndi datasource should be specified
        "PentahoXMLA", 
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
     );

    MondrianCatalog cat = new MondrianCatalog("SteelWheels3", "Provider=mondrian;DataSource=SampleDataTest",
        "solution:test/charts/steelwheels.mondrian.xml", ds, schema);

    helper.addCatalog(cat, true, PentahoSessionHolder.getSession());
    List<MondrianCatalog> cats = helper.listCatalogs(PentahoSessionHolder.getSession(), false);
    Assert.assertEquals(4, cats.size());
    
    MondrianCatalog catalog = helper.getCatalog("SteelWheels3", PentahoSessionHolder.getSession());
    Assert.assertEquals("Provider=mondrian;DataSource=SampleDataTest", catalog.getDataSourceInfo());
    return null;
      }
    });
  }
  
  @Test
  public void testAddCatalogLeadingSlash() throws Exception {
    SecurityHelper.runAsUser("joe", new Callable<Void>() {
      @Override
      public Void call() throws Exception {
    MondrianCatalogHelper helper = (MondrianCatalogHelper)PentahoSystem.get(IMondrianCatalogService.class);

    MondrianSchema schema = new MondrianSchema("SteelWheels4", null);
    MondrianDataSource ds = new MondrianDataSource(
        "Provider=Mondrian;DataSource=SteelWheels4",
        "Pentaho BI Platform Datasources",
        "http://localhost:8080/pentaho/Xmla?userid=joe&amp;password=password", 
        "Provider=Mondrian", // no default jndi datasource should be specified
        "PentahoXMLA", 
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
      );

    MondrianCatalog cat = new MondrianCatalog("SteelWheels4", "Provider=mondrian;DataSource=SampleData;",
        "solution:/test/charts/steelwheels.mondrian.xml", ds, schema);

    helper.addCatalog(cat, true, PentahoSessionHolder.getSession());
    List<MondrianCatalog> cats = helper.listCatalogs(PentahoSessionHolder.getSession(), false);
    Assert.assertEquals(4, cats.size());
    
    MondrianCatalog catalog = helper.getCatalog("SteelWheels4", PentahoSessionHolder.getSession());
    Assert.assertEquals("Provider=mondrian;DataSource=SampleData;", catalog.getDataSourceInfo());
    return null;
  }
});
  }
  
  
  public static class TestUserRoleListService implements IUserRoleListService {

    @Override
    public List<String> getAllRoles() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAllUsers() {
      throw new UnsupportedOperationException();  
    }

    @Override
    public List<String> getUsersInRole(String role) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getRolesForUser(String username) {
      if (username.equals("joe")) {
        return Arrays.asList(new String[] { "ceo", "Admin", "Authenticated" });
      } else if (username.equals("suzy")) {
        return Arrays.asList(new String[] { "Authenticated" });
      } else if (username.equals("tiffany")) {
        return Arrays.asList(new String[] { "Authenticated" });
      } else if (username.equals("pat")) {
        return Arrays.asList(new String[0]);
      }
      return Collections.emptyList();  
    }
    
  }

}