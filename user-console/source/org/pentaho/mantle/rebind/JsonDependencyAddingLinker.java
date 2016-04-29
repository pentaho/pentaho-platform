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
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.linker.IFrameLinker;
import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * IE8 and IE9 browsers provide JSON object in Standards Mode only,
 * so as far as we continue working in Quirks Mode ("Almost Standards Mode" in IE terms),
 * we have to include json2.js library to our application.
 * <p/>
 * This class extends GWT's standard linker,
 * which wraps generated permutation with HTML (<code>.cache.html</code> files),
 * adding json2.js library dependency to the wrapping HTML code.
 * <p/>
 * To make this linker be used by GWT compiler,
 * it should be included in the module XML file (<code>.gwt.xml</code>),
 * redefining standard iframe linker (<b>"std"</b> code).
 */
public class JsonDependencyAddingLinker extends IFrameLinker {

  static final String PROPERTY_NAME = "user.agent";

  private static final String WHERE_TO_PLACE = "</head>";
  private static final String JSON_SCRIPT = "<script type=\"text/javascript\" language=\"javascript\" "
    + "src=\"browser/lib/json/json2.js\"></script>\n";

  private static final List<String> BROWSERS = Arrays.asList( "ie8", "ie9" );

  @Override
  public String getDescription() {
    return "Standard Extended (adding JSON script dependency)";
  }

  /**
   * Check if current compilation result is for IE8 or IE9.
   * If true, then add json2.js dependency to the wrapping HTML,
   * otherwise do nothing.
   */
  @Override
  protected String wrapPrimaryFragment( TreeLogger logger, LinkerContext context, String script,
                                        ArtifactSet artifacts, CompilationResult result )
    throws UnableToCompleteException {
    String primaryFragment = super.wrapPrimaryFragment( logger, context, script, artifacts, result );

    boolean foundProperty = false;
    for ( SelectionProperty property : context.getProperties() ) {
      foundProperty = PROPERTY_NAME.equals( property.getName());
      if ( foundProperty ) {
        SortedSet<SortedMap<SelectionProperty, String>> propertyMap = result.getPropertyMap();
        for ( SortedMap<SelectionProperty, String> selectionPropertyMap : propertyMap ) {
          String value = selectionPropertyMap.get( property );
          if ( BROWSERS.contains( value ) ) {
            return addJsonDependency( logger, primaryFragment );
          }
        }
      }
    }
    if ( !foundProperty ) {
      logger.log( TreeLogger.ERROR,
        String.format( "Unable to read 'user.agent' property in permutation %d.", result.getPermutationId() ) );
    }
    return primaryFragment;
  }

  @VisibleForTesting
  String testWrapPrimaryFragment( TreeLogger logger, LinkerContext context, String script,
                                  ArtifactSet artifacts, CompilationResult result )
    throws UnableToCompleteException {
    return wrapPrimaryFragment( logger, context, script, artifacts, result );
  }

  private String addJsonDependency( TreeLogger logger, String primaryFragment ) {
    int placeIdx = primaryFragment.indexOf( WHERE_TO_PLACE );

    // fallback
    if (placeIdx == -1) {
      logger.log( TreeLogger.ERROR, "Unable to find an appropriate place for json2.js script tag placing." );
      return primaryFragment;
    }

    return new StringBuilder( primaryFragment )
      .insert( placeIdx, JSON_SCRIPT )
      .toString();
  }
}
