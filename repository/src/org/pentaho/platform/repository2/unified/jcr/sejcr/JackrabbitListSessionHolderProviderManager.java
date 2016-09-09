package org.pentaho.platform.repository2.unified.jcr.sejcr;

import java.util.Collections;
import java.util.List;

import javax.jcr.Repository;
import org.springframework.binding.collection.AbstractCachingMapDecorator;
import org.springframework.extensions.jcr.SessionHolderProvider;
import org.springframework.extensions.jcr.support.AbstractSessionHolderProviderManager;

public class JackrabbitListSessionHolderProviderManager extends AbstractSessionHolderProviderManager {

  private List<SessionHolderProvider> providers = Collections.emptyList();
  

  protected class ProvidersCache extends AbstractCachingMapDecorator {
      private ProvidersCache() {
          super(true);
      }

      @Override
      protected Object create(Object key) {
          return parentLookup((Repository) key);
      }

  }

  /**
   * Providers cache.
   */
  private final ProvidersCache providersCache = new ProvidersCache();

  /**
   * Method for retrieving the parent functionality.
   * @param sf
   * @return
   */
  private SessionHolderProvider parentLookup(Repository repository) {
      return super.getSessionProvider(repository);
  }

  public SessionHolderProvider getSessionProvider(Repository repository) {
      return (SessionHolderProvider) providersCache.get(repository);
  }
  
  @Override
  public List<SessionHolderProvider> getProviders() {
    return providers;
  }

  /**
   * @param providers The providers to set.
   */
  public void setProviders(List<SessionHolderProvider> providers) {
      this.providers = providers;
  }
}
