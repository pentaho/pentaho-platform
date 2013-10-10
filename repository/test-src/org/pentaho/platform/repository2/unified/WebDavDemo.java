/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified;

//import java.util.Calendar;
//
//import javax.jcr.Node;
//import javax.jcr.Repository;
//import javax.jcr.Session;
//import javax.jcr.SimpleCredentials;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;

/**
 * To run this, put jackrabbit-jcr-rmi-1.5.0.jar on the classpath.
 */
public class WebDavDemo {

  public static void main( final String[] args ) throws Exception {
    // String name = "rmi://localhost:1099/jackrabbit";
    //
    // ClientRepositoryFactory factory = new ClientRepositoryFactory();
    // Repository repository = factory.getRepository(name);
    // Session session = repository.login(new SimpleCredentials("admin", "password".toCharArray()));
    // Node testFolderNode = session.getRootNode().addNode("testbymat", "nt:folder");
    // Node test1Node = testFolderNode.addNode("testfile1", "nt:file");
    // Node test1ResourceNode = test1Node.addNode("jcr:content", "nt:resource");
    // test1ResourceNode.setProperty("jcr:lastModified", Calendar.getInstance());
    // test1ResourceNode.setProperty("jcr:mimeType", "text/plain");
    // test1ResourceNode.setProperty("jcr:data", IOUtils.toInputStream("hello world"));
    //
    // Node test2Node = testFolderNode.addNode("testfile2", "nt:file");
    // Node test2UnstructuredNode = test2Node.addNode("jcr:content", "nt:unstructured");
    // test2UnstructuredNode.setProperty("now", Calendar.getInstance());
    // session.save();
  }

}
