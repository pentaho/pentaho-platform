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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.rebind;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.impl.StandardCompilationResult;
import com.google.gwt.dev.Permutation;
import com.google.gwt.dev.cfg.StaticPropertyOracle;
import com.google.gwt.dev.jjs.PermutationResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonDependencyAddingLinkerTest {

  private static final String INPUT = "<html>\n"
    + "<head><meta charset=\"UTF-8\" /><script>\n"
    + "var $gwt_version = \"2.5.1\";\n"
    + "</script></head>\n"
    + "<body></body></html>";

  private static final String EXPECTED_OUTPUT = "<html>\n"
    + "<head><meta charset=\"UTF-8\" /><script>\n"
    + "var $gwt_version = \"2.5.1\";\n"
    + "</script>"
    + "<script type=\"text/javascript\" language=\"javascript\" "
    + "src=\"browser/lib/json/json2.js\"></script>\n</head>\n"
    + "<body></body></html>";

  @SuppressWarnings( "all" )
  private static final Comparator<SelectionProperty> SELECTION_PROPERTY_COMPARATOR =
    new Comparator<SelectionProperty>() {
      @Override
      public int compare( SelectionProperty o1, SelectionProperty o2 ) {
        return 1;
      }
    };

  private JsonDependencyAddingLinker linker = new JsonDependencyAddingLinker();

  // Mocks
  private LinkerContext context;
  private StandardCompilationResult result;
  private SelectionProperty property;

  @Before
  public void setUp() {
    context = mock( LinkerContext.class );
    property = mock( SelectionProperty.class );
    when( property.getName() ).thenReturn( JsonDependencyAddingLinker.PROPERTY_NAME );
    SortedSet<SelectionProperty> properties = new TreeSet<SelectionProperty>( SELECTION_PROPERTY_COMPARATOR );
    properties.add( property );
    when( context.getProperties() ).thenReturn( properties );

    PermutationResult permutationResult = mock( PermutationResult.class );
    when( permutationResult.getJs() ).thenReturn( new byte[][] { { 0 } } );
    when( permutationResult.getSerializedSymbolMap() ).thenReturn( new byte[] { 0 } );
    Permutation permutation = new Permutation( 0, mock( StaticPropertyOracle.class ) );
    when( permutationResult.getPermutation() ).thenReturn( permutation );
    result = new StandardCompilationResult( permutationResult );
  }

  /**
   * Given generated permutation result for IE8 browser.
   * <p/>
   * When customized html iframe linker is called to wrap html output,<br/>
   * then it should add JSON dependency to the output.
   */
  @Test
  public void shouldAddJsonDependencyForIE8() throws Exception {
    result.addSelectionPermutation( Collections.singletonMap( property, "ie8" ) );

    String wrapped = linker.testWrapPrimaryFragment( null, context, INPUT, null, result );

    assertEquals( EXPECTED_OUTPUT, wrapped );
  }

  /**
   * Given generated permutation result for IE9 browser.
   * <p/>
   * When customized html iframe linker is called to wrap html output,<br/>
   * then it should add JSON dependency to the output.
   */
  @Test
  public void shouldAddJsonDependencyForIE9() throws Exception {
    result.addSelectionPermutation( Collections.singletonMap( property, "ie9" ) );

    String wrapped = linker.testWrapPrimaryFragment( null, context, INPUT, null, result );

    assertEquals( EXPECTED_OUTPUT, wrapped );
  }

  /**
   * Given generated permutation result for Safari browser.
   * <p/>
   * When customized html iframe linker is called to wrap html output,<br/>
   * then it should NOT add JSON dependency to the output.
   */
  @Test
  public void shouldNotAddJsonDependencyForSafari() throws Exception {
    result.addSelectionPermutation( Collections.singletonMap( property, "safari" ) );

    String wrapped = linker.testWrapPrimaryFragment( null, context, INPUT, null, result );

    assertEquals( INPUT, wrapped );
  }

  /**
   * Given generated permutation result for Opera browser.
   * <p/>
   * When customized html iframe linker is called to wrap html output,<br/>
   * then it should NOT add JSON dependency to the output.
   */
  @Test
  public void shouldNotAddJsonDependencyForOpera() throws Exception {
    result.addSelectionPermutation( Collections.singletonMap( property, "opera" ) );

    String wrapped = linker.testWrapPrimaryFragment( null, context, INPUT, null, result );

    assertEquals( INPUT, wrapped );
  }
}
