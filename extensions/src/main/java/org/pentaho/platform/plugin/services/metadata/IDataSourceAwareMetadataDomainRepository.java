/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
