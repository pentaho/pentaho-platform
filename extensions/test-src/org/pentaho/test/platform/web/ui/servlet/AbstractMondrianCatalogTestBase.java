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
package org.pentaho.test.platform.web.ui.servlet;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

/**
 * Superclass of tests for IMondrianCatalogService and MondrianCatalogPublisher instances.
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public abstract class AbstractMondrianCatalogTestBase extends BaseTest {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(AbstractMondrianCatalogTestBase.class);

  // ~ Instance fields =================================================================================================

  protected static final String DEFAULT_CONTENT_TEMPLATE = "-----1234\r\n" + "Content-Disposition: form-data; " //$NON-NLS-1$//$NON-NLS-2$
      + "name=\"file\"; " + "filename=\"{0}\"\r\n" //$NON-NLS-1$ //$NON-NLS-2$
      + "Content-Type: text/xml\r\n\r\n{1}\n\r\n" + "-----1234--\r\n"; //$NON-NLS-1$ //$NON-NLS-2$

  protected File destFile;

  protected static final String DEFAULT_FILENAME = "foo11.mondrian.xml"; //$NON-NLS-1$

  protected static final String DEFAULT_FILE_CONTENT = "<?xml version=\"1.0\"?><Schema name=\"Foo\" />"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================
  private static final String SOLUTION_PATH = "test-src/web-servlet-solution";

  private static final String ALT_SOLUTION_PATH = "test-src/web-servlet-solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }
  public AbstractMondrianCatalogTestBase() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Override
  public void setUp() {
    super.setUp();
    setUpTempFile();
    SecurityHelper.getInstance().becomeUser("admin");
  }

  /**
   * Makes a copy of the test-datasources.xml so the test can write to it and muck it up.
   */
  protected void setUpTempFile() {
    InputStream src = this.getClass().getResourceAsStream("/org/pentaho/test/platform/web/ui/servlet/test-datasources.xml");
    OutputStream dest = null;
    try {
      destFile = File.createTempFile("test-datasources", ".xml");
      dest = new FileOutputStream(destFile);
      IOUtils.copy(src, dest);
    } catch (FileNotFoundException e) {
      if (logger.isErrorEnabled()) {
        logger.error("an exception occurred", e);
      }
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("an exception occurred", e);
      }
    }
    IOUtils.closeQuietly(src);
    IOUtils.closeQuietly(dest);
  }

  @Override
  public void tearDown() {
    super.tearDown();
    if (null != destFile) {
      FileUtils.deleteQuietly(destFile);
    }
  }

}