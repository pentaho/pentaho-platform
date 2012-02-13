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


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.web.servlet.MondrianCatalogPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

public class MondrianCatalogPublisherTests extends AbstractMondrianCatalogTestBase {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(MondrianCatalogPublisherTests.class);

  // ~ Instance fields =================================================================================================

  // ~ Methods =========================================================================================================

  @Test
  public void testNoOverwrite() throws Exception {
    MockHttpServletResponse response = simulateRequest(getDefaultMap());
    assertTrue("status \"" + response.getContentAsString() + "\" not valid", isStatusValid(response
        .getContentAsString()));

    assertTrue("expected status=" + ISolutionRepository.FILE_ADD_SUCCESSFUL + ", actual status="
        + response.getContentAsString().trim(), ISolutionRepository.FILE_ADD_SUCCESSFUL == Integer.valueOf(response
        .getContentAsString().trim()));
  }

  @Test
  public void testFileExists() throws Exception {
    Map<String, String> map = getDefaultMap();
    MockHttpServletResponse response = simulateRequest(map);

    assertTrue("status \"" + response.getContentAsString() + "\" not valid", isStatusValid(response
        .getContentAsString()));

    assertTrue("expected status=" + ISolutionRepository.FILE_ADD_SUCCESSFUL + ", actual status="
        + response.getContentAsString().trim(), ISolutionRepository.FILE_ADD_SUCCESSFUL == Integer.valueOf(response
        .getContentAsString().trim()));

    // redo the request
    response = simulateRequest(map);

    assertTrue("status \"" + response.getContentAsString() + "\" not valid", isStatusValid(response
        .getContentAsString()));

    assertTrue("expected status=" + ISolutionRepository.FILE_EXISTS + ", actual status="
        + response.getContentAsString().trim(), ISolutionRepository.FILE_EXISTS == Integer.valueOf(response
        .getContentAsString().trim()));

  }

  @Test
  public void testOverwrite() throws Exception {
    Map<String, String> map = getDefaultMap();
    MockHttpServletResponse response = simulateRequest(map);

    assertTrue("status \"" + response.getContentAsString() + "\" not valid", isStatusValid(response
        .getContentAsString()));

    assertTrue("expected status=" + ISolutionRepository.FILE_ADD_SUCCESSFUL + ", actual status="
        + response.getContentAsString().trim(), ISolutionRepository.FILE_ADD_SUCCESSFUL == Integer.valueOf(response
        .getContentAsString().trim()));

    // redo the request

    map.put("overwrite", "true");
    response = simulateRequest(map);

    assertTrue("status \"" + response.getContentAsString() + "\" not valid", isStatusValid(response
        .getContentAsString()));

    assertTrue("expected status=" + ISolutionRepository.FILE_ADD_SUCCESSFUL + ", actual status="
        + response.getContentAsString().trim(), ISolutionRepository.FILE_ADD_SUCCESSFUL == Integer.valueOf(response
        .getContentAsString().trim()));

  }

  protected MockHttpServletResponse simulateRequest(Map<String, String> map) throws Exception {

    // prepare request
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContentType("multipart/form-data; boundary=---1234"); //$NON-NLS-1$
    request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
    String mondrianSchemaFile = map.get("mondrianSchemaFile");
    if (logger.isDebugEnabled()) {
      logger.debug("uploading mondrian schema file named \"" + mondrianSchemaFile + "\"");
    }
    String content = MessageFormat.format(DEFAULT_CONTENT_TEMPLATE, mondrianSchemaFile, DEFAULT_FILE_CONTENT);
    if (logger.isDebugEnabled()) {
      logger.debug("content=" + content); //$NON-NLS-1$
    }
    request.setContent(content.getBytes("UTF-8")); //$NON-NLS-1$
    request.addParameter("publishPath", map.get("publishPath")); //$NON-NLS-1$ //$NON-NLS-2$
    request.addParameter("publishKey", map.get("publishKey")); //$NON-NLS-1$ //$NON-NLS-2$
    request.addParameter("overwrite", map.get("overwrite")); //$NON-NLS-1$ //$NON-NLS-2$
    request.addParameter("jndiName", map.get("jndiName")); //$NON-NLS-1$ //$NON-NLS-2$

    MockHttpSession httpSession = new MockHttpSession();

    request.setSession(httpSession);

    // prepare response
    MockHttpServletResponse response = new MockHttpServletResponse();

    // prepare mondrian catalog service
    MondrianCatalogHelper catService = new MondrianCatalogHelper();
    catService.setDataSourcesConfig("file:" + destFile.getAbsolutePath()); //$NON-NLS-1$
//    catService.afterPropertiesSet();

    // prepare mondrian catalog publisher
    MondrianCatalogPublisher pub = new MondrianCatalogPublisher();
    pub.setMondrianCatalogService(catService);
    pub.setFullyQualifiedServerURL("http://localhost:8080/pentaho"); //$NON-NLS-1$
//    pub.afterPropertiesSet();

    // process request
   // TODO We need to figure out how to test this . doGet is a protected method now  
    //pub.doGet(request, response); 

    // assertions
    response.getWriter().flush();
    String responseContent = response.getContentAsString();
    if (logger.isDebugEnabled()) {
      logger.debug("response=" + responseContent); //$NON-NLS-1$
    }
    return response;
  }

  protected boolean isStatusValid(final String statusString) {
    int status = -1;
    try {
      status = Integer.valueOf(statusString.trim());
    } catch (NumberFormatException e) {
      return false;
    }
    if (status == ISolutionRepository.FILE_ADD_FAILED
        || status == ISolutionRepository.FILE_ADD_INVALID_PUBLISH_PASSWORD
        || status == ISolutionRepository.FILE_ADD_INVALID_USER_CREDENTIALS
        || status == ISolutionRepository.FILE_ADD_SUCCESSFUL || status == ISolutionRepository.FILE_EXISTS) {
      return true;
    } else {
      return false;
    }
  }

  protected Map<String, String> getDefaultMap() {
    Map<String, String> defaultMap = new HashMap<String, String>();
    defaultMap.put("publishPath", "samples/steel-wheels/analysis");
    // publishKey value is a hash of the word 'password'
    defaultMap.put("publishKey", "b827d867e750adfc0c29114ad863d85c");
    defaultMap.put("mondrianSchemaFile", UUID.randomUUID().toString() + ".mondrian.xml");
    defaultMap.put("overwrite", "false");
    defaultMap.put("jndiName", "Hibernate");
    return defaultMap;

  }
}