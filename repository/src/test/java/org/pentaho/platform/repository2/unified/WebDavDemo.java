/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
