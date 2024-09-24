/*
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
 * Copyright (c) 2022 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.metadata.repository.IMetadataDomainRepository;

import java.util.Set;

/**
 * This interface defines a metadata domain repository, used to maintain a system-wide set of metadata domains.
 * Includes the ability to retrieve Domains based on data source type.
 */
public interface IDataSourceAwareMetadataDomainRepository extends IMetadataDomainRepository {

    /**
     * Retrieve a list of all the domain ids in the repository of the data source type Metadata.
     * See {@link #getDomainIds()} for similar functionality.
     *
     * @return the metadata domain Ids.
     */
    public Set<String> getMetadataDomainIds();

    /**
     * Retrieve a list of all the domain ids in the repository of the data source type DataSourceWizard.
     * See {@link #getDomainIds()} for similar functionality.
     *
     * @return the data source wizard domain Ids.
     */
    public Set<String> getDataSourceWizardDomainIds();
}
