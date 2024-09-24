/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importer;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;

public class DefaultRepositoryContentConverterHandler implements IRepositoryContentConverterHandler {

  Map<String, Converter> converters;

  public DefaultRepositoryContentConverterHandler( final Map<String, Converter> converterMap ) {
    converters = new HashMap<String, Converter>();
    converters.putAll( converterMap );
  }

  @Override
  public Map<String, Converter> getConverters() {
    return converters;
  }

  @Override
  public void addConverter( String extension, Converter converter ) {
    converters.put( extension, converter );

  }

  @Override
  public Converter getConverter( String extension ) {
    return converters.get( extension );
  }
}
