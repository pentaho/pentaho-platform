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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.platform.repository2.unified.jcr.sejcr.NodeTypeDefinitionProvider;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.version.OnParentVersionAction;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.NT;
import static org.pentaho.platform.repository2.unified.jcr.sejcr.ntdproviders.NodeTypeDefinitionProviderUtils.PHO_NT;

// Equivalent CND:
// [pho_nt:locale]
//     - rootLocale (string) copy
//     - en (string) copy
//     - es (string) copy,
//     - ...etc
public class LocaleNtdProvider implements NodeTypeDefinitionProvider {

  /**
   *  A non-existent locale removed in java 9
   */
  @VisibleForTesting
  static final String GERMAN_GREECE = "de_GR";

  @VisibleForTesting
  static final String SIMPLIFIED_CHINESE_SINGAPORE = "zh_SG_#Hans";
  static final String TRADITIONAL_CHINESE_TAIWAN = "zh_TW_#Hant";
  static final String TRADITIONAL_CHINESE_HONG_KONG = "zh_HK_#Hant";
  static final String SIMPLIFIED_CHINESE_CHINA = "zh_CN_#Hans";
  static final String NORWEGIAN_BOKMAL = "nb_NO";
  static final String NORWEGIAN = "nb";
  static final String NORWEGIAN_NYNORSK = "nn_NO";

  /**
   * Indonesian and Hebrew changed from Java 11 to Java 17
   * Java 11 Indonesian: in, in_ID
   *         Hebrew: iw, iw_IL
   * Java 17 Indonesian: id, id_ID
   *         Hebrew: he, he_IL
   */
  static final String INDONESIAN_11 = "in";
  static final String INDONESIAN_INDONESIA_11 = "in_ID";
  static final String HEBREW_11 = "iw";
  static final String HEBREW_ISRAEL_11 = "iw_IL";
  static final String INDONESIAN_17 = "id";
  static final String INDONESIAN_INDONESIA_17 = "id_ID";
  static final String HEBREW_17 = "he";
  static final String HEBREW_ISRAEL_17 = "he_IL";

  @SuppressWarnings( "unchecked" )
  @Override
  public NodeTypeDefinition getNodeTypeDefinition( final NodeTypeManager ntMgr, final ValueFactory vFac )
    throws RepositoryException {
    NodeTypeTemplate t = ntMgr.createNodeTypeTemplate();
    t.setName( PHO_NT + "locale" ); //$NON-NLS-1$

    // create node definition for default locale
    t.getNodeDefinitionTemplates().add( getLocaleNode( ntMgr, "default" ) );

    for ( String localeName : getLocaleNames() ) {
      t.getNodeDefinitionTemplates().add( getLocaleNode( ntMgr, localeName ) );
    }

    return t;
  }

  @VisibleForTesting
  static List<String> getLocaleNames() {
    List<String> localeNames =
      Arrays.stream( Locale.getAvailableLocales() )
        .filter( Objects::nonNull ) // filter out null Locales
        .map( Locale::toString )
        .filter( Objects::nonNull ) //filter out null Strings
        .filter( localeName -> !localeName.isEmpty() )
        //GERMAN_GREECE was removed in Java 11, filter it out to prevent duplication when we add it back later
        .filter( localeName -> !GERMAN_GREECE.equals( localeName ) )
        //INDONESIAN and HEBREW were changed in Java 17 from Java 11, filter them out to prevent duplication when we add them back later
        .filter( localeName -> !INDONESIAN_11.equals( localeName ) )
        .filter( localeName -> !INDONESIAN_INDONESIA_11.equals( localeName ) )
        .filter( localeName -> !HEBREW_11.equals( localeName ) )
        .filter( localeName -> !HEBREW_ISRAEL_11.equals( localeName ) )
        .filter( localeName -> !INDONESIAN_17.equals( localeName ) )
        .filter( localeName -> !INDONESIAN_INDONESIA_17.equals( localeName ) )
        .filter( localeName -> !HEBREW_17.equals( localeName ) )
        .filter( localeName -> !HEBREW_ISRAEL_17.equals( localeName ) )
        /* All the below locales were added in Java 11. Locales are used as NodeType children in Jackrabbit. If a user
           upgrades to Java 11, then reverts back to Java 8, the jackrabbit repository will have non-trivial remove
           operations for each of these locales. To prevent this don't add these new locales at this time.
           Note - When Java 8 is no longer supported the below filters can be removed */
        .filter( localeName -> !SIMPLIFIED_CHINESE_SINGAPORE.equals( localeName ) )
        .filter( localeName -> !TRADITIONAL_CHINESE_TAIWAN.equals( localeName ) )
        .filter( localeName -> !TRADITIONAL_CHINESE_HONG_KONG.equals( localeName ) )
        .filter( localeName -> !SIMPLIFIED_CHINESE_CHINA.equals( localeName ) )
        .filter( localeName -> !NORWEGIAN_BOKMAL.equals( localeName ) )
        .filter( localeName -> !NORWEGIAN.equals( localeName ) )
        .filter( localeName -> !NORWEGIAN_NYNORSK.equals( localeName ) )
        .collect( Collectors.toList() );

    // Add German (Greece) as this was deleted in Java 11, this prevents Jackrabbit from having a non-trivial remove
    //operation after upgrading from Java 8 to Java 11.
    localeNames.add( GERMAN_GREECE );
    // Add Indonesian and Hebrew back in for Java 11 and java 17
    localeNames.add( INDONESIAN_11 );
    localeNames.add( INDONESIAN_INDONESIA_11 );
    localeNames.add( HEBREW_11 );
    localeNames.add( HEBREW_ISRAEL_11 );
    localeNames.add( INDONESIAN_17 );
    localeNames.add( INDONESIAN_INDONESIA_17 );
    localeNames.add( HEBREW_17);
    localeNames.add( HEBREW_ISRAEL_17) ;
    return localeNames;
  }

  private NodeDefinitionTemplate getLocaleNode( final NodeTypeManager ntMgr, final String localeName )
    throws RepositoryException {
    NodeDefinitionTemplate t = ntMgr.createNodeDefinitionTemplate();
    t.setName( localeName ); //$NON-NLS-1$
    t.setRequiredPrimaryTypeNames( new String[] { NT + "unstructured" } ); //$NON-NLS-1$
    t.setOnParentVersion( OnParentVersionAction.COPY );
    t.setSameNameSiblings( false );
    return t;
  }
}
