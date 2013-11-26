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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.uifoundation.component.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SolutionHelper;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@SuppressWarnings( "rawtypes" )
public class PMDUIComponent extends XmlComponent {

  private static final long serialVersionUID = -911457505257919399L;

  private static final Log logger = LogFactory.getLog( PMDUIComponent.class );

  public static final int ACTION_LIST_DOMAINS = 1;

  public static final int ACTION_LIST_MODELS = 2;

  public static final int ACTION_MODELS_DETAIL = 3;

  public static final int ACTION_LOAD_MODEL = 4;

  public static final int ACTION_LOOKUP = 5;

  private int action;

  private String domainName;

  private String modelId;

  private String columnId;

  private IParameterProvider parameters;

  public PMDUIComponent( final IPentahoUrlFactory urlFactory, final List messages ) {
    super( urlFactory, messages, "" ); //$NON-NLS-1$
  }

  @Override
  public Log getLogger() {
    return PMDUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

  public IMetadataDomainRepository getMetadataRepository() {
    return PentahoSystem.get( IMetadataDomainRepository.class, getSession() );
  }

  @Override
  public Document getXmlContent() {

    if ( action == PMDUIComponent.ACTION_LIST_MODELS ) {
      return listModels();
    } else if ( action == PMDUIComponent.ACTION_LOAD_MODEL ) {
      return loadModel();
    } else if ( action == PMDUIComponent.ACTION_LOOKUP ) {
      return getLookup();
    } else {
      throw new RuntimeException( Messages.getInstance().getErrorString(
          "PMDUIComponent.ERROR_0002_ILLEGAL_ACTION", String.valueOf( action ) ) ); //$NON-NLS-1$
    }
  }

  private Document listModels() {

    // Create a document that describes the result
    Document doc = DocumentHelper.createDocument();

    Element root = doc.addElement( "metadata" ); //$NON-NLS-1$

    Element modelsNode = root.addElement( "models" ); //$NON-NLS-1$
    if ( domainName == null ) {
      try {
        for ( String domain : getMetadataRepository().getDomainIds() ) {
          addThinDomainModels( domain, modelsNode, root );
        }
      } catch ( Throwable t ) {
        error( Messages.getInstance().getString( "PMDUIComponent.ERROR_0001_GET_MODEL_LIST" ) ); //$NON-NLS-1$
        t.printStackTrace();
        root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_NO_DOMAIN_SPECIFIED" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }

    } else {
      addThinDomainModels( domainName, modelsNode, root );
    }
    return doc;
  }

  private void addThinDomainModels( final String domain, final Element modelsNode, final Element root ) {

    IMetadataDomainRepository repo = getMetadataRepository();

    Domain domainObject = repo.getDomain( domain );

    String locale = LocaleHelper.getClosestLocale( LocaleHelper.getLocale().toString(), domainObject.getLocaleCodes() );

    Element modelNode;
    for ( LogicalModel model : domainObject.getLogicalModels() ) {
      String vis = (String) model.getProperty( "visible" );
      if ( vis != null ) {
        String[] visibleContexts = vis.split( "," );
        boolean visibleToContext = false;
        for ( String context : visibleContexts ) {
          if ( "adhoc".equals( context.trim() ) ) {
            visibleToContext = true;
            break;
          }
        }
        if ( !visibleToContext ) {
          continue;
        }
      }
      modelNode = modelsNode.addElement( "model" ); //$NON-NLS-1$
      modelNode.addElement( "domain_id" ).setText( domain ); //$NON-NLS-1$
      if ( model.getId() != null ) {
        modelNode.addElement( "model_id" ).setText( model.getId() ); //$NON-NLS-1$
      }
      String modelName = model.getName( locale );
      if ( modelName != null ) {
        modelNode.addElement( "model_name" ).setText( modelName ); //$NON-NLS-1$
      }

      if ( model.getDescription() != null ) {
        String modelDescription = model.getDescription( locale );
        if ( modelDescription != null ) {
          modelNode.addElement( "model_description" ).setText( modelDescription ); //$NON-NLS-1$
        }
      }
    }
    return;
  }

  private Document loadModel() {
    // Create a document that describes the result
    Document doc = DocumentHelper.createDocument();

    Element root = doc.addElement( "metadata" ); //$NON-NLS-1$

    if ( domainName == null ) {
      // we can't do this without a model
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_NO_DOMAIN_SPECIFIED" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    if ( modelId == null ) {
      // we can't do this without a model
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_NO_MODEL_SPECIFIED" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    Element modelNode = root.addElement( "model" ); //$NON-NLS-1$

    // because it's lighter weight, check the thin model
    Domain domain = getMetadataRepository().getDomain( domainName );
    if ( domain == null ) {
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_DOMAIN_LOADING_ERROR", domainName ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }
    String locale = LocaleHelper.getClosestLocale( LocaleHelper.getLocale().toString(), domain.getLocaleCodes() );
    LogicalModel model = domain.findLogicalModel( modelId );

    if ( model == null ) {
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_MODEL_LOADING_ERROR", modelId ) ); //$NON-NLS-1$ //$NON-NLS-2$
      error( Messages.getInstance().getString( "PMDUIComponent.USER_MODEL_LOADING_ERROR", modelId ) ); //$NON-NLS-1$
      return doc;
    }
    modelNode.addElement( "domain_id" ).setText( domainName ); //$NON-NLS-1$
    if ( model.getId() != null ) {
      modelNode.addElement( "model_id" ).setText( model.getId() ); //$NON-NLS-1$
    }
    if ( model.getName( locale ) != null ) {
      modelNode.addElement( "model_name" ).setText( model.getName( locale ) ); //$NON-NLS-1$
    }
    if ( model.getDescription( locale ) != null ) {
      modelNode.addElement( "model_description" ).setText( model.getDescription( locale ) ); //$NON-NLS-1$
    }

    Element tableNode;

    for ( Category category : model.getCategories() ) {
      tableNode = modelNode.addElement( "view" ); //$NON-NLS-1$
      if ( category.getId() != null ) {
        tableNode.addElement( "view_id" ).setText( category.getId() ); //$NON-NLS-1$
      }
      if ( category.getName( locale ) != null ) {
        tableNode.addElement( "view_name" ).setText( category.getName( locale ) ); //$NON-NLS-1$
      }
      if ( category.getDescription( locale ) != null ) {
        tableNode.addElement( "view_description" ).setText( category.getDescription( locale ) ); //$NON-NLS-1$
      }
      for ( LogicalColumn column : category.getLogicalColumns() ) {
        Boolean hidden = (Boolean) column.getProperty( "hidden" ); //$NON-NLS-1$
        if ( hidden != null && hidden ) {
          continue;
        }
        addColumn( column, tableNode, locale );
      }
    }

    return doc;
  }

  @SuppressWarnings( "deprecation" )
  public void addColumn( final LogicalColumn column, final Element tableNode, final String locale ) {
    Element columnNode = tableNode.addElement( "column" ); //$NON-NLS-1$

    if ( column.getId() != null ) {
      columnNode.addElement( "column_id" ).setText( column.getId() ); //$NON-NLS-1$
    }
    if ( column.getName( locale ) != null ) {
      columnNode.addElement( "column_name" ).setText( column.getName( locale ) ); //$NON-NLS-1$
    }
    if ( column.getDescription( locale ) != null ) {
      columnNode.addElement( "column_description" ).setText( column.getDescription( locale ) ); //$NON-NLS-1$
    }
    if ( column.getFieldType() != null ) {
      // TODO this should take a locale
      String desc = column.getFieldType().getDescription();
      desc = org.pentaho.pms.messages.Messages.getString( desc );
      columnNode.addElement( "column_field_type" ).setText( desc ); //$NON-NLS-1$
    }
    DataType dataType = column.getDataType();
    if ( dataType != null ) {
      columnNode.addElement( "column_type" ).setText( dataType.getName() ); //$NON-NLS-1$
    }
    if ( column.getProperty( "lookup" ) != null ) { //$NON-NLS-1$
      columnNode.addElement( "column_lookup" ).setText( "true" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public Document getLookup() {
    // Create a document that describes the result
    Document doc = DocumentHelper.createDocument();

    Element root = doc.addElement( "metadata" ); //$NON-NLS-1$

    if ( domainName == null ) {
      // we can't do this without a model
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_NO_DOMAIN_SPECIFIED" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    if ( modelId == null ) {
      // we can't do this without a view
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_NO_MODEL_SPECIFIED" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    if ( columnId == null ) {
      // we can't do this without a view
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_NO_COLUMN_SPECIFIED" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    Domain domain = getMetadataRepository().getDomain( domainName );
    String locale = LocaleHelper.getClosestLocale( LocaleHelper.getLocale().toString(), domain.getLocaleCodes() );
    LogicalModel model = domain.findLogicalModel( modelId ); // This is the business view that was selected.
    if ( model == null ) {
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_MODEL_LOADING_ERROR", modelId ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    LogicalColumn column = model.findLogicalColumn( columnId );
    if ( column == null ) {
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_COLUMN_NOT_FOUND" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    // Temporary hack to get the BusinessCategory. When fixed properly, you should be able to interrogate the
    // business column thingie for it's containing BusinessCategory.
    Category view = null;
    for ( Category category : model.getCategories() ) {
      for ( LogicalColumn col : category.getLogicalColumns() ) {
        if ( col.getId().equals( column.getId() ) ) {
          view = category;
          break;
        }
      }
    }

    if ( view == null ) {
      root.addElement( "message" ).setText( Messages.getInstance().getString( "PMDUIComponent.USER_VIEW_NOT_FOUND" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return doc;
    }

    String mql =
        "<mql><domain_type>relational</domain_type><domain_id>" + domainName + "</domain_id><model_id>" + modelId + "</model_id>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    if ( column.getProperty( "lookup" ) == null ) { //$NON-NLS-1$
      mql += "<selection><view>" + view.getId() + "</view><column>" + column.getId() + "</column></selection>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      mql +=
          "<orders><order><direction>asc</direction><view_id>" + view.getId() + "</view_id><column_id>" + column.getId() + "</column_id></order></orders>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    } else {

      String lookup = (String) column.getProperty( "lookup" ); //$NON-NLS-1$
      // assume model and view are the same...
      StringTokenizer tokenizer1 = new StringTokenizer( lookup, ";" ); //$NON-NLS-1$
      while ( tokenizer1.hasMoreTokens() ) {
        StringTokenizer tokenizer2 = new StringTokenizer( tokenizer1.nextToken(), "." ); //$NON-NLS-1$
        if ( tokenizer2.countTokens() == 2 ) {
          String lookupViewId = tokenizer2.nextToken();
          String lookupColumnId = tokenizer2.nextToken();
          mql += "<selection><view>" + lookupViewId + "</view><column>" + lookupColumnId + "</column></selection>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
      }
    }
    mql += "</mql>"; //$NON-NLS-1$
    ArrayList messages = new ArrayList();
    SimpleParameterProvider lookupParameters = new SimpleParameterProvider();
    lookupParameters.setParameter( "mql", mql ); //$NON-NLS-1$

    IRuntimeContext runtime =
        SolutionHelper.doAction(
            "/system/metadata/PickList.xaction", "lookup-list", lookupParameters, getSession(), messages, this ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    IPentahoResultSet results = null;
    if ( runtime != null ) {
      if ( runtime.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
        if ( runtime.getOutputNames().contains( "data" ) ) { //$NON-NLS-1$
          results = runtime.getOutputParameter( "data" ).getValueAsResultSet(); //$NON-NLS-1$
          Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
          boolean hasColumnHeaders = columnHeaders != null;

          Element rowElement;
          Element dataElement = root.addElement( "data" ); //$NON-NLS-1$
          if ( hasColumnHeaders ) {
            for ( int rowNo = 0; rowNo < columnHeaders.length; rowNo++ ) {
              rowElement = dataElement.addElement( "COLUMN-HDR-ROW" ); //$NON-NLS-1$
              for ( int columnNo = 0; columnNo < columnHeaders[rowNo].length; columnNo++ ) {
                Object nameAttr = results.getMetaData().getAttribute( rowNo, columnNo, "name" ); //$NON-NLS-1$
                if ( ( nameAttr != null ) && ( nameAttr instanceof LocalizedString ) ) {
                  LocalizedString str = (LocalizedString) nameAttr;
                  String name = str.getLocalizedString( locale );
                  if ( name != null ) {
                    rowElement.addElement( "COLUMN-HDR-ITEM" ).setText( name ); //$NON-NLS-1$
                  } else {
                    rowElement.addElement( "COLUMN-HDR-ITEM" ).setText( columnHeaders[rowNo][columnNo].toString() ); //$NON-NLS-1$
                  }
                } else {
                  rowElement.addElement( "COLUMN-HDR-ITEM" ).setText( columnHeaders[rowNo][columnNo].toString() ); //$NON-NLS-1$
                }
              }
            }
          }
          Object[] row = results.next();
          while ( row != null ) {
            rowElement = dataElement.addElement( "DATA-ROW" ); //$NON-NLS-1$
            for ( Object element : row ) {
              if ( element == null ) {
                rowElement.addElement( "DATA-ITEM" ).setText( "" ); //$NON-NLS-1$ //$NON-NLS-2$
              } else {
                rowElement.addElement( "DATA-ITEM" ).setText( element.toString() ); //$NON-NLS-1$
              }
            }
            row = results.next();
          }
        }
      }
    }

    return doc;
  }

  public void setAction( final int action ) {
    this.action = action;
  }

  public int getAction() {
    return action;
  }

  public void setDomainName( final String domainName ) {
    this.domainName = domainName;
  }

  public String getDomainName() {
    return domainName;
  }

  public IParameterProvider getParameters() {
    return parameters;
  }

  public void setParameters( final IParameterProvider parameters ) {
    this.parameters = parameters;
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId( final String modelId ) {
    this.modelId = modelId;
  }

  public String getColumnId() {
    return columnId;
  }

  public void setColumnId( final String columnId ) {
    this.columnId = columnId;
  }

}
