package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.pentaho.platform.web.http.api.resources.i18n.Messages;
import org.pentaho.platform.web.http.api.resources.services.Attribute;
import org.pentaho.platform.web.http.api.resources.services.JMXService;
import org.pentaho.platform.web.http.api.resources.services.LDAPServiceException;
import org.pentaho.platform.web.http.api.resources.services.MgmtServicesApplicationContext;
import org.pentaho.platform.web.http.api.resources.services.ServiceMgmt;

@Path("/ldap/config/")
public class LDAPResource extends AbstractJaxRSResource {

  public static final String LDAP_SERVICE_NAME = "LDAP"; //$NON-NLS-1$
  public static final String SERVICE_NAME = "LDAP Service"; //$NON-NLS-1$
  public static final String SERVICE_MANAGEMENT = "servicemgmt"; //$NON-NLS-1$
  public static final Attribute USERNAME = new Attribute(LDAP_SERVICE_NAME, "username"); //$NON-NLS-1$
  public static final Attribute USERNAMEDN = new Attribute(LDAP_SERVICE_NAME, "usernamedn"); //$NON-NLS-1$
  public static final Attribute ROLE_ATTRIBUTE = new Attribute(LDAP_SERVICE_NAME, "allAuthoritiesSearch.roleAttribute"); //$NON-NLS-1$
  public static final Attribute SEARCH_BASE = new Attribute(LDAP_SERVICE_NAME, "allAuthoritiesSearch.searchBase"); //$NON-NLS-1$
  public static final Attribute SEARCH_FILTER = new Attribute(LDAP_SERVICE_NAME, "allAuthoritiesSearch.searchFilter"); //$NON-NLS-1$
  public static final Attribute USER_DN = new Attribute(LDAP_SERVICE_NAME, "contextSource.userDn"); //$NON-NLS-1$
  public static final Attribute PASSWORD = new Attribute(LDAP_SERVICE_NAME, "contextSource.password"); //$NON-NLS-1$
  public static final Attribute PROVIDER_URL = new Attribute(LDAP_SERVICE_NAME, "contextSource.providerUrl"); //$NON-NLS-1$
  public static final Attribute CONVERT_TO_UPPER_CASE = new Attribute(LDAP_SERVICE_NAME,"populator.convertToUpperCase"); //$NON-NLS-1$
  public static final Attribute GROUP_ROLE_ATTRIBUTE = new Attribute(LDAP_SERVICE_NAME, "populator.groupRoleAttribute"); //$NON-NLS-1$
  public static final Attribute GROUP_SEARCH_BASE = new Attribute(LDAP_SERVICE_NAME, "populator.groupSearchBase"); //$NON-NLS-1$
  public static final Attribute GROUP_SEARCH_FILTER = new Attribute(LDAP_SERVICE_NAME, "populator.groupSearchFilter"); //$NON-NLS-1$
  public static final Attribute ROLE_PREFIX = new Attribute(LDAP_SERVICE_NAME, "populator.rolePrefix"); //$NON-NLS-1$
  public static final Attribute SEARCH_SUBTREE = new Attribute(LDAP_SERVICE_NAME, "populator.searchSubtree"); //$NON-NLS-1$
  public static final Attribute USER_SEARCH_BASE = new Attribute(LDAP_SERVICE_NAME, "userSearch.searchBase"); //$NON-NLS-1$
  public static final Attribute USER_SEARCH_FILTER = new Attribute(LDAP_SERVICE_NAME, "userSearch.searchFilter"); //$NON-NLS-1$
  
  public static final String TEST_CONTEXT_SOURCE_OPERATION = "testContextSource";  //$NON-NLS-1$
  public static final String TEST_USER_SEARCH_OPERATION = "testUserSearch";  //$NON-NLS-1$
  public static final String TEST_POPULATOR_OPERATION = "testPopulator";  //$NON-NLS-1$
  public static final String TEST_ALL_AUTHORITIES_SEARCH_OPERATION = "testAllAuthoritiesSearch";  //$NON-NLS-1$
  public static final String SAVE_ALL_AUTHORITIES_SEARCH_OPERATION = "saveAllAuthoritiesSearch";  //$NON-NLS-1$
  public static final String SAVE_CONTEXT_SOURCE_OPERATION = "saveContextSource";  //$NON-NLS-1$
  public static final String SAVE_POPULATOR_OPERATION = "savePopulator";  //$NON-NLS-1$
  public static final String SAVE_USER_SEARCH_OPERATION= "saveUserSearch";  //$NON-NLS-1$
  
