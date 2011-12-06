/**
 * 
 */
package org.pentaho.platform.web.http.api.resources.services;

/**
 * @author RMansoor
 * 
 */
public class Attribute implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private String serviceName;

  private String name;
  
  public Attribute() {  
  }
  
  public Attribute(String serviceName, String name) {
    if (serviceName == null || name == null)
      throw new NullPointerException();
    this.serviceName = serviceName;
    this.name = name;
  }

  public String getServiceName() {
    return this.serviceName;
  }

  public String getName() {
    return this.name;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public boolean equals(Object o) {

    if (o instanceof Attribute) {
      Attribute a = (Attribute) o;
      if ((this.serviceName.equals(a.serviceName)) && (this.name.equals(a.name)))
        return true;
    }
    return false;
  }

  public int hashCode() {
    return 31 * serviceName.hashCode() + name.hashCode();
  }

  public String toString() {
    return "SERVICE NAME = " + this.serviceName + " ATTRIBUTE NAME =   " + this.name; //$NON-NLS-1$//$NON-NLS-2$
  }

  public int compareTo(Object o) {
    Attribute n = (Attribute) o;
    int lastCmp = serviceName.compareTo(n.serviceName);
    return (lastCmp != 0 ? lastCmp : name.compareTo(n.name));
  }
}
