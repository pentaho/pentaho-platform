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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.services.cache.CacheManager;
import org.pentaho.platform.repository2.unified.JackrabbitRepositoryTestBase;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.plugin.UserRoleMapperTest.TestUserRoleListService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring-ext.xml", "classpath:/repository-test-override.spring-ext.xml" })
@SuppressWarnings("nls")
public class MondrianCatalogHelperTest extends JackrabbitRepositoryTestBase implements ApplicationContextAware {

	// ~ Instance fields
	// =================================================================================================

	private MicroPlatform booter;

	private IUnifiedRepository repo;

	private boolean startupCalled;

	public MondrianCatalogHelperTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		// unfortunate reference to superclass
		JackrabbitRepositoryTestBase.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		JackrabbitRepositoryTestBase.tearDownClass();
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		startupCalled = true;

		booter = new MicroPlatform("test-src/solution");
		booter.define(IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL);
		booter.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);
		booter.define(IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL);
		booter.define(IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL);
		booter.define(IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL);
		booter.define(ICacheManager.class, CacheManager.class, Scope.GLOBAL);
		booter.define(IUserRoleListService.class, TestUserRoleListService.class, Scope.GLOBAL);
		booter.defineInstance(IUnifiedRepository.class, repo);
		booter.setSettingsProvider(new SystemSettings());
		booter.start();

		// Clear up the cache
		final ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
		cacheMgr.clearRegionCache(MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		if (startupCalled) {
			manager.shutdown();
		}
		// null out fields to get back memory
		repo = null;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
	}

	@Test
	public void testAddCatalog() throws Exception {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		IPentahoSession session = PentahoSessionHolder.getSession();
		MondrianCatalogHelper helper = (MondrianCatalogHelper) PentahoSystem.get(IMondrianCatalogService.class);

		MondrianSchema schema = new MondrianSchema("SteelWheels", null);
		MondrianDataSource ds = new MondrianDataSource("SteelWheels", "", "", "Provider=mondrian;DataSource=SampleData;", "", "", "", null);
		MondrianCatalog cat = new MondrianCatalog("SteelWheels", "Provider=mondrian;DataSource=SampleData;", "solution:test/charts/steelwheels.mondrian.xml", ds, schema);
		File file = new File("test-src/solution/test/charts/steelwheels.mondrian.xml");
		String mondrianSchema = IOUtils.toString(new FileInputStream(file));
		session.setAttribute("MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema);
		helper.addCatalog(cat, false, session);

		MondrianCatalog catalog = helper.getCatalog("mondrian:/SteelWheels", session);
		Assert.assertNotNull(catalog);
	}

	@Test
	public void testListCatalog() throws Exception {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		IPentahoSession session = PentahoSessionHolder.getSession();
		MondrianCatalogHelper helper = (MondrianCatalogHelper) PentahoSystem.get(IMondrianCatalogService.class);

		MondrianSchema schema1 = new MondrianSchema("SteelWheels", null);
		MondrianDataSource ds1 = new MondrianDataSource("SteelWheels", "", "", "Provider=mondrian;DataSource=SampleData;", "", "", "", null);
		MondrianCatalog cat1 = new MondrianCatalog("SteelWheels", "Provider=mondrian;DataSource=SampleData;", "solution:test/charts/steelwheels.mondrian.xml", ds1, schema1);
		File file1 = new File("test-src/solution/test/charts/steelwheels.mondrian.xml");
		String mondrianSchema1 = IOUtils.toString(new FileInputStream(file1));
		session.setAttribute("MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema1);
		helper.addCatalog(cat1, false, session);

		MondrianSchema schema2 = new MondrianSchema("SampleData", null);
		MondrianDataSource ds2 = new MondrianDataSource("SampleData", "", "", "Provider=mondrian;DataSource=SampleData;", "", "", "", null);
		MondrianCatalog cat2 = new MondrianCatalog("SampleData", "Provider=mondrian;DataSource=SampleData;", "solution:samples/reporting/SampleData.mondrian.xml", ds2, schema2);
		File file2 = new File("test-src/solution/samples/reporting/SampleData.mondrian.xml");
		String mondrianSchema2 = IOUtils.toString(new FileInputStream(file2));
		session.setAttribute("MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema2);
		helper.addCatalog(cat2, false, session);

		List<MondrianCatalog> cats = helper.listCatalogs(session, false);
		Assert.assertEquals(2, cats.size());
	}

	@Test
	public void testRemoveCatalog() throws Exception {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		IPentahoSession session = PentahoSessionHolder.getSession();
		MondrianCatalogHelper helper = (MondrianCatalogHelper) PentahoSystem.get(IMondrianCatalogService.class);

		MondrianSchema schema = new MondrianSchema("SteelWheels", null);
		MondrianDataSource ds = new MondrianDataSource("SteelWheels", "", "", "Provider=mondrian;DataSource=SampleData;", "", "", "", null);
		MondrianCatalog cat = new MondrianCatalog("SteelWheels", "Provider=mondrian;DataSource=SampleData;", "solution:test/charts/steelwheels.mondrian.xml", ds, schema);
		File file = new File("test-src/solution/test/charts/steelwheels.mondrian.xml");
		String mondrianSchema = IOUtils.toString(new FileInputStream(file));
		session.setAttribute("MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema);
		helper.addCatalog(cat, false, session);

		MondrianCatalog catalog = helper.getCatalog("mondrian:/SteelWheels", session);
		Assert.assertNotNull(catalog);

		helper.removeCatalog("mondrian:/SteelWheels", session);
		List<MondrianCatalog> cats = helper.listCatalogs(session, false);
		Assert.assertEquals(0, cats.size());
	}
}
