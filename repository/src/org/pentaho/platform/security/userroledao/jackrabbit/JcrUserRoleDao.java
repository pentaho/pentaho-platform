package org.pentaho.platform.security.userroledao.jackrabbit;

import java.io.IOException;
import java.util.List;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.NotFoundException;
import org.pentaho.platform.api.engine.security.userroledao.UncategorizedUserRoleDaoException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IRepositoryDefaultAclHandler;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.jcr.ILockHelper;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.userroledao.messages.Messages;
import org.springframework.dao.DataAccessException;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.extensions.jcr.JcrSystemException;
import org.springframework.extensions.jcr.JcrTemplate;

public class JcrUserRoleDao extends AbstractJcrBackedUserRoleDao {

  NameFactory NF = NameFactoryImpl.getInstance();

  Name P_PRINCIPAL_NAME = NF.create(Name.NS_REP_URI, "principalName"); //$NON-NLS-1$

  JcrTemplate adminJcrTemplate;

  public JcrUserRoleDao(JcrTemplate adminJcrTemplate, ITenantedPrincipleNameResolver userNameUtils,
      ITenantedPrincipleNameResolver roleNameUtils, String authenticatedRoleName, String tenantAdminRoleName,
      String repositoryAdminUsername, IRepositoryFileAclDao repositoryFileAclDao, IRepositoryFileDao repositoryFileDao, IPathConversionHelper pathConversionHelper,
      ILockHelper  lockHelper, IRepositoryDefaultAclHandler  defaultAclHandler, final List<String> systemRoles)
      throws NamespaceException {
    super(userNameUtils, roleNameUtils, authenticatedRoleName, tenantAdminRoleName, repositoryAdminUsername,
        repositoryFileAclDao, repositoryFileDao, pathConversionHelper, lockHelper, defaultAclHandler, systemRoles);
    this.adminJcrTemplate = adminJcrTemplate;
  }

  @Override
  public void setRoleMembers(final ITenant tenant, final String roleName, final String[] memberUserNames) {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setRoleMembers(session, tenant, roleName, memberUserNames);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }

      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0001_UPDATING_ROLE"), e);
    }
  }

  @Override
  public void setUserRoles(final ITenant tenant, final String userName, final String[] roles) throws NotFoundException,
      UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setUserRoles(session, tenant, userName, roles);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0001_UPDATING_ROLE"), e);
    }
  }

  @Override
  public IPentahoRole createRole(final ITenant tenant, final String roleName, final String description,
      final String[] memberUserNames) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return createRole(session, tenant, roleName, description, memberUserNames);
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0002_CREATING_ROLE"), e);
    }
  }

  @Override
  public IPentahoUser createUser(final ITenant tenant, final String userName, final String password,
      final String description, final String[] roles) throws AlreadyExistsException, UncategorizedUserRoleDaoException {
    final IPentahoUser user;
    try {
      user = (IPentahoUser) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return createUser(session, tenant, userName, password, description, roles);
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof AuthorizableExistsException)) {
        throw new AlreadyExistsException("");
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0003_CREATING_USER"), e);
    }
    return user;
  }

  @Override
  public void deleteRole(final IPentahoRole role) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          deleteRole(session, role);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0004_DELETING_ROLE", role.getName()), e);
    }
  }

  @Override
  public void deleteUser(final IPentahoUser user) throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          deleteUser(session, user);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0005_DELETING_USER", user.getUsername()), e);
    }
  }

  @Override
  public List<IPentahoRole> getRoles() throws UncategorizedUserRoleDaoException {
    return getRoles(JcrTenantUtils.getTenant());
  }

  @Override
  public List<IPentahoUser> getUsers() throws UncategorizedUserRoleDaoException {
    return getUsers(JcrTenantUtils.getTenant());
  }

  @Override
  public void setRoleDescription(final ITenant tenant, final String roleName, final String description)
      throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setRoleDescription(session, tenant, roleName, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0001_UPDATING_ROLE"), e);
    }
  }

  @Override
  public void setUserDescription(final ITenant tenant, final String userName, final String description)
      throws NotFoundException, UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setUserDescription(session, tenant, userName, description);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0006_UPDATING_USER"), e);
    }
  }

  @Override
  public void setPassword(final ITenant tenant, final String userName, final String password) throws NotFoundException,
      UncategorizedUserRoleDaoException {
    try {
      adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          setPassword(session, tenant, userName, password);
          return null;
        }
      });
    } catch (DataAccessException e) {
      if ((e instanceof JcrSystemException) && (e.getCause() instanceof NotFoundException)) {
        throw (NotFoundException) e.getCause();
      }
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0007_UPDATING_USER_PASSWORD"), e);
    }
  }

  @Override
  public List<IPentahoRole> getRoles(ITenant tenant) throws UncategorizedUserRoleDaoException {
    return getRoles(tenant, false);
  }

  @Override
  public List<IPentahoRole> getRoles(final ITenant tenant, final boolean includeSubtenants)
      throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRoles(session, tenant, includeSubtenants);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0008_LISTING_ROLES"), e);
    }
  }

  @Override
  public List<IPentahoUser> getUsers(ITenant tenant) throws UncategorizedUserRoleDaoException {
    return getUsers(tenant, false);
  }

  @Override
  public List<IPentahoUser> getUsers(final ITenant tenant, final boolean includeSubtenants)
      throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUsers(session, tenant, includeSubtenants);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0009_LISTING_USERS"), e);
    }
  }

  @Override
  public IPentahoRole getRole(final ITenant tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoRole) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRole(session, tenant, name);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0010_GETTING_ROLE"), e);    }
  }

  @Override
  public IPentahoUser getUser(final ITenant tenant, final String name) throws UncategorizedUserRoleDaoException {
    try {
      return (IPentahoUser) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUser(session, tenant, name);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0010_GETTING_ROLE"), e);
    }
  }

  @Override
  public List<IPentahoUser> getRoleMembers(final ITenant tenant, final String roleName)
      throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoUser>) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getRoleMembers(session, tenant, roleName);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0011_LISTING_ROLE_MEMBERS"), e);
    }
  }

  @Override
  public List<IPentahoRole> getUserRoles(final ITenant tenant, final String userName)
      throws UncategorizedUserRoleDaoException {
    try {
      return (List<IPentahoRole>) adminJcrTemplate.execute(new JcrCallback() {
        @Override
        public Object doInJcr(Session session) throws IOException, RepositoryException {
          return getUserRoles(session, tenant, userName);
        }
      });
    } catch (DataAccessException e) {
      throw new UncategorizedUserRoleDaoException(Messages.getInstance().getString(
          "JcrUserRoleDao.ERROR_0011_LISTING_ROLE_MEMBERS"), e);
    }
  }
}