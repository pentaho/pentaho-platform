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


package org.pentaho.platform.uifoundation.component.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.ui.UIException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a user interface that lets users select items from lists or radio buttons. The selections
 * are used by other components to filter queries used to populate data.
 * 
 * @author James Dixon
 */
public class FilterPanelComponent extends XmlComponent {
  private static final Log log = LogFactory.getLog( FilterPanelComponent.class );

  /**
   * 
   */
  private static final long serialVersionUID = 700681534058825526L;

  private static final Log logger = LogFactory.getLog( FilterPanelComponent.class );

  /**
   * repository relative path and filename to the filter panel definition file
   */
  private String definitionPath;

  private FilterPanel filterPanel;

  private String xslName;

  private Map defaultValues;

  public FilterPanelComponent( final String definitionPath, String xslName, final IPentahoUrlFactory urlFactory,
      final List messages ) {
    super( urlFactory, messages, null );
    this.definitionPath = definitionPath;
    if ( xslName == null ) {
      // use a default XSL
      xslName = "FilterPanelDefault.xsl"; //$NON-NLS-1$
    }
    ActionInfo info = ActionInfo.parseActionString( definitionPath );
    if ( info != null ) {
      setSourcePath( info.getSolutionName() + File.separator + info.getPath() );
    }
    this.xslName = xslName;
    defaultValues = new HashMap();
  }

  public void setDefaultValue( final String filterName, final String[] defaultValue ) {
    defaultValues.put( filterName, defaultValue );
  }

  @Override
  public Log getLogger() {
    return FilterPanelComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

  public List getFilters() {
    return filterPanel.getFilters();
  }

  public boolean init() {
    if ( filterPanel == null ) {
      // load the XML document that defines the dial
      Document filterDocument = null;
      try {
        org.dom4j.io.SAXReader reader = XMLParserFactoryProducer.getSAXReader( new SolutionURIResolver() );
        filterDocument =
            reader.read( ActionSequenceResource.getInputStream( definitionPath, LocaleHelper.getLocale() ) );
      } catch ( Throwable t ) {
        FilterPanelComponent.logger.error( Messages.getInstance().getString(
            "FilterPanelComponent.ERROR_0002_CREATE_XML" ), t ); //$NON-NLS-1$
        return false;
      }
      try {
        filterPanel = getFilterPanel( filterDocument );
      } catch ( FilterPanelException e ) {
        // TODO sbarkdull localize
        FilterPanelComponent.logger.error(
            Messages.getInstance().getString( "FilterPanelComponent.ERROR_0003_CREATE" ), e ); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  @Override
  public Document getXmlContent() {
    //      assert null != urlFactory : Messages.getInstance().getString("FilterPanelComponent.ERROR_0000_FACTORY_CANNOT_BE_NULL"); //$NON-NLS-1$

    boolean ok = filterPanel.populate( getParameterProviders(), defaultValues );

    if ( !ok ) {
      String msg = Messages.getInstance().getString( "FilterPanelComponent.ERROR_0001_POPULATE" ); //$NON-NLS-1$
      FilterPanelComponent.log.error( msg );
      throw new UIException( msg );
    }

    String actionUrl = urlFactory.getActionUrlBuilder().getUrl();
    Document xForm = filterPanel.getXForm( actionUrl );

    setXsl( "text/html", xslName ); //$NON-NLS-1$

    return xForm;
  }

  private FilterPanel getFilterPanel( final Document filterDocument ) throws FilterPanelException {
    FilterPanel newFilterPanel = new FilterPanel( getSession(), filterDocument, this );
    return newFilterPanel;
  }

}
