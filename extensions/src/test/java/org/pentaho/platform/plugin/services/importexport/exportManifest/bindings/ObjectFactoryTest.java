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

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Created by rfellows on 10/26/15.
 */
public class ObjectFactoryTest {

  ObjectFactory factory;

  @Before
  public void setUp() throws Exception {
    factory = new ObjectFactory();
  }

  @Test
  public void testCreateEntityAcl() throws Exception {
    assertNotNull( factory.createEntityAcl() );
  }

  @Test
  public void testCreateParameters() throws Exception {
    assertNotNull( factory.createParameters() );
  }

  @Test
  public void testCreateParametersEntries() throws Exception {
    assertNotNull( factory.createParametersEntries() );
  }

  @Test
  public void testCreateComplexJobTrigger() throws Exception {
    assertNotNull( factory.createComplexJobTrigger() );
  }

  @Test
  public void testCreateDatabaseConnection() throws Exception {
    assertNotNull( factory.createDatabaseConnection() );
  }

  @Test
  public void testCreateDatabaseConnectionExtraOptions() throws Exception {
    assertNotNull( factory.createDatabaseConnectionExtraOptions() );
  }

  @Test
  public void testCreateDatabaseConnectionConnectionPoolingProperties() throws Exception {
    assertNotNull( factory.createDatabaseConnectionConnectionPoolingProperties() );
  }

  @Test
  public void testCreateDatabaseConnectionAttributes() throws Exception {
    assertNotNull( factory.createDatabaseConnectionAttributes() );
  }

  @Test
  public void testCreateExportManifestDto() throws Exception {
    assertNotNull( factory.createExportManifestDto() );
  }

  @Test
  public void testCreateDatabaseType() throws Exception {
    assertNotNull( factory.createDatabaseType() );
  }

  @Test
  public void testCreateSimpleJobTrigger() throws Exception {
    assertNotNull( factory.createSimpleJobTrigger() );
  }

  @Test
  public void testCreateQualifiedDayOfMonth() throws Exception {
    assertNotNull( factory.createQualifiedDayOfMonth() );
  }

  @Test
  public void testCreateJobScheduleRequest() throws Exception {
    assertNotNull( factory.createJobScheduleRequest() );
  }

  @Test
  public void testCreateIncrementalRecurrence() throws Exception {
    assertNotNull( factory.createIncrementalRecurrence() );
  }

  @Test
  public void testCreatePartitionDatabaseMeta() throws Exception {
    assertNotNull( factory.createPartitionDatabaseMeta() );
  }

  @Test
  public void testCreateSequentialRecurrence() throws Exception {
    assertNotNull( factory.createSequentialRecurrence() );
  }

  @Test
  public void testCreateRecurrenceList() throws Exception {
    assertNotNull( factory.createRecurrenceList() );
  }

  @Test
  public void testCreateCronJobTrigger() throws Exception {
    assertNotNull( factory.createCronJobTrigger() );
  }

  @Test
  public void testCreateQualifiedDayOfWeek() throws Exception {
    assertNotNull( factory.createQualifiedDayOfWeek() );
  }

  @Test
  public void testCreateComplexJobTriggerProxy() throws Exception {
    assertNotNull( factory.createComplexJobTriggerProxy() );
  }

  @Test
  public void testCreateExportManifestProperty() throws Exception {
    assertNotNull( factory.createExportManifestProperty() );
  }

  @Test
  public void testCreateEntityMetaData() throws Exception {
    assertNotNull( factory.createEntityMetaData() );
  }

  @Test
  public void testCreateCustomProperty() throws Exception {
    assertNotNull( factory.createCustomProperty() );
  }

  @Test
  public void testCreateExportManifestMetadata() throws Exception {
    assertNotNull( factory.createExportManifestMetadata() );
  }

  @Test
  public void testCreateExportManifestEntityDto() throws Exception {
    assertNotNull( factory.createExportManifestDto() );
  }

  @Test
  public void testCreateExportManifestMondrian() throws Exception {
    assertNotNull( factory.createExportManifestMondrian() );
  }

  @Test
  public void testCreateJobScheduleParam() throws Exception {
    assertNotNull( factory.createJobScheduleParam() );
  }

  @Test
  public void testCreateEntityAclAces() throws Exception {
    assertNotNull( factory.createEntityAclAces() );
  }

