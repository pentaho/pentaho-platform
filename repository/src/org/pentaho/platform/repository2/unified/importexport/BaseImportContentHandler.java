/*
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
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.repository2.unified.importexport;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository2.messages.Messages;

import java.util.Map;

/**
 * Base class which can be used for any ImportContentHandlers
 * User: dkincade
 */
public abstract class BaseImportContentHandler implements ImportContentHandler {
  protected static Messages messages = Messages.getInstance();

  private IUnifiedRepository repository;
  private Map<String, Converter> converters;
  private String destinationFolderPath;
  private String versionMessage;

  @Override
  public void initialize(final IUnifiedRepository repository,
                         final Map<String, Converter> converters,
                         final String destinationFolderPath,
                         final String versionMessage)
      throws InitializationException {
    if (null == repository) {
      throw new InitializationException(messages.getErrorString("ImportContentHandler.ERROR_0001_NULL_REPOSITORY"));
    }
    this.repository = repository;
  }

  public IUnifiedRepository getRepository() {
    return repository;
  }

  public Map<String, Converter> getConverters() {
    return converters;
  }

  public String getDestinationFolderPath() {
    return destinationFolderPath;
  }

  public String getVersionMessage() {
    return versionMessage;
  }
}