  public static final String ERROR_MESSAGE_ATTRIBUTE= "errorMessage";  //$NON-NLS-1$

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(LDAPResource.class);
  JMXService ldapService;

  public LDAPResource() {
    super();
    initialze();
  }

  public void initialze() {
  	if(MgmtServicesApplicationContext.getContext() != null) {
  	    ServiceMgmt serviceMgmt = (ServiceMgmt) MgmtServicesApplicationContext.getContext().getBean("servicemgmt"); //$NON-NLS-1$
  	    ldapService = serviceMgmt.getJmxService(LDAP_SERVICE_NAME);
  	}
  }
  
  @GET
  @Path("/findConfigErrors")
  @Produces(TEXT_PLAIN)
  public String findConfigurationErrors() throws LDAPServiceException{
    Object returnValue = getAttribute(ldapService, ERROR_MESSAGE_ATTRIBUTE);
    if(returnValue != null) {
      return (String) returnValue; 
    } else {
      return null;
    }
  }

  @GET
  @Path("/testContextSource")
  @Produces(TEXT_PLAIN)
  public String testContextSource(Map<Attribute, String> map) throws LDAPServiceException {
    try {
    return ldapService.invoke(TEST_CONTEXT_SOURCE_OPERATION, getParameters(map), getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_CONTEXT_SOURCE_OPERATION)); //$NON-NLS-1$
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_CONTEXT_SOURCE_OPERATION), e);//$NON-NLS-1$    
    }
  }
  
  @PUT
  @Path("/saveContextSource")
  @Produces(TEXT_PLAIN)
  public String saveContextSource(Map<Attribute, String> map) throws LDAPServiceException {
    try {
      Object[] objects = getParameters(map);
      return ldapService.invoke(SAVE_CONTEXT_SOURCE_OPERATION, objects, getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_CONTEXT_SOURCE_OPERATION)); //$NON-NLS-1$
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_CONTEXT_SOURCE_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @GET
  @Path("/testUserSearch")
  @Produces(TEXT_PLAIN)
  public String testUserSearch(Map<Attribute, String> map) throws LDAPServiceException {
    try {
      return ldapService.invoke(TEST_USER_SEARCH_OPERATION, getParameters(map), getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_USER_SEARCH_OPERATION)); //$NON-NLS-1$      
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_USER_SEARCH_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @GET
  @Path("/testPopulator")
  @Produces(TEXT_PLAIN)
  public String testPopulator(Map<Attribute, String> map) throws LDAPServiceException {
    try {  
      return ldapService.invoke(TEST_POPULATOR_OPERATION, getParameters(map), getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_POPULATOR_OPERATION)); //$NON-NLS-1$      
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_POPULATOR_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @GET
  @Path("/testAllAuthoritiesSearch")
  @Produces(TEXT_PLAIN)
  public String testAllAuthoritiesSearch(Map<Attribute, String> map) throws LDAPServiceException {
    try {  
      Object[] objects = getParameters(map);
      return ldapService.invoke(TEST_ALL_AUTHORITIES_SEARCH_OPERATION, objects, getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_ALL_AUTHORITIES_SEARCH_OPERATION)); //$NON-NLS-1$      
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, TEST_ALL_AUTHORITIES_SEARCH_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @PUT
  @Path("/saveAllAuthoritiesSearch")
  @Produces(TEXT_PLAIN)
  public String saveAllAuthoritiesSearch(Map<Attribute, String> map) throws LDAPServiceException {
    try {  
      Object[] objects = getParameters(map);
      return ldapService.invoke(SAVE_ALL_AUTHORITIES_SEARCH_OPERATION, objects, getTypes(map)); 
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_ALL_AUTHORITIES_SEARCH_OPERATION)); //$NON-NLS-1$      
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_ALL_AUTHORITIES_SEARCH_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @PUT
  @Path("/savePopulator")
  @Produces(TEXT_PLAIN)
  public String savePopulator(Map<Attribute, String> map) throws LDAPServiceException {
    try {  
      Object[] objects = getParameters(map);
      return ldapService.invoke(SAVE_POPULATOR_OPERATION, objects, getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_POPULATOR_OPERATION)); //$NON-NLS-1$
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_POPULATOR_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @PUT
  @Path("/saveUserSearch")
  @Produces(TEXT_PLAIN)
  public String saveUserSearch(Map<Attribute, String> map) throws LDAPServiceException {
    try {  
      Object[] objects = getParameters(map);
      return ldapService.invoke(SAVE_USER_SEARCH_OPERATION, objects, getTypes(map));
    } catch(Exception e) {
      logger.error(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_USER_SEARCH_OPERATION)); //$NON-NLS-1$      
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0001_UNABLE_INVOKE_METHOD", SERVICE_NAME, SAVE_USER_SEARCH_OPERATION), e);//$NON-NLS-1$    
    }
  }

  @GET
  @Path("/getRoleAttribute")
  @Produces(TEXT_PLAIN)
  public String getRoleAttribute() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, ROLE_ATTRIBUTE.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setRoleAttribute")
  public Response setRoleAttribute(String roleAttribute) throws LDAPServiceException {
    setAttribute(ldapService, ROLE_ATTRIBUTE.getName(), roleAttribute);
    return Response.ok().build();
  }
  
  
  @GET
  @Path("/getSearchBase")
  @Produces(TEXT_PLAIN)
  public String getSearchBase() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, SEARCH_BASE.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setSearchBase")
  public Response setSearchBase(String searchBase) throws LDAPServiceException {
    setAttribute(ldapService, SEARCH_BASE.getName(), searchBase);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getSearchFilter")
  @Produces(TEXT_PLAIN) 
  public String getSearchFilter() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, SEARCH_FILTER.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setSearchFilter")
  public Response setSearchFilter(String searchFilter) throws LDAPServiceException {
    setAttribute(ldapService, SEARCH_FILTER.getName(), searchFilter);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getManagerDn")
  @Produces(TEXT_PLAIN)   
  public String getManagerDn() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, USER_DN.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  
  @PUT
  @Path("/setManagerDn")
  public Response setManagerDn(String managerDn) throws LDAPServiceException {
    setAttribute(ldapService, USER_DN.getName(), managerDn);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getManagerDn")
  @Produces(TEXT_PLAIN)   
  public String getManagerPassword() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, PASSWORD.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setManagerPassword")
  public Response setManagerPassword(String managerPassword) throws LDAPServiceException {
    setAttribute(ldapService, PASSWORD.getName(), managerPassword);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getProviderUrl")
  @Produces(TEXT_PLAIN)   
  public String getProviderUrl() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, PROVIDER_URL.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  
  @PUT
  @Path("/setProviderUrl")
  public Response setProviderUrl(String providerUrl) throws LDAPServiceException {
    setAttribute(ldapService, PROVIDER_URL.getName(), providerUrl);
    return Response.ok().build();
  }
  
  // Populator
  @GET
  @Path("/getConvertToUpperCase")
  @Produces(TEXT_PLAIN)   
  public Boolean getConvertToUpperCase() throws LDAPServiceException {
	Boolean returnValue = null;
    String value = (String) getAttribute(ldapService, CONVERT_TO_UPPER_CASE.getName());
    if(value != null && value.length() > 0) {
    	returnValue = Boolean.parseBoolean(value);
    } 
    return returnValue; 
  }
  
  @PUT
  @Path("/setConvertToUpperCase")
  public Response setConvertToUpperCase(Boolean convertToUpperCase) throws LDAPServiceException {
    setAttribute(ldapService, CONVERT_TO_UPPER_CASE.getName(), Boolean.toString(convertToUpperCase));
    return Response.ok().build();
  }
  
  @GET
  @Path("/getGroupRoleAttribute")
  @Produces(TEXT_PLAIN)   
  public String getGroupRoleAttribute() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, GROUP_ROLE_ATTRIBUTE.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setGroupRoleAttribute")
  public Response setGroupRoleAttribute(String groupRoleAttribute) throws LDAPServiceException {
    setAttribute(ldapService, GROUP_ROLE_ATTRIBUTE.getName(), groupRoleAttribute);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getGroupSearchBase")
  @Produces(TEXT_PLAIN)     
  public String getGroupSearchBase() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, GROUP_SEARCH_BASE.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setGroupSearchBase")
  public Response setGroupSearchBase(String searchBase) throws LDAPServiceException {
    setAttribute(ldapService, GROUP_SEARCH_BASE.getName(), searchBase);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getGroupSearchFilter")
  @Produces(TEXT_PLAIN)       
  public String getGroupSearchFilter() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, GROUP_SEARCH_FILTER.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setGroupSearchFilter")
  public Response setGroupSearchFilter(String groupSearchFilter) throws LDAPServiceException {
    setAttribute(ldapService, GROUP_SEARCH_FILTER.getName(), groupSearchFilter);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getRolePrefix")
  @Produces(TEXT_PLAIN)       
  public String getRolePrefix() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, ROLE_PREFIX.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setRolePrefix")
  public Response setRolePrefix(String rolePrefix) throws LDAPServiceException {
    setAttribute(ldapService, ROLE_PREFIX.getName(), rolePrefix);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getSearchSubtree")
  @Produces(TEXT_PLAIN)       
  public Boolean getSearchSubtree() throws LDAPServiceException {
    Boolean returnValue = null;
    String value = (String) getAttribute(ldapService, SEARCH_SUBTREE.getName());
    if(value != null && value.length() > 0) {
    	returnValue = Boolean.parseBoolean(value);
    } 
    return returnValue; 
  }
  
  @PUT
  @Path("/setRolePrefix")
  public Response setSearchSubtree(Boolean searchSubtree) throws LDAPServiceException {
    setAttribute(ldapService, SEARCH_SUBTREE.getName(), Boolean.toString(searchSubtree));
    return Response.ok().build();
  }
  
  // User Search
  @GET
  @Path("/getUserSearchBase")
  @Produces(TEXT_PLAIN)       
  public String getUserSearchBase() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, USER_SEARCH_BASE.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setUserSearchBase")
  public Response setUserSearchBase(String searchBase) throws LDAPServiceException {
    setAttribute(ldapService, USER_SEARCH_BASE.getName(), searchBase);
    return Response.ok().build();
  }
  
  @GET
  @Path("/getUserSearchFilter")
  @Produces(TEXT_PLAIN)       
  public String getUserSearchFilter() throws LDAPServiceException {
    String returnValue = null;
    Object obj = getAttribute(ldapService, USER_SEARCH_FILTER.getName());
    if(obj != null) {
      returnValue = (String) obj;
    }
    return returnValue; 
  }
  
  @PUT
  @Path("/setUserSearchFilter")
  public Response setUserSearchFilter(String searchFilter) throws LDAPServiceException {
    setAttribute(ldapService, USER_SEARCH_FILTER.getName(), searchFilter);
    return Response.ok().build();
  }
  
  private void setAttribute(JMXService service, String attributeName, String value) throws LDAPServiceException {
    try {
      service.setAttribute(attributeName, value);
    } catch (Exception e) {
      logger.error(Messages.getErrorString(
          "Service.ERROR_0003_UNABLE_SET_ATTRIBUTE", SERVICE_NAME, attributeName, value)); //$NON-NLS-1$
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0003_UNABLE_SET_ATTRIBUTE", SERVICE_NAME, attributeName, value), e); //$NON-NLS-1$
    }
  }

  private Object getAttribute(JMXService service, String attributeName) throws LDAPServiceException {
    Object returnValue = null;
    try {
      returnValue = service.getAttribute(attributeName);
    } catch (Exception e) {
      logger.error(Messages.getErrorString(
          "Service.ERROR_0002_UNABLE_GET_ATTRIBUTE", SERVICE_NAME, attributeName)); //$NON-NLS-1$
      throw new LDAPServiceException(Messages.getErrorString("Service.ERROR_0002_UNABLE_GET_ATTRIBUTE", SERVICE_NAME, attributeName),  e); //$NON-NLS-1$
    }
    return returnValue;
  }
 
  @GET
  @Path("/getAttributeValues")
  @Produces({ APPLICATION_XML, APPLICATION_JSON })       
  public Map<Attribute, String> getAttributeValues(Attribute[] attributes) throws LDAPServiceException {    
    HashMap<Attribute, String> map = new HashMap<Attribute, String>();
    for (int i = 0; i < attributes.length; i++) {
      Object value = null;
      if (attributes[i].equals(ROLE_ATTRIBUTE)) {
        value = getRoleAttribute();
      } else if (attributes[i].equals(SEARCH_BASE)) {
        value = getSearchBase();
      } else if (attributes[i].equals(SEARCH_FILTER)) {
        value = getSearchFilter();
      } else if (attributes[i].equals(USER_DN)) {
        value = getManagerDn();
      } else if (attributes[i].equals(PASSWORD)) {
        value = getManagerPassword();
      } else if (attributes[i].equals(PROVIDER_URL)) {
        value = getProviderUrl();
      } else if (attributes[i].equals(CONVERT_TO_UPPER_CASE)) {
        value = getConvertToUpperCase();
      } else if (attributes[i].equals(GROUP_ROLE_ATTRIBUTE)) {
        value = getGroupRoleAttribute();
      } else if (attributes[i].equals(GROUP_SEARCH_BASE)) {
        value = getGroupSearchBase();
      } else if (attributes[i].equals(GROUP_SEARCH_FILTER)) {
        value = getGroupSearchFilter();
      } else if (attributes[i].equals(ROLE_PREFIX)) {
        value = getRolePrefix();
      } else if (attributes[i].equals(SEARCH_SUBTREE)) {
        value = getSearchSubtree();
      } else if (attributes[i].equals(USER_SEARCH_BASE)) {
        value = getUserSearchBase();
      } else if (attributes[i].equals(USER_SEARCH_FILTER)) {
        value = getUserSearchFilter();
      }
      map.put(attributes[i], value != null ? value.toString() : null);
    }
    return map;
    
  }
  
  @PUT
  @Path("/setAttributeValues")
  @Produces(TEXT_PLAIN)  
  public Response setAttributeValues(Map<Attribute, String> attributeValues) throws LDAPServiceException {
    for (Map.Entry<Attribute, String> entry : attributeValues.entrySet()) {
      if (entry.getKey().equals(ROLE_ATTRIBUTE)) {
        setRoleAttribute(entry.getValue());
      } else if (entry.getKey().equals(SEARCH_BASE)) {
        setSearchBase(entry.getValue());
      } else if (entry.getKey().equals(SEARCH_FILTER)) {
        setSearchFilter(entry.getValue());
      } else if (entry.getKey().equals(USER_DN)) {
        setManagerDn(entry.getValue());
      } else if (entry.getKey().equals(PASSWORD)) {
        setManagerPassword(entry.getValue());
      } else if (entry.getKey().equals(PROVIDER_URL)) {
        setProviderUrl(entry.getValue());
      } else if (entry.getKey().equals(CONVERT_TO_UPPER_CASE)) {
        if(entry.getValue() != null) {
          setConvertToUpperCase(Boolean.valueOf(entry.getValue()));  
        }
      } else if (entry.getKey().equals(GROUP_ROLE_ATTRIBUTE)) {
        setGroupRoleAttribute(entry.getValue());
      } else if (entry.getKey().equals(GROUP_SEARCH_BASE)) {
        setGroupSearchBase(entry.getValue());
      } else if (entry.getKey().equals(GROUP_SEARCH_FILTER)) {
        setGroupSearchBase(entry.getValue());
      } else if (entry.getKey().equals(ROLE_PREFIX)) {
        setRolePrefix(entry.getValue());
      } else if (entry.getKey().equals(SEARCH_SUBTREE)) {
        if(entry.getValue() != null) {
          setSearchSubtree(Boolean.valueOf(entry.getValue()));
        }
      } else if (entry.getKey().equals(USER_SEARCH_BASE)) {
        setUserSearchBase(entry.getValue());
      } else if (entry.getKey().equals(USER_SEARCH_FILTER)) {
        setUserSearchFilter(entry.getValue());
      }
    }
    return Response.ok().build();
  }
  
  private Object[] getParameters(Map<Attribute, String> attributeValues) {
    
    Object[] objects = new Object[attributeValues.size()];
    try {
    int i=0;
    SortedMap<Attribute, String> sortedMap = new TreeMap<Attribute, String>(new ValueComparer());
    sortedMap.putAll(attributeValues);
    for (Map.Entry<Attribute, String> entry : sortedMap.entrySet()) {
      objects[i++] = entry.getValue();
    }
    }catch(Exception e) {
      e.printStackTrace();
    }
    return objects;
  }

  @SuppressWarnings("unused")
  private String[] getTypes(Map<Attribute, String> attributeValues) {
    String[] objects = new String[attributeValues.size()];
    int i=0;
    for (Map.Entry<Attribute, String> entry : attributeValues.entrySet()) {
      objects[i++] = "java.lang.String"; //$NON-NLS-1$
    }
    return objects;
  }
  
  /** inner class to do sorting of the map **/
  private static class ValueComparer implements Comparator<Attribute> {
      public int compare(Attribute attr1, Attribute attr2) {
           return attr1.getName().compareTo(attr2.getName());
        }
  }

  public void onConfigChanged() {
    initialze();
  }
     
}
