package org.pentaho.platform.api.repository2.unified;

import org.pentaho.platform.api.mt.ITenant;

/**
 * Allows external code to do initialization work on the backing repository at certain lifecycle milestones. An example
 * of a backing repository is JCR. Note that there is no code dependency between this interface and 
 * {@code IUnifiedRepository}. This interface is for code that initializes <b>any</b> backing repository.
 * 
 * <p>
 * Methods in this class must be called by an external caller (see example below). A caller can get a reference to the 
 * {@link IBackingRepositoryLifecycleManager} using{@code PentahoSystem}. Methods should be able to be called more than 
 * once with the same arguments with no adverse effects.
 * </p>
 * 
 * <p>
 * Example: When a servlet-based application starts up, a {@code ServletContextListener} calls {@link #startup()}. 
 * When a user logs in, {@link #newTenant(String)} and {@link #onNewUser(String)} are called. Finally, the 
 * {@code ServletContextListener} calls {@link #shutdown()}.
 * </p>
 * 
 * <p>
 * This class is necessary since some implementations cannot observe logins. Example: JCR cannot observe logins--only
 * node and property events.
 * </p>
 */
public interface IBackingRepositoryLifecycleManager {

  /**
   * To be called before any (non-admin) users interact with the backing repository.
   */
  void startup();

  /**
   * To be called on repository shutdown.
   */
  void shutdown();

  /**
   * To be called before any users belonging to a particular tenant interact with the backing repository.
   * @param new Tenant
   */
  void newTenant(final ITenant tenant);

  /**
   * To be called before any users belonging to the current tenant interact with the backing repository.
   */
  void newTenant();

  /**
   * To be called before user indicated by {@code username} interacts with the backing repository.
   * @param tenant to which the user belongs
   * @param username new username
   */
  void newUser(final ITenant tenant, final String username);

  /**
   * To be called before current user interacts with the backing repository.
   */
  void newUser();
}
