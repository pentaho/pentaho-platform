package org.pentaho.platform.repository2.unified.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;

public class DefaultUserRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
	
	private static final Log logger = LogFactory.getLog(DefaultUserRepositoryLifecycleManager.class);
	private static final ITenant DEFAULT_TENANT = JcrTenantUtils.getDefaultTenant();
	private static final String[] EMPTY_STRING_ARRAY = new String[]{};
	
	private final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;
	private final IPasswordService passwordService;
	
	private final IUserRoleDao userRoleDao;
	private Map<String,List<String>> roleMappings;
	private Map<String,List<String>> userRoleMappings;
	private String password;
	
	public DefaultUserRepositoryLifecycleManager(final IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
			final IPasswordService passwordService,
			final IUserRoleDao userRoleDao) {
		this.roleBindingDao = roleBindingDao;
		this.passwordService = passwordService;
		this.userRoleDao = userRoleDao;
	}

	@Override
	public void newTenant() {
	}

	@Override
	public void newTenant(ITenant arg0) {
	}

	@Override
	public void newUser() {
	}

	@Override
	public void newUser(ITenant arg0, String arg1) {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void startup() {

		configureRoles();
		try {
			configureUsers();
		} catch (PasswordServiceException e) {
			logger.error("Failed configuring users.", e);
		}
	}
	
	private void configureRoles() {

		if (logger.isDebugEnabled()) {
			logger.debug("Configuring default role mappings.");
		}

		for (final String roleName : roleMappings.keySet()) {
			final IPentahoRole role = userRoleDao.getRole(DEFAULT_TENANT, roleName);
			if (role == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Creating user role: " + roleName);
				}
				userRoleDao.createRole(DEFAULT_TENANT, roleName, "", EMPTY_STRING_ARRAY);
				final List<String> logicalRoles = roleMappings.get(roleName);
				if (logicalRoles.size() > 0) {
					roleBindingDao.setRoleBindings(DEFAULT_TENANT, roleName,logicalRoles);
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping config. Role["+roleName+"] already registered.");
				}
			}
		}
	}

	private void configureUsers() throws PasswordServiceException {

		String plainTextPassword = passwordService.decrypt(password);

		for (final String userName : userRoleMappings.keySet()) {

			final IPentahoUser user = userRoleDao.getUser(DEFAULT_TENANT,
					userName);

			if (user == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Creating user: " + userName);
				}

				List<String> roleNames = new ArrayList<String>();

				for (String roleName : userRoleMappings.get(userName)) {

					if (roleMappings.containsKey(roleName)) {
						roleNames.add(roleName);
					} else {
						logger.error("Attempting to map undefined role to user. User["
								+ userName + "] Role[" + roleName + "]");
					}
				}

				userRoleDao.createUser(DEFAULT_TENANT, userName,
						plainTextPassword, "user",
						roleNames.toArray(EMPTY_STRING_ARRAY));

			}
		}
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public Map<String, List<String>> getRoleMappings() {
		return roleMappings;
	}

	public void setRoleMappings(Map<String, List<String>> roleMappings) {
		this.roleMappings = roleMappings;
	}


	public Map<String,List<String>> getUserRoleMappings() {
		return userRoleMappings;
	}


	public void setUserRoleMappings(Map<String,List<String>> userRoleMappings) {
		this.userRoleMappings = userRoleMappings;
	}

}
