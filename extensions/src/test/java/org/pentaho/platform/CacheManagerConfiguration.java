/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2 as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, you can obtain
 * a copy at http://www.gnu.org/licenses/gpl-2.0.html or from the Free Software Foundation, Inc.,  51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2018 Hitachi Vantara.  All rights reserved.
 *
 */

package org.pentaho.platform;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.cache.SimpleMapCacheManager;
import org.pentaho.platform.plugin.services.cache.CacheManager;
import org.pentaho.platform.plugin.services.metadata.SessionCachingMetadataDomainRepository;
import org.pentaho.platform.repository2.unified.jcr.jackrabbit.security.SpringSecurityPrincipalProvider;
import org.pentaho.platform.web.http.filters.PentahoWebContextFilter;
import org.pentaho.test.platform.plugin.services.metadata.MockSessionAwareMetadataDomainRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Created by Dmitriy Stepanov on 07.02.18.
 */

@Profile( "test" )
@Configuration
public class CacheManagerConfiguration {

  @Bean
  @Primary
  @Scope( ConfigurableBeanFactory.SCOPE_SINGLETON )
  public ICacheManager iCacheManager() {
    return mock( ICacheManager.class );
  }

  @Bean( name = "withSessionAware" )
  @Primary
  public SessionCachingMetadataDomainRepository sessionCachingMetadataDomainRepository() {
    return new SessionCachingMetadataDomainRepository( mockSessionAwareMetadataDomainRepository() );
  }

  @Bean
  @Primary
  public MockSessionAwareMetadataDomainRepository mockSessionAwareMetadataDomainRepository() {
    return spy( new MockSessionAwareMetadataDomainRepository() );
  }

  @Bean
  @Primary
  public PentahoWebContextFilter pentahoWebContextFilter() {
    return spy( new PentahoWebContextFilter() );
  }

  @Bean
  @Primary
  public IPluginManager pluginManager() {
    return mock( IPluginManager.class );
  }
}
