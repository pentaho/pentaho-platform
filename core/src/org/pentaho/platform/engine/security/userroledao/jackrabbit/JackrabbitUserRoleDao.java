package org.pentaho.platform.engine.security.userroledao.jackrabbit;

import java.io.IOException;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.pentaho.platform.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrSystemException;
import org.springframework.extensions.jcr.JcrTemplate;

public class JackrabbitUserRoleDao extends JackrabbitUserRoleService /*implements IUserRoleDao*/  {
  
  NameFactory NF = NameFactoryImpl.getInstance();
  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$
  
  JcrTemplate jcrTemplate;
  
  public JackrabbitUserRoleDao(JcrTemplate jcrTemplate) throws NamespaceException {
    this.jcrTemplate = jcrTemplate;
  }
  
  public void setRoleMembers(final String tenantName, final String roleName, final String[] memberUserNames) {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setRoleMembers(session, tenantName, roleName, memberUserNames);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    } 
  }
  
  public void setRoleMembers(String roleName, String[] memberUserNames) {
    setRoleMembers((String)null, roleName, memberUserNames);
  }
  
  public void setUserRoles(final String tenantName, final String userName, final String[] roles) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setUserRoles(session, tenantName, userName, roles);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    }
  }
  
  public void setUserRoles(String userName, String[] roles) throws NotFoundException, UncategorizedUserRoleDaoException {
    setUserRoles((String)null, userName, roles);
  }
  
  public IPentahoRole createRole(String roleName, String description, String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    return createRole((String)null, roleName, description, memberUserNames);
  }
  
  public IPentahoRole createRole(final String tenantName, final String roleName, final String description, final String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return createRole(session, tenantName, roleName, description, memberUserNames);
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException("Error creating role.", e);
    }
  }
  
  public IPentahoUser createUser(final String tenantName, final String userName, final String password, final String description, final String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    try {
      return (IPentahoUser)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return createUser(session, tenantName, userName, password, description, roles);
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException("Error creating user.", e);
    }
  }

  public IPentahoUser createUser(String userName, String password, String description, String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    return createUser((String)null, userName, password, description, roles);
  }

  public void deleteRole(final IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          deleteRole(session, role);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error deleting role. " + role.getName(), e);
    }
  }

  public void deleteUser(final IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          deleteUser(session, user);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error deleting user. " + user.getUsername(), e);
    }
  }

  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    throw new UnsupportedOperationException();
  }

  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return getUsers(getDefaultTenant());
  }

  public void setRoleDescription(final String tenantName, final String roleName, final String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setRoleDescription(session, tenantName, roleName, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating role.", e);
    }
  }
  
  public void setRoleDescription(String roleName, String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    setRoleDescription((String)null, roleName, description);
  }
  
  public void setUserDescription(final String tenantName, final String userName, final String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setUserDescription(session, tenantName, userName, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error updating user.", e);
    }
  }
  
  public void setUserDescription(String userName, String description) throws NotFoundException, UncategorizedUserRoleDaoException {
    setUserDescription((String)null, userName, description);
  }
  
  public void setPassword(final String tenantName, final String userName, final String password) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setPassword(session, tenantName, userName, password);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException)e.getCause();
      }
      throw new UncategorizedUserRoleDaoException("Error setting user password.", e);
    }
  }
    
  public void setPassword(String userName, String password) throws NotFoundException, UncategorizedUserRoleDaoException {
    setPassword((String)null, userName, password);
  }
  
  public List<IPentahoRole> getRoles(String tenant) throws UncategorizedUserRoleDaoException {
    return getRoles(tenant, false);
  }

  public List<IPentahoRole> getRoles(final String tenant, final boolean includeSubtenants) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRoles(session, tenant, includeSubtenants);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing roles.", e);
    }   
  }
  
  public List<IPentahoUser> getUsers(String tenant) throws UncategorizedUserRoleDaoException {
    return getUsers(tenant, false);
  }

  public List<IPentahoUser> getUsers(final String tenant, final boolean includeSubtenants) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUsers(session, tenant, includeSubtenants);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing users.", e);
    }
  }
  
  public IPentahoRole getRole(String name) throws UncategorizedUserRoleDaoException {
    return getRole((String)null, name);
  }

  public IPentahoRole getRole(final String tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRole(session, tenant, name);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error getting role.", e);
    }
  }
  
  public IPentahoUser getUser(String name) throws UncategorizedUserRoleDaoException {
    return getUser((String)null, name);
  }
  
  public IPentahoUser getUser(final String tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoUser)jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUser(session, tenant, name);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error getting role.", e);
    }
  }
  
  public List<IPentahoUser> getRoleMembers(final String tenantName, final String roleName) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>) jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRoleMembers(session, tenantName, roleName);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing role members.", e);
    }
  }

  public List<IPentahoUser> getRoleMembers(String roleName) throws UncategorizedUserRoleDaoException {
    return getRoleMembers((String)null, roleName);
  }

  public List<IPentahoRole> getUserRoles(final String tenantName, final String userName) throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>) jcrTemplate.execute(new JcrCallback() {      
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUserRoles(session, tenantName, userName);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException("Error listing role members.", e);
    }
  }

  public List<IPentahoRole> getUserRoles(String userName) throws UncategorizedUserRoleDaoException {
    return getUserRoles((String)null, userName);
  }
}

