/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