  @Test
  public void testCreateParametersEntriesEntry() throws Exception {
    assertNotNull( factory.createParametersEntriesEntry() );
  }

  @Test
  public void testCreateComplexJobTriggerDayOfMonthRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerDayOfMonthRecurrences() );
  }

  @Test
  public void testCreateComplexJobTriggerDayOfWeekRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerDayOfWeekRecurrences() );
  }

  @Test
  public void testCreateComplexJobTriggerHourlyRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerHourlyRecurrences() );
  }

  @Test
  public void testCreateComplexJobTriggerMinuteRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerMinuteRecurrences() );
  }

  @Test
  public void testCreateComplexJobTriggerMonthlyRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerMonthlyRecurrences() );
  }

  @Test
  public void testCreateComplexJobTriggerSecondRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerSecondRecurrences() );
  }

  @Test
  public void testCreateComplexJobTriggerYearlyRecurrences() throws Exception {
    assertNotNull( factory.createComplexJobTriggerYearlyRecurrences() );
  }

  @Test
  public void testCreateDatabaseConnectionExtraOptionsEntry() throws Exception {
    assertNotNull( factory.createDatabaseConnectionExtraOptionsEntry() );
  }

  @Test
  public void testCreateDatabaseConnectionConnectionPoolingPropertiesEntry() throws Exception {
    assertNotNull( factory.createDatabaseConnectionConnectionPoolingPropertiesEntry() );
  }

  @Test
  public void testCreateDatabaseConnectionAttributesEntry() throws Exception {
    assertNotNull( factory.createDatabaseConnectionAttributesEntry() );
  }

  @Test
  public void testCreateExportManifestDtoExportManifestInformation() throws Exception {
    assertNotNull( factory.createExportManifestDtoExportManifestInformation() );
  }

  @Test
  public void testCreateIncrementalRecurrence1() throws Exception {
    assertNotNull( factory.createIncrementalRecurrence( mock( IncrementalRecurrence.class ) ) );
  }

  @Test
  public void testCreateJobScheduleRequest1() throws Exception {
    assertNotNull( factory.createJobScheduleRequest( mock( JobScheduleRequest.class ) ) );
  }

  @Test
  public void testCreateQualifiedDayOfMonth1() throws Exception {
    assertNotNull( factory.createQualifiedDayOfMonth( mock( QualifiedDayOfMonth.class ) ) );
  }

  @Test
  public void testCreateExportManifest() throws Exception {
    assertNotNull( factory.createExportManifest( mock( ExportManifestDto.class ) ) );
  }

  @Test
  public void testCreateSimpleJobTrigger1() throws Exception {
    assertNotNull( factory.createSimpleJobTrigger( mock( SimpleJobTrigger.class ) ) );
  }

  @Test
  public void testCreateDatabaseType1() throws Exception {
    assertNotNull( factory.createDatabaseType( mock( DatabaseType.class ) ) );
  }

  @Test
  public void testCreateCronJobTrigger1() throws Exception {
    assertNotNull( factory.createCronJobTrigger( mock( CronJobTrigger.class ) ) );
  }

  @Test
  public void testCreateRecurrenceList1() throws Exception {
    assertNotNull( factory.createRecurrenceList( mock( RecurrenceList.class ) ) );
  }

  @Test
  public void testCreateSequentialRecurrence1() throws Exception {
    assertNotNull( factory.createSequentialRecurrence( mock( SequentialRecurrence.class ) ) );
  }

  @Test
  public void testCreatePartitionDatabaseMeta1() throws Exception {
    assertNotNull( factory.createPartitionDatabaseMeta( mock( PartitionDatabaseMeta.class ) ) );
  }

  @Test
  public void testCreateDatabaseConnection1() throws Exception {
    assertNotNull( factory.createDatabaseConnection( mock( DatabaseConnection.class ) ) );
  }

  @Test
  public void testCreateComplexJobTriggerProxy1() throws Exception {
    assertNotNull( factory.createComplexJobTriggerProxy( mock( ComplexJobTriggerProxy.class ) ) );
  }

  @Test
  public void testCreateQualifiedDayOfWeek1() throws Exception {
    assertNotNull( factory.createQualifiedDayOfWeek( mock( QualifiedDayOfWeek.class ) ) );
  }

  @Test
  public void testCreateComplexJobTrigger1() throws Exception {
    assertNotNull( factory.createComplexJobTrigger( mock( ComplexJobTrigger.class ) ) );
  }
}
