package org.pentaho.platform.web.http.api.resources.services;

import javax.management.MBeanServer;

public class MBeanRegister {

  private static MBeanServer server;
  public MBeanRegister(MBeanServer server) {
    MBeanRegister.server = server; 
  }
  public static MBeanServer getMBeanServer() {
    return server;
  }
}
