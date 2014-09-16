/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.plugin.action.jfreereport.outputs;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;

public class JFreeReportPdfComponentTest {
  private final Level level = LogManager.getRootLogger().getLevel();

  @Before
  public void setUp() {
    //off loger for test
    LogManager.getRootLogger().setLevel( Level.OFF );
  }

  @Test
  public void performExportTest() throws IOException {
    //load report engine    
    ClassicEngineBoot.getInstance().start();
    MasterReport report = new MasterReport();
    OutputStream outputStream = spy( new ByteArrayOutputStream() );
    JFreeReportPdfComponent jFreeReportPdfComponent  = spy( new JFreeReportPdfComponent() );

    doReturn( 0 ).when( jFreeReportPdfComponent ).getYieldRate();
    assertTrue( jFreeReportPdfComponent.performExport( report, outputStream ) );
    verify( outputStream, atLeastOnce() ).close();
    verify( outputStream, atLeastOnce() ).flush();
  }

  @After
  public void tearDown() {
    //return to level
    LogManager.getRootLogger().setLevel( level );
  }

}
