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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.google.gwt.user.server.rpc.RPCServletUtils;

public class DebugMantleServlet extends HttpServlet {

  private static final long serialVersionUID = -2907930444468382603L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    // use HTTPClient to forward on the data to whatever server we want
    // eg. http://localhost:8080/pentaho/MantleService
    // 1. set the contentType
    // 2. add the data
    // 3. tack the response onto our response
    try {
      HttpClient client = new HttpClient();

      // If server userid/password was supplied, use basic authentication to
      // authenticate with the server.
      Credentials creds = new UsernamePasswordCredentials("joe", "password"); //$NON-NLS-1$ //$NON-NLS-2$
      client.getState().setCredentials(AuthScope.ANY, creds);
      client.getParams().setAuthenticationPreemptive(true);

//      Enumeration attributes = req.getAttributeNames();
//      while (attributes.hasMoreElements()) {
//        System.out.println("Attribute: " + attributes.nextElement());
//      }
//
//      Enumeration params = req.getParameterNames();
//      while (params.hasMoreElements()) {
//        System.out.println("Parameter: " + params.nextElement());
//      }
//
//      Enumeration headers = req.getHeaderNames();
//      while (headers.hasMoreElements()) {
//        String headerName = (String) headers.nextElement();
//        String headerValue = req.getHeader(headerName).replaceAll("8888", "8080");
//        System.out.println("Header: " + headerName + "=" + headerValue);
//        if (!headerName.equals("accept-encoding") && !headerName.equals("content-type") && !"content-length".equals(headerName)) {
//          postMethod.setRequestHeader(headerName, headerValue);
//        }
//      }

      
      String requestPayload = RPCServletUtils.readContentAsUtf8(req);
      System.out.println("INCOMING: " + requestPayload); //$NON-NLS-1$
      requestPayload = requestPayload.replaceAll("8888/mantle", "8080/pentaho/mantle"); //$NON-NLS-1$ //$NON-NLS-2$
      
      PostMethod postMethod = null;
      if (requestPayload.indexOf("MantleLoginService") != -1) { //$NON-NLS-1$
        postMethod = new PostMethod("http://localhost:8080/pentaho/mantleLogin/MantleLoginService?userid=joe&password=password"); //$NON-NLS-1$
      } else if (requestPayload.indexOf("MantleService") != -1) { //$NON-NLS-1$
        postMethod = new PostMethod("http://localhost:8080/pentaho/mantle/MantleService?userid=joe&password=password"); //$NON-NLS-1$
      }
      requestPayload = requestPayload.replaceAll("org.pentaho.mantle.MantleApplication", "pentaho/mantle"); //$NON-NLS-1$ //$NON-NLS-2$
      requestPayload = requestPayload.replaceAll("org.pentaho.mantle.login.MantleLogin", "pentaho/mantleLogin"); //$NON-NLS-1$ //$NON-NLS-2$

      System.out.println("OUTGOING: " + requestPayload); //$NON-NLS-1$
      
      StringRequestEntity stringEntity = new StringRequestEntity(requestPayload, "text/x-gwt-rpc", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
      postMethod.setRequestEntity(stringEntity);

      try {
        @SuppressWarnings("unused")
        int status = client.executeMethod(postMethod);
        String postResult = postMethod.getResponseBodyAsString();
        resp.getOutputStream().write(postResult.getBytes("UTF-8")); //$NON-NLS-1$
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // use HTTPClient to forward on the data to whatever server we want
    // eg. http://localhost:8080/pentaho/MantleService
    // 1. set the contentType
    // 2. add the data
    // 3. tack the response onto our response
    try {
      HttpClient client = new HttpClient();
      GetMethod getMethod = null;

      String passthru = req.getParameter("passthru"); //$NON-NLS-1$

      if (!"".equals(passthru)) { //$NON-NLS-1$
        getMethod = new GetMethod("http://localhost:8080/pentaho/" + passthru); //$NON-NLS-1$
        getMethod.setQueryString(req.getQueryString());
      } else {
        // not known
        resp.setStatus(404);
        return;
      }

      // If server userid/password was supplied, use basic authentication to
      // authenticate with the server.
      Credentials creds = new UsernamePasswordCredentials("joe", "password"); //$NON-NLS-1$ //$NON-NLS-2$
      client.getState().setCredentials(AuthScope.ANY, creds);
      client.getParams().setAuthenticationPreemptive(true);

      try {
        @SuppressWarnings("unused")
        int status = client.executeMethod(getMethod);
        String postResult = getMethod.getResponseBodyAsString();
        resp.getOutputStream().write(postResult.getBytes("UTF-8")); //$NON-NLS-1$
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
    }

  }
}