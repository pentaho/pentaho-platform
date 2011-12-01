<%@ page session="true" contentType="text/html;"
  import="
  java.util.*,
  java.io.ByteArrayOutputStream,
  javax.sql.DataSource,
  org.dom4j.DocumentHelper,
  org.dom4j.Element,
  org.dom4j.Document,
  org.pentaho.platform.util.VersionHelper,
  org.pentaho.platform.util.UUIDUtil,
    org.pentaho.platform.util.StringUtil,
  org.pentaho.platform.util.web.SimpleUrlFactory,
  org.pentaho.platform.util.messages.LocaleHelper,
    org.pentaho.platform.api.data.IDBDatasourceService,
    org.pentaho.platform.api.engine.IPentahoSession,
    org.pentaho.platform.api.engine.ISolutionEngine,
    org.pentaho.platform.api.engine.IRuntimeContext,
    org.pentaho.platform.api.repository.ISubscriptionRepository,
  org.pentaho.platform.engine.core.output.SimpleOutputHandler,
  org.pentaho.platform.engine.core.system.PentahoSystem,
  org.pentaho.platform.engine.services.solution.SimpleParameterSetter,
  org.pentaho.platform.engine.core.solution.ActionInfo,
  org.pentaho.platform.engine.core.system.PentahoSessionHolder,
    org.pentaho.platform.web.http.WebTemplateHelper,
    org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
    org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
    org.pentaho.platform.web.jsp.messages.Messages,
 	org.pentaho.commons.connection.IPentahoConnection,
 	org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection,
	org.pentaho.commons.connection.IPentahoResultSet,
	org.pentaho.platform.api.data.IDBDatasourceService,
	org.pentaho.platform.api.engine.IConnectionUserRoleMapper,
	org.pentaho.platform.engine.core.system.IPentahoLoggingConnection,
	org.pentaho.platform.engine.core.system.PentahoSessionHolder,
	org.pentaho.platform.engine.core.system.PentahoSystem,
  org.pentaho.platform.plugin.action.mondrian.PivotViewComponent,
  org.pentaho.platform.plugin.action.mondrian.AnalysisSaver,
  org.pentaho.platform.plugin.action.mondrian.MissingParameterException,
  org.pentaho.platform.repository.subscription.Subscription,
  org.pentaho.platform.repository.subscription.SubscriptionHelper,
  com.tonbeller.jpivot.table.TableComponent,
  com.tonbeller.jpivot.olap.model.OlapModel,
  com.tonbeller.jpivot.tags.OlapModelProxy,
  com.tonbeller.jpivot.olap.model.OlapModelDecorator,
  com.tonbeller.jpivot.olap.query.MdxOlapModel,
  com.tonbeller.jpivot.mondrian.MondrianModel,
  com.tonbeller.jpivot.chart.ChartComponent,
  com.tonbeller.wcf.form.FormComponent,
  com.tonbeller.wcf.controller.MultiPartEnabledRequest,
  org.apache.log4j.MDC,
  com.tonbeller.wcf.controller.RequestContext,
  com.tonbeller.wcf.controller.RequestContextFactoryFinder,
  javax.servlet.jsp.jstl.core.Config,
  com.tonbeller.wcf.controller.Controller,
  com.tonbeller.wcf.controller.WcfController,
  org.owasp.esapi.ESAPI"%>
<jsp:directive.page
  import="org.pentaho.platform.api.repository.ISolutionRepository" />
<%
 // the following code replaces wcf's RequestFilter due to session based
 // synchronization logic that is no longer necessary. (PDB-369)
 MultiPartEnabledRequest mprequest = new MultiPartEnabledRequest((HttpServletRequest) request);
 HttpSession mpsession = mprequest.getSession(true);
 MDC.put("SessionID", mpsession.getId());
 String cpath = mprequest.getContextPath();
 mprequest.setAttribute("context", cpath);
 RequestContext wcfcontext = RequestContextFactoryFinder.createContext(mprequest, response, true);
 try {
   Config.set(mprequest, Config.FMT_LOCALE, wcfcontext.getLocale());
   Controller controller = WcfController.instance(session);
   controller.request(wcfcontext);
%>
<%@ 
   taglib uri="http://www.tonbeller.com/jpivot" prefix="jp"%>
<%@ 
   taglib uri="http://www.tonbeller.com/wcf" prefix="wcf"%>
<%@ 
   taglib prefix="c" uri="http://java.sun.com/jstl/core"%>
<%

/*
 * Copyright 2006 - 2010 Pentaho Corporation.  All rights reserved. 
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
 */

  response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
  PentahoSystem.systemEntryPoint();
  try {
    IPentahoSession userSession = PentahoSessionHolder.getSession();

  String pivotId = null;
  if (request.getParameter("pivotId") != null) {
    pivotId = request.getParameter("pivotId");
  } else {
    pivotId = UUIDUtil.getUUIDAsString();
    if( pivotId == null ) {
      // TODO need to log an error here
      return;
    }
  }

  // this allows navigation renderer to have access to the pivotId, which it uses
  // in an href link back to itself.
  Map map = new HashMap();
  map.put("pivotId", pivotId);
  request.setAttribute("com.tonbeller.wcf.component.RendererParameters", map);
  
  int saveResult = 0;
  String saveMessage = "";
  String queryId = "query"+pivotId; //$NON-NLS-1$
  String mdxEditId = "mdxedit" + pivotId;
  String tableId = "table" + pivotId;
  String titleId = PivotViewComponent.TITLE+pivotId;
  String optionsId = "pivot-"+PivotViewComponent.OPTIONS+"-"+pivotId; //$NON-NLS-1$
  String chartId = "chart" + pivotId;
  String naviId = "navi" + pivotId;
  String sortFormId = "sortform" + pivotId;
  String chartFormId = "chartform" + pivotId;
  String printId = "print" + pivotId;
  String printFormId = "printform" + pivotId;
  String drillThroughTableId = queryId + ".drillthroughtable";
  String toolbarId = "toolbar" + pivotId;

  // Internal JPivot References, if available.  Note that these references change
  // after each creation tag within the JSP.
  OlapModel _olapModel = (OlapModel)session.getAttribute(queryId);
  FormComponent _mdxEdit = (FormComponent)session.getAttribute(mdxEditId);
  TableComponent _table = (TableComponent) session.getAttribute(tableId);
  ChartComponent _chart = (ChartComponent) session.getAttribute(chartId);

  boolean authenticated = userSession.getName() != null;
  String pageName = "Pivot"; //$NON-NLS-1$

  String solutionName = request.getParameter( "solution" ); //$NON-NLS-1$
  String actionPath = request.getParameter( "path" ); //$NON-NLS-1$
  String actionName = request.getParameter( "action" ); //$NON-NLS-1$

  String actionReference = (String) session.getAttribute("pivot-action-"+pivotId); //$NON-NLS-1$

  String subscribeResult = null;
  String subscribeAction = request.getParameter( "subscribe" ); //$NON-NLS-1$
  String saveAction = request.getParameter( "save-action"); //$NON-NLS-1$

  String dataSource = null;
  String catalogUri = null;
  String query = null;  
  String role  = null;
  String pivotTitle = (String) session.getAttribute( "pivot-"+PivotViewComponent.TITLE+"-"+pivotId ); //$NON-NLS-1$
  String actionTitle = (String) session.getAttribute( "action-"+PivotViewComponent.TITLE+"-"+pivotId );;
  ArrayList options = (ArrayList) session.getAttribute( optionsId );
  boolean chartChange = false;
  boolean showGrid = true;
  
  if( session.getAttribute( "save-message-01") != null ) {
    saveMessage = ((String) session.getAttribute("save-message-01"));
  }
  
  if( session.getAttribute( "pivot-"+PivotViewComponent.SHOWGRID+"-"+pivotId ) != null ) {
    showGrid = ((Boolean) session.getAttribute("pivot-"+PivotViewComponent.SHOWGRID+"-"+pivotId)).booleanValue();
  }
  if (session.getAttribute( "pivot-"+PivotViewComponent.MODEL+"-"+pivotId ) != null ) { //$NON-NLS-1$
      catalogUri = (String)session.getAttribute( "pivot-"+PivotViewComponent.MODEL+"-"+pivotId );
  }
  
  int chartType = 1;
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTTYPE+"-"+pivotId ) != null ) { //$NON-NLS-1$
    chartType = ((Integer) session.getAttribute( "pivot-"+PivotViewComponent.CHARTTYPE+"-"+pivotId )).intValue(); //$NON-NLS-1$
  }
  String chartLocation = "bottom"; //$NON-NLS-1$
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTLOCATION+"-"+pivotId ) != null ) { //$NON-NLS-1$
    chartLocation = (String) session.getAttribute( "pivot-"+PivotViewComponent.CHARTLOCATION+"-"+pivotId );
  }
  int chartWidth = -1;
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTWIDTH+"-"+pivotId ) != null ) {
    chartWidth = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTWIDTH+"-"+pivotId )).intValue();
  }
  int chartHeight = -1;
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTHEIGHT+"-"+pivotId ) != null ) {
    chartHeight = ((Integer) session.getAttribute( "pivot-"+PivotViewComponent.CHARTHEIGHT+"-"+pivotId )).intValue();
  }
  boolean chartDrillThroughEnabled = false;
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTDRILLTHROUGHENABLED+"-"+pivotId ) != null ) {
    chartDrillThroughEnabled = ((Boolean) session.getAttribute( "pivot-"+PivotViewComponent.CHARTDRILLTHROUGHENABLED+"-"+pivotId )).booleanValue();
  }
  String chartTitle = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLE+"-"+pivotId) != null ) {
    chartTitle = session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLE+"-"+pivotId).toString() ;
  }
  String chartTitleFontFamily = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTFAMILY+"-"+pivotId) != null ) {
    chartTitleFontFamily = session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTFAMILY+"-"+pivotId).toString();
  }
  int chartTitleFontStyle = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTSTYLE+"-"+pivotId) != null ) {
    chartTitleFontStyle = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTSTYLE+"-"+pivotId)).intValue();
  }
  int chartTitleFontSize = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTSIZE+"-"+pivotId) != null ) {
    chartTitleFontSize = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTSIZE+"-"+pivotId)).intValue();
  }
  String chartHorizAxisLabel = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTHORIZAXISLABEL+"-"+pivotId) != null ) {
    chartHorizAxisLabel = session.getAttribute( "pivot-"+PivotViewComponent.CHARTHORIZAXISLABEL+"-"+pivotId).toString();
  }
  String chartVertAxisLabel = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTVERTAXISLABEL+"-"+pivotId) != null ) {
    chartVertAxisLabel = session.getAttribute( "pivot-"+PivotViewComponent.CHARTVERTAXISLABEL+"-"+pivotId).toString();
  }
  String chartAxisLabelFontFamily = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTFAMILY+"-"+pivotId) != null ) {
    chartAxisLabelFontFamily = session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTFAMILY+"-"+pivotId).toString();
  }
  int chartAxisLabelFontStyle = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTSTYLE+"-"+pivotId) != null ) {
    chartAxisLabelFontStyle = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTSTYLE+"-"+pivotId)).intValue();
  }
  int chartAxisLabelFontSize = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTSIZE+"-"+pivotId) != null ) {
    chartAxisLabelFontSize = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTSIZE+"-"+pivotId)).intValue();
  }
  String chartAxisTickFontFamily = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTFAMILY+"-"+pivotId) != null ) {
    chartAxisTickFontFamily = session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTFAMILY+"-"+pivotId).toString();
  }
  int chartAxisTickFontStyle = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTSTYLE+"-"+pivotId) != null ) {
    chartAxisTickFontStyle = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTSTYLE+"-"+pivotId)).intValue();
  }
  int chartAxisTickFontSize = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTSIZE+"-"+pivotId) != null ) {
    chartAxisTickFontSize = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTSIZE+"-"+pivotId)).intValue();
  }
  int chartAxisTickLabelRotation = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKLABELROTATION+"-"+pivotId) != null ) {
    chartAxisTickLabelRotation = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKLABELROTATION+"-"+pivotId)).intValue();
  }
  boolean chartShowLegend = false;
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTSHOWLEGEND+"-"+pivotId ) != null ) {
    chartShowLegend = ((Boolean) session.getAttribute( "pivot-"+PivotViewComponent.CHARTSHOWLEGEND+"-"+pivotId )).booleanValue();
  }
  int chartLegendLocation = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDLOCATION+"-"+pivotId) != null ) {
    chartLegendLocation = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDLOCATION+"-"+pivotId)).intValue();
  }
  String chartLegendFontFamily = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTFAMILY+"-"+pivotId) != null ) {
    chartLegendFontFamily = session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTFAMILY+"-"+pivotId).toString();
  }
  int chartLegendFontStyle = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTSTYLE+"-"+pivotId) != null ) {
    chartLegendFontStyle = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTSTYLE+"-"+pivotId)).intValue();
  }
    int chartLegendFontSize = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTSIZE+"-"+pivotId) != null ) {
    chartLegendFontSize = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTSIZE+"-"+pivotId)).intValue();
  }
    boolean chartShowSlicer = false;
  if ( session.getAttribute( "pivot-"+PivotViewComponent.CHARTSHOWSLICER+"-"+pivotId ) != null ) {
    chartShowSlicer = ((Boolean) session.getAttribute( "pivot-"+PivotViewComponent.CHARTSHOWSLICER+"-"+pivotId )).booleanValue();
  }
    int chartSlicerLocation = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERLOCATION+"-"+pivotId) != null ) {
    chartSlicerLocation = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERLOCATION+"-"+pivotId)).intValue();
  }
  int chartSlicerAlignment = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERALIGNMENT+"-"+pivotId) != null ) {
    chartSlicerAlignment = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERALIGNMENT+"-"+pivotId)).intValue();
  }
  String chartSlicerFontFamily = "";
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTFAMILY+"-"+pivotId) != null ) {
    chartSlicerFontFamily = session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTFAMILY+"-"+pivotId).toString();
  }
  int chartSlicerFontStyle = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTSTYLE+"-"+pivotId) != null ) {
    chartSlicerFontStyle = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTSTYLE+"-"+pivotId)).intValue();
  }
    int chartSlicerFontSize = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTSIZE+"-"+pivotId) != null ) {
    chartSlicerFontSize = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTSIZE+"-"+pivotId)).intValue();
  }   
    int chartBackgroundR = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDR+"-"+pivotId) != null ) {
    chartBackgroundR = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDR+"-"+pivotId)).intValue();
  } 
    int chartBackgroundG = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDG+"-"+pivotId) != null ) {
    chartBackgroundG = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDG+"-"+pivotId)).intValue();
  }
    int chartBackgroundB = -1;
  if (session.getAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDB+"-"+pivotId) != null ) {
    chartBackgroundB = ((Integer)session.getAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDB+"-"+pivotId)).intValue();
  }
      
  if( solutionName != null && actionPath != null && actionName != null ) {
      // we need to initialize from an action sequence document

    IRuntimeContext context = null;
    try {
      context = getRuntimeForQuery( solutionName, actionPath, actionName, request, userSession );
      if( context != null && context.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
          if (context.getOutputNames().contains(PivotViewComponent.MODEL)) {
            try {
                catalogUri = context.getOutputParameter( PivotViewComponent.MODEL ).getStringValue(); //$NON-NLS-1$
                session.setAttribute("pivot-"+PivotViewComponent.MODEL+"-"+pivotId, catalogUri);
              } catch (Exception e) {
              }
          }
        
        dataSource = context.getOutputParameter( PivotViewComponent.CONNECTION ).getStringValue(); //$NON-NLS-1$
        query = context.getOutputParameter( "mdx" ).getStringValue(); //$NON-NLS-1$

        if (catalogUri == null || dataSource == null || query == null) {
          throw new Exception(Messages.getInstance().getErrorString("UI.ERROR_0003_XACTION_INVALID_OUTPUTS", ActionInfo.buildSolutionPath(solutionName,actionPath,actionName), "Catalog URI=" + catalogUri + "; Data Source=" + dataSource + "; MDX Query=" + query, "isPromptPending=" + context.isPromptPending()));
        }
        
        if (context.getOutputNames().contains(PivotViewComponent.ROLE)) { //$NON-NLS-1$
          role = context.getOutputParameter( PivotViewComponent.ROLE ).getStringValue(); //$NON-NLS-1$
        }

        if ((role==null) || (role.trim().length()==0)){
          // Only if the action sequence/requester hasn't already injected a role in here do this.
          if(PentahoSystem.getObjectFactory().objectDefined(MDXConnection.MDX_CONNECTION_MAPPER_KEY)) {
            IConnectionUserRoleMapper mondrianUserRoleMapper = PentahoSystem.get(IConnectionUserRoleMapper.class, MDXConnection.MDX_CONNECTION_MAPPER_KEY, null);
            if (mondrianUserRoleMapper != null) {
              // Do role mapping
              String[] validMondrianRolesForUser = mondrianUserRoleMapper.mapConnectionRoles(PentahoSessionHolder.getSession(), catalogUri);
              if ( (validMondrianRolesForUser != null) && (validMondrianRolesForUser.length>0) ) {
                StringBuffer buff = new StringBuffer();
                String aRole = null;
                for (int i=0; i<validMondrianRolesForUser.length; i++) {
                  aRole = validMondrianRolesForUser[i];
                  // According to http://mondrian.pentaho.org/documentation/configuration.php
                  // double-comma escapes a comma
                  if (i>0) {
                    buff.append(",");
                  }
                  buff.append(aRole.replaceAll(",", ",,"));
                }
                role = buff.toString();
              }
            }
          }
        }

        if( context.getOutputNames().contains( PivotViewComponent.CHARTTYPE ) ) { //$NON-NLS-1$
          try {
            chartType = Integer.parseInt( context.getOutputParameter( PivotViewComponent.CHARTTYPE ).getStringValue() ); //$NON-NLS-1$
            session.setAttribute( "pivot-"+PivotViewComponent.CHARTTYPE+"-"+pivotId, new Integer(chartType) ); //$NON-NLS-1$
            
          } catch (Exception e) {
          }
        } else {
          chartType = 1;
        }
        if (context.getOutputNames().contains(PivotViewComponent.SHOWGRID) ) {
          try {
            showGrid = Boolean.valueOf(context.getOutputParameter( PivotViewComponent.SHOWGRID ).getStringValue()).booleanValue();
            session.setAttribute("pivot-"+PivotViewComponent.SHOWGRID+"-"+pivotId, new Boolean(showGrid));
          } catch (Exception e) {
          }
        } else {
          showGrid = true;
        }
        if (context.getOutputNames().contains(PivotViewComponent.CHARTWIDTH) ) { //$NON-NLS-1$
          try {
            chartWidth = Integer.parseInt( context.getOutputParameter( PivotViewComponent.CHARTWIDTH ).getStringValue() ); //$NON-NLS-1$
            session.setAttribute( "pivot-"+PivotViewComponent.CHARTWIDTH+"-"+pivotId, new Integer(chartWidth) ); //$NON-NLS-1$
          } catch (Exception e) {
          }
        } else {
          chartWidth = 500;  // Default from ChartComponent
        }
        if (context.getOutputNames().contains(PivotViewComponent.CHARTHEIGHT) ) { //$NON-NLS-1$
          try {
            chartHeight = Integer.parseInt( context.getOutputParameter( PivotViewComponent.CHARTHEIGHT ).getStringValue() ); //$NON-NLS-1$
            session.setAttribute( "pivot-"+PivotViewComponent.CHARTHEIGHT+"-"+pivotId, new Integer(chartHeight) ); //$NON-NLS-1$
          } catch (Exception e) {
          }
        } else {
          chartHeight = 300; // Default from ChartComponent
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTLOCATION ) ) { //$NON-NLS-1$
          chartLocation = context.getOutputParameter( PivotViewComponent.CHARTLOCATION ).getStringValue(); //$NON-NLS-1$
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTLOCATION+"-"+pivotId, chartLocation ); //$NON-NLS-1$
        } else {
          chartLocation = "none"; //$NON-NLS-1$
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTDRILLTHROUGHENABLED )) {
          chartDrillThroughEnabled = Boolean.valueOf(context.getOutputParameter( PivotViewComponent.CHARTDRILLTHROUGHENABLED ).getStringValue()).booleanValue();
          session.setAttribute("pivot-"+PivotViewComponent.CHARTDRILLTHROUGHENABLED+"-"+pivotId, new Boolean(chartDrillThroughEnabled));
        } else {
          chartDrillThroughEnabled = false;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTTITLE ) ) {
          chartTitle = context.getOutputParameter( PivotViewComponent.CHARTTITLE ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTTITLE+"-"+pivotId, chartTitle );
        } else {
          chartTitle = "";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTTITLEFONTFAMILY ) ) {
          chartTitleFontFamily = context.getOutputParameter( PivotViewComponent.CHARTTITLEFONTFAMILY ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTFAMILY+"-"+pivotId, chartTitleFontFamily );
        } else {
          chartTitleFontFamily = "SansSerif";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTTITLEFONTSTYLE ) ) {
          chartTitleFontStyle = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTTITLEFONTSTYLE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTSTYLE+"-"+pivotId, new Integer(chartTitleFontStyle));
        } else {
          chartTitleFontStyle = java.awt.Font.BOLD;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTTITLEFONTSIZE ) ) {
          chartTitleFontSize = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTTITLEFONTSIZE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTTITLEFONTSIZE+"-"+pivotId, new Integer(chartTitleFontSize));
        } else {
          chartTitleFontSize = 18;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTHORIZAXISLABEL ) ) {
          chartHorizAxisLabel = context.getOutputParameter( PivotViewComponent.CHARTHORIZAXISLABEL ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTHORIZAXISLABEL+"-"+pivotId, chartHorizAxisLabel );
        } else {
          chartHorizAxisLabel = "";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTVERTAXISLABEL ) ) {
          chartVertAxisLabel = context.getOutputParameter( PivotViewComponent.CHARTVERTAXISLABEL ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTVERTAXISLABEL+"-"+pivotId, chartVertAxisLabel );
        } else {
          chartVertAxisLabel = "";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISLABELFONTFAMILY ) ) {
          chartAxisLabelFontFamily = context.getOutputParameter( PivotViewComponent.CHARTAXISLABELFONTFAMILY ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTFAMILY+"-"+pivotId, chartAxisLabelFontFamily );
        } else {
          chartAxisLabelFontFamily = "SansSerif";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISLABELFONTSTYLE ) ) {
          chartAxisLabelFontStyle = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTAXISLABELFONTSTYLE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTSTYLE+"-"+pivotId, new Integer(chartAxisLabelFontStyle));
        } else {
          chartAxisLabelFontStyle = java.awt.Font.PLAIN;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISLABELFONTSIZE ) ) {
          chartAxisLabelFontSize = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTAXISLABELFONTSIZE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISLABELFONTSIZE+"-"+pivotId, new Integer(chartAxisLabelFontSize));
        } else {
          chartAxisLabelFontSize = 12;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISTICKFONTFAMILY ) ) {
          chartAxisTickFontFamily = context.getOutputParameter( PivotViewComponent.CHARTAXISTICKFONTFAMILY ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTFAMILY+"-"+pivotId, chartAxisTickFontFamily );
        } else {
          chartAxisTickFontFamily = "SansSerif";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISTICKFONTSTYLE ) ) {
          chartAxisTickFontStyle = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTAXISTICKFONTSTYLE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTSTYLE+"-"+pivotId, new Integer(chartAxisTickFontStyle));
        } else {
          chartAxisTickFontStyle = java.awt.Font.PLAIN;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISTICKFONTSIZE ) ) {
          chartAxisTickFontSize = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTAXISTICKFONTSIZE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKFONTSIZE+"-"+pivotId, new Integer(chartAxisTickFontSize));
        } else {
          chartAxisTickFontSize = 12;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTAXISTICKLABELROTATION ) ) {
          chartAxisTickLabelRotation = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTAXISTICKLABELROTATION ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTAXISTICKLABELROTATION+"-"+pivotId, new Integer(chartAxisTickLabelRotation));
        } else {
          chartAxisTickLabelRotation = 30;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTSHOWLEGEND )) {
          chartShowLegend = Boolean.valueOf(context.getOutputParameter( PivotViewComponent.CHARTSHOWLEGEND ).getStringValue()).booleanValue();
          session.setAttribute("pivot-"+PivotViewComponent.CHARTSHOWLEGEND+"-"+pivotId, new Boolean(chartShowLegend));
        } else {
          chartShowLegend = true;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTLEGENDLOCATION ) ) {
          chartLegendLocation = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTLEGENDLOCATION ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDLOCATION+"-"+pivotId, new Integer(chartLegendLocation));
        } else {
          chartLegendLocation = 3;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTLEGENDFONTFAMILY ) ) {
          chartLegendFontFamily = context.getOutputParameter( PivotViewComponent.CHARTLEGENDFONTFAMILY ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTFAMILY+"-"+pivotId, chartLegendFontFamily );
        } else {
          chartLegendFontFamily = "SansSerif";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTLEGENDFONTSTYLE ) ) {
          chartLegendFontStyle = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTLEGENDFONTSTYLE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTSTYLE+"-"+pivotId, new Integer(chartLegendFontStyle));
        } else {
          chartLegendFontStyle = java.awt.Font.PLAIN;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTLEGENDFONTSIZE ) ) {
          chartLegendFontSize = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTLEGENDFONTSIZE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTLEGENDFONTSIZE+"-"+pivotId, new Integer(chartLegendFontSize));
        } else {
          chartLegendFontSize = 10;
        }
          if( context.getOutputNames().contains( PivotViewComponent.CHARTSHOWSLICER )) {
          chartShowSlicer = Boolean.valueOf(context.getOutputParameter( PivotViewComponent.CHARTSHOWSLICER ).getStringValue()).booleanValue();
          session.setAttribute("pivot-"+PivotViewComponent.CHARTSHOWSLICER+"-"+pivotId, new Boolean(chartShowSlicer));
        } else {
          chartShowSlicer = true;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTSLICERLOCATION ) ) {
          chartSlicerLocation = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTSLICERLOCATION ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTSLICERLOCATION+"-"+pivotId, new Integer(chartSlicerLocation));
        } else {
          chartSlicerLocation = 1;
        }
            if( context.getOutputNames().contains( PivotViewComponent.CHARTSLICERALIGNMENT ) ) {
          chartSlicerAlignment = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTSLICERALIGNMENT ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTSLICERALIGNMENT+"-"+pivotId, new Integer(chartSlicerAlignment));
        } else {
          chartSlicerAlignment = 3;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTSLICERFONTFAMILY ) ) {
          chartSlicerFontFamily = context.getOutputParameter( PivotViewComponent.CHARTSLICERFONTFAMILY ).getStringValue();
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTFAMILY+"-"+pivotId, chartSlicerFontFamily );
        } else {
          chartSlicerFontFamily = "SansSerif";
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTSLICERFONTSTYLE ) ) {
          chartSlicerFontStyle = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTSLICERFONTSTYLE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTSTYLE+"-"+pivotId, new Integer(chartSlicerFontStyle));
        } else {
          chartSlicerFontStyle = java.awt.Font.PLAIN;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTSLICERFONTSIZE ) ) {
          chartSlicerFontSize = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTSLICERFONTSIZE ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTSLICERFONTSIZE+"-"+pivotId, new Integer(chartSlicerFontSize));
        } else {
          chartSlicerFontSize = 12;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTBACKGROUNDR ) ) {
          chartBackgroundR = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTBACKGROUNDR ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDR+"-"+pivotId, new Integer(chartBackgroundR));
        } else {
          chartBackgroundR = 255;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTBACKGROUNDG ) ) {
          chartBackgroundG = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTBACKGROUNDG ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDG+"-"+pivotId, new Integer(chartBackgroundG));
        } else {
          chartBackgroundG = 255;
        }
        if( context.getOutputNames().contains( PivotViewComponent.CHARTBACKGROUNDB ) ) {
          chartBackgroundB = Integer.parseInt(context.getOutputParameter( PivotViewComponent.CHARTBACKGROUNDB ).getStringValue());
          session.setAttribute( "pivot-"+PivotViewComponent.CHARTBACKGROUNDB+"-"+pivotId, new Integer(chartBackgroundB));
        } else {
          chartBackgroundB = 255;
        }
        
        chartChange = true;
        
        options = (ArrayList) context.getOutputParameter( PivotViewComponent.OPTIONS ).getValue(); //$NON-NLS-1$
        pivotTitle = context.getOutputParameter( PivotViewComponent.TITLE ).getStringValue(); //$NON-NLS-1$
        actionTitle = context.getActionTitle();
        if( options != null ) {
          session.setAttribute( optionsId, options );
        } else {
          session.removeAttribute( optionsId );
        }
        actionReference = solutionName+"/"+actionPath+"/"+actionName; //$NON-NLS-1$ //$NON-NLS-2$

        session.setAttribute( "pivot-action-"+pivotId, actionReference ); //$NON-NLS-1$
        session.setAttribute( "pivot-"+PivotViewComponent.TITLE+"-"+pivotId, pivotTitle ); //$NON-NLS-1$
        session.setAttribute( "action-"+PivotViewComponent.TITLE+"-"+pivotId, actionTitle ); //$NON-NLS-1$
      }
    } finally {
      if( context != null ) {
        context.dispose();
      }
    }

  }

  if( pivotTitle == null ) {
    pivotTitle = Messages.getInstance().getString("UI.USER_ANALYSIS_UNTITLED_PIVOT_NAME"); //$NON-NLS-1$
  }

  // Take care of saving this xaction
  if ( saveAction != null ) {
    // Get the current mdx
    String mdx = null;
    String connectString = null;
    if( _table != null ) {
    OlapModel olapModel = _table.getOlapModel();
    while( olapModel != null ) {
      if( olapModel instanceof OlapModelProxy ) {
      OlapModelProxy proxy = (OlapModelProxy) olapModel;
      olapModel = proxy.getDelegate();
      }
      if( olapModel instanceof OlapModelDecorator) {
      OlapModelDecorator decorator = (OlapModelDecorator) olapModel;
      olapModel = decorator.getDelegate();
      }
      if( olapModel instanceof MdxOlapModel) {
      MdxOlapModel model = (MdxOlapModel) olapModel;
      mdx = model.getCurrentMdx();
      olapModel = null;
      }
    }
    }
      
    HashMap props = new HashMap();
      
    props.put(PivotViewComponent.MODEL, catalogUri);
    props.put(PivotViewComponent.CONNECTION, dataSource);
    props.put(PivotViewComponent.ROLE, role);
    props.put(PivotViewComponent.SHOWGRID, new Boolean(showGrid));
    props.put("query", mdx);
    props.put(PivotViewComponent.OPTIONS, options);
    props.put(PivotViewComponent.TITLE, request.getParameter("save-title"));
    props.put("actionreference", actionReference);
  
    if(_chart != null){
      props.put(PivotViewComponent.CHARTTYPE, new Integer(_chart.getChartType()));
      props.put(PivotViewComponent.CHARTWIDTH, new Integer(_chart.getChartWidth()));
      props.put(PivotViewComponent.CHARTHEIGHT, new Integer(_chart.getChartHeight()));
      if (_chart.isVisible() && chartLocation.equalsIgnoreCase("none")){
        chartLocation = "bottom";
      }
      props.put(PivotViewComponent.CHARTLOCATION, _chart.isVisible() ? chartLocation : "none");
      props.put(PivotViewComponent.CHARTDRILLTHROUGHENABLED, new Boolean(_chart.isDrillThroughEnabled()));
      props.put(PivotViewComponent.CHARTTITLE, _chart.getChartTitle());
      props.put(PivotViewComponent.CHARTTITLEFONTFAMILY, _chart.getFontName());
      props.put(PivotViewComponent.CHARTTITLEFONTSTYLE, new Integer(_chart.getFontStyle()));
      props.put(PivotViewComponent.CHARTTITLEFONTSIZE, new Integer(_chart.getFontSize()));
      props.put(PivotViewComponent.CHARTHORIZAXISLABEL, _chart.getHorizAxisLabel());
      props.put(PivotViewComponent.CHARTVERTAXISLABEL, _chart.getVertAxisLabel());
      props.put(PivotViewComponent.CHARTAXISLABELFONTFAMILY, _chart.getAxisFontName());
      props.put(PivotViewComponent.CHARTAXISLABELFONTSTYLE, new Integer(_chart.getAxisFontStyle()));
      props.put(PivotViewComponent.CHARTAXISLABELFONTSIZE, new Integer(_chart.getAxisFontSize()));
      props.put(PivotViewComponent.CHARTAXISTICKFONTFAMILY, _chart.getAxisTickFontName());
      props.put(PivotViewComponent.CHARTAXISTICKFONTSTYLE, new Integer(_chart.getAxisTickFontStyle()));
      props.put(PivotViewComponent.CHARTAXISTICKFONTSIZE, new Integer(_chart.getAxisTickFontSize()));
      props.put(PivotViewComponent.CHARTAXISTICKLABELROTATION, new Integer(_chart.getTickLabelRotate()));
      props.put(PivotViewComponent.CHARTSHOWLEGEND, new Boolean(_chart.getShowLegend()));
      props.put(PivotViewComponent.CHARTLEGENDLOCATION, new Integer(_chart.getLegendPosition()));
      props.put(PivotViewComponent.CHARTLEGENDFONTFAMILY, _chart.getLegendFontName());
      props.put(PivotViewComponent.CHARTLEGENDFONTSTYLE, new Integer(_chart.getLegendFontStyle()));
      props.put(PivotViewComponent.CHARTLEGENDFONTSIZE, new Integer(_chart.getLegendFontSize()));
      props.put(PivotViewComponent.CHARTSHOWSLICER, new Boolean(_chart.isShowSlicer()));
      props.put(PivotViewComponent.CHARTSLICERLOCATION, new Integer(_chart.getSlicerPosition()));
      props.put(PivotViewComponent.CHARTSLICERALIGNMENT, new Integer(_chart.getSlicerAlignment()));
        props.put(PivotViewComponent.CHARTSLICERFONTFAMILY, _chart.getSlicerFontName());
      props.put(PivotViewComponent.CHARTSLICERFONTSTYLE, new Integer(_chart.getSlicerFontStyle()));
      props.put(PivotViewComponent.CHARTSLICERFONTSIZE, new Integer(_chart.getSlicerFontSize()));
      props.put(PivotViewComponent.CHARTBACKGROUNDR, new Integer(_chart.getBgColorR()));
      props.put(PivotViewComponent.CHARTBACKGROUNDG, new Integer(_chart.getBgColorG()));
      props.put(PivotViewComponent.CHARTBACKGROUNDB, new Integer(_chart.getBgColorB()));
    }

    if (( "save".equals(saveAction)) || ("saveAs".equals(saveAction)))  {    
      
      // Overwrite is true, because the saveAs dialog checks for overwrite, and we never
      // would have gotten here unless the user selected to overwrite the file. 
    try{
      saveResult = AnalysisSaver.saveAnalysis(userSession, props, request.getParameter("save-path"), request.getParameter("save-file"), true);
      switch (saveResult) {
        case ISolutionRepository.FILE_ADD_SUCCESSFUL: 
          saveMessage = Messages.getInstance().getString("UI.USER_SAVE_SUCCESS");
          // only set the session attribute on success, it's the only path that requires it
          session.setAttribute( "save-message-01", saveMessage); //$NON-NLS-1$
          break;
        case ISolutionRepository.FILE_EXISTS:
          // Shouldn't ever get here, since we pass overwrite=true;
            break;
        case ISolutionRepository.FILE_ADD_FAILED:
          saveMessage = Messages.getInstance().getString("UI.USER_SAVE_FAILED_GENERAL");
          break;
        case ISolutionRepository.FILE_ADD_INVALID_PUBLISH_PASSWORD:
          // There is no publish password on this save...
          break;
        case ISolutionRepository.FILE_ADD_INVALID_USER_CREDENTIALS:
          saveMessage = Messages.getInstance().getString("UI.USER_SAVE_FAILED_INVALID_USER_CREDS");
          break;
        case 0:
            saveMessage="";
            break;
      } 
      } catch (Throwable e){
      saveResult = ISolutionRepository.FILE_ADD_FAILED;
        saveMessage = e.getMessage();
      }
    }
  }
 
  if( query != null ) { 
    IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get(IDBDatasourceService.class, null);
    DataSource currDataSource = null; 
    try {
      currDataSource = datasourceService.getDataSource(dataSource);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    if (currDataSource != null) {
      request.setAttribute("currDataSource", currDataSource);
%>
<jp:mondrianQuery id="<%=queryId%>" dataSource="${currDataSource}"
  dynResolver="mondrian.i18n.LocalizingDynamicSchemaProcessor"
  dynLocale="<%= userSession.getLocale().toString() %>"
  role="<%=role%>" catalogUri="<%=catalogUri%>">
  <%=query%>
</jp:mondrianQuery>
<%
    } else {
%>
<jp:mondrianQuery id="<%=queryId%>" dataSource="<%=dataSource%>"
  dynResolver="mondrian.i18n.LocalizingDynamicSchemaProcessor"
  dynLocale="<%= userSession.getLocale().toString() %>"
  role="<%=role%>" catalogUri="<%=catalogUri%>">
  <%=query%>
</jp:mondrianQuery>
<% 
    }
  }

  _olapModel =  (OlapModel)session.getAttribute(queryId);
  session.setAttribute(titleId, pivotTitle);
%><html>
<head>
<title><%= Messages.getInstance().getString("UI.USER_ANALYSIS") %></title>
<meta http-equiv="Content-Type"
  content="text/html; charset=<%= LocaleHelper.getSystemEncoding() %>">
<link rel="stylesheet" type="text/css" href="jpivot/table/mdxtable.css">
<link rel="stylesheet" type="text/css" href="jpivot/navi/mdxnavi.css">
<link rel="stylesheet" type="text/css" href="wcf/form/xform.css">
<link rel="stylesheet" type="text/css" href="wcf/table/xtable.css">
<link rel="stylesheet" type="text/css" href="wcf/tree/xtree.css">
<link href="/pentaho-style/styles-new.css" rel="stylesheet"
  type="text/css" />
<link rel="shortcut icon" href="/pentaho-style/favicon.ico" />

<!-- ****************************************************************************************** -->
<!-- ****************        JAVASCRIPT FOR SAVE DIALOGS              ************************* -->
<!-- ****************************************************************************************** -->

<link href="adhoc/styles/repositoryBrowserStyles.css" rel="stylesheet" type="text/css" />
<link href="adhoc/styles/jpivot.css" rel="stylesheet" type="text/css" />
<!--[if IE]>
      <link href="adhoc/styles/jpivotIE6.css" rel="stylesheet" type="text/css"/>  
    <![endif]-->


<script src="wcf/scroller.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/xmltoken.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/util.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/dom.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/xpath.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/xslt.js" type="text/javascript"></script>

<script src="js/pentaho-ajax.js" type="text/javascript"></script>
<script src="js/utils.js" type="text/javascript"></script>
<script type="text/javascript">
    djConfig = { isDebug: false};
  </script>

<script src="js/dojo.js" type="text/javascript"></script>

<script type="text/javascript">
    dojo.registerModulePath("adhoc", "../adhoc/js");
  </script>

<script src="adhoc/js/common/ui/messages/Messages.js" type="text/javascript"></script>

<script type="text/javascript">
    Messages.addBundle("adhoc.ui.messages", "message_strings");
  </script>

<script src="adhoc/js/common/ui/MessageCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/server/WebServiceProxy.js" type="text/javascript"></script>
<script src="adhoc/js/common/util/StringUtils.js" type="text/javascript"></script>
<script src="adhoc/js/common/util/Status.js" type="text/javascript"></script>
<script src="adhoc/js/common/util/XmlUtil.js" type="text/javascript"></script>

<script src="adhoc/js/model/SolutionRepository.js" type="text/javascript"></script>

<script src="adhoc/js/common/ui/UIUtil.js" type="text/javascript"></script>
<script type="text/javascript">
    UIUtil.setImageFolderPath( "adhoc/images/" );
  </script>
<script src="adhoc/js/common/ui/HTMLCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/Logger.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/BusyCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/PickListCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/ListCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/ComboCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/Dialog.js" type="text/javascript"></script>

<script src="adhoc/js/common/ui/ButtonCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/ui/MessageCtrl.js" type="text/javascript"></script>

<script src="adhoc/js/ui/RepositoryBrowser.js" type="text/javascript"></script>
<script src="js/pivot/PivotRepositoryBrowserController.js" type="text/javascript"></script>

<script type="text/javascript"><!--
    
    var controller = null;
    var newActionName = null;
    var newSolution = null;
    var newActionPath = null;
    
    function cursor_wait() {
      document.body.style.cursor = 'wait';
    }
    
    function cursor_clear() {
      document.body.style.cursor = 'default';
    }
    
    //
    // This method creates a temporary form in the dom,
    // adds the inputs we want to post back to ourselves,
    // and then posts the form. Once the form is posted,
    // we remove the temporary form from the DOM.
    //
    function doSaveAsPost(postActionName, postActionSolution, postActionPath, postActionTitle) {
      var postForm = document.createElement("form");
      postForm.method="post" ;
      postForm.action = '<%= pageName %>';
      var anInput;
      // save-action
      anInput = document.createElement("input");
      anInput.setAttribute("name", "save-action");
      anInput.setAttribute("value", "saveAs");
      postForm.appendChild(anInput);
      // save-path
      anInput = document.createElement("input");
      anInput.setAttribute("name", "save-path");
      anInput.setAttribute("value", postActionSolution +'/'+postActionPath );
      postForm.appendChild(anInput);
      // save-file
      anInput = document.createElement("input");
      anInput.setAttribute("name", "save-file");
      anInput.setAttribute("value",  postActionName);
      postForm.appendChild(anInput);
      // save-title
      anInput = document.createElement("input");
      anInput.setAttribute("name", "save-title");
      anInput.setAttribute("value",  postActionTitle);
      postForm.appendChild(anInput);
      // pivotId
      anInput = document.createElement("input");
      anInput.setAttribute("name", "pivotId");
      anInput.setAttribute("value",  "<%=ESAPI.encoder().encodeForJavaScript(pivotId)%>");
      postForm.appendChild(anInput);
      
      document.body.appendChild(postForm); // Add the form into the document...
      postForm.submit(); // Post to ourselves...
      document.body.removeChild(postForm); // Remove the temporary form from the DOM.
    }
    
    function load(){
      xScrollerScroll(); 
      cursor_wait();
      controller = new PivotRepositoryBrowserController();
      controller.setOnAfterSaveCallback( function()
      {
        var nActionName = controller.getActionName();
        var nSolution = controller.getSolution();
        var nActionPath = controller.getActionPath();
        var nActionTitle = controller.getActionTitle()!=null?controller.getActionTitle():controller.getActionName();
        doSaveAsPost(nActionName, nSolution, nActionPath, nActionTitle);
      });
      cursor_clear();
      if (saveMessage != null && "" != saveMessage) {
        if (window.top != null && window.top.mantle_initialized) {
        window.top.mantle_refreshRepository();
          window.top.mantle_showMessage("Info", saveMessage);
        } else {
          alert(saveMessage);
        }
      }
      
//      if (window.top != null && window.top.mantle_initialized) { // Uncomment this line and the close brace to enable these buttons when in window only mode
        var tmpSaveButton = document.getElementById('folder-down');
        var tmpSaveAsButton = document.getElementById('folder-up');
        tmpSaveButton.parentNode.parentNode.removeChild(tmpSaveButton.parentNode);
        tmpSaveAsButton.parentNode.parentNode.removeChild(tmpSaveAsButton.parentNode);
//      }  // Uncomment this if above if is uncommented

      window.pivot_initialized = true;
       <%    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { %>
               try {
                 var mdxEditTxtBx = document.getElementById('<%=ESAPI.encoder().encodeForJavaScript(mdxEditId)%>.9');
                 if (mdxEditTxtBx) {
                   mdxEditTxtBx.readOnly = true;
                 }
               } catch (ignored) {
               }
       <%    }%>
    }
    
    function save() {
      cursor_wait();
    <%
      ActionInfo actionInfo = ActionInfo.parseActionString( actionReference );
      if (actionInfo != null) {
    %>
      var nActionName = "<%= ESAPI.encoder().encodeForJavaScript(actionInfo.getActionName()) %>";
      var nSolution = "<%= ESAPI.encoder().encodeForJavaScript(actionInfo.getSolutionName()) %>";
      var nActionPath = "<%= ESAPI.encoder().encodeForJavaScript(actionInfo.getPath()) %>";
      var nActionTitle = "<%= ESAPI.encoder().encodeForJavaScript(actionTitle) %>";
      doSaveAsPost(nActionName, nSolution, nActionPath, nActionTitle);
    <% } %>
      cursor_clear();
    }

    function saveAs() {
      controller.save();
    }

  --></script>

<%-- ****************************************************************************************** --%>
<%-- ****************************************************************************************** --%>
<%-- ****************************************************************************************** --%>


<script type="text/javascript">

    
    function doSubscribed() {
        var submitUrl = '';
      var action= document.getElementById('subscription-action').value;
      var target='';
        
      if( action == 'load' ) {
        submitUrl += '<%= pageName %>?subscribe=load&query=SampleData';
      }
      else 
      if( action == 'delete' ) {
        submitUrl += '<%= pageName %>?subscribe=delete';
      }

      var name= document.getElementById('subscription').value;
      submitUrl += '&subscribe-name='+encodeURIComponent(name);
          document.location.href=submitUrl;
        return false;
      }

    /***********************************************
    * Ajax Includes script-  Dynamic Drive DHTML code library (www.dynamicdrive.com)
    * This notice MUST stay intact for legal use
    * Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
    ***********************************************/

    //To include a page, invoke ajaxinclude("afile.htm") in the BODY of page
    //Included file MUST be from the same domain as the page displaying it.
    
    var rootdomain="http://"+window.location.hostname
    
    function ajaxinclude(url) {
      var page_request = false
      if (window.XMLHttpRequest) // if Mozilla, Safari etc
        page_request = new XMLHttpRequest()
      else if (window.ActiveXObject){ // if IE
        try {
          page_request = new ActiveXObject("Msxml2.XMLHTTP")
        } catch (e){
          try{
            page_request = new ActiveXObject("Microsoft.XMLHTTP")
          }catch (e){}
        }
      }
      else
        return false
      page_request.open('GET', url, false) //get page synchronously 
      page_request.send(null)
      writecontent(page_request)
    }
    
    function writecontent(page_request){
      if (window.location.href.indexOf("http")==-1 || page_request.status==200)
        document.write(page_request.responseText)
    }
    
  </script>

<%-- ****************************************************************************************** --%>
<%-- ****************************************************************************************** --%>
<%-- ****************************************************************************************** --%>

</head>
<body class="body_dialog01" dir="<%= LocaleHelper.getTextDirection() %>" onload="javascript:load();">
<div class="dialog01_content">
<%
  if( subscribeResult != null ) {
    out.println( ESAPI.encoder().encodeForHTML( subscribeResult ));
    out.println( "<br/>" ); //$NON-NLS-1$
  }
%>

<table border="0" width="100%" class="content_container2"
  cellpadding="0" cellspacing="0">
  <tr>
    <td class="content_body">

    <form action="<%= pageName %>" method="post">
      <%-- this allows us to remember which pivot we're working with --%>
      <input type="hidden" name="pivotId" value="<%=ESAPI.encoder().encodeForHTMLAttribute(pivotId)%>">
      <% if (_olapModel == null) { %>
        <%= Messages.getInstance().getString("UI.USER_ANALYSIS_INVALID_PAGE") %> 
      <% } else { %>
      <%-- define table, navigator and forms --%> 
      <wcf:scroller />
      <jp:table id="<%=tableId%>" query="<%=queryId%>" />
      <jp:navigator id="<%=naviId%>" query="<%=queryId%>" visible="false" /> 
      <%
      String wrappedQueryId = "#{" + queryId + "}";
      String wrappedTableId = "#{" + tableId + "}";
      String wrappedPrintId = "#{" + printId + "}";
      String chartControllerURL = "?pivotId=" + pivotId;
      %> 
      <wcf:form id="<%=mdxEditId%>" xmlUri="/WEB-INF/jpivot/table/mdxedit.xml"
        model="<%=wrappedQueryId%>" visible="false" />
      <wcf:form id="<%=sortFormId%>" xmlUri="/WEB-INF/jpivot/table/sortform.xml"
        model="<%=wrappedTableId%>" visible="false" />

      <jp:print id="<%=printId%>" />
      <wcf:form id="<%=printFormId%>"
        xmlUri="/WEB-INF/jpivot/print/printpropertiesform.xml"
        model="<%=wrappedPrintId%>" visible="false" />
      <jp:chart
      id="<%=chartId%>" query="<%=wrappedQueryId%>" visible="false" controllerURL="<%=chartControllerURL%>"/> <% 
      
  // we've reloaded the following session objects
  _table =  (TableComponent) session.getAttribute(tableId);
  _mdxEdit = (FormComponent)session.getAttribute(mdxEditId);
  _chart = (ChartComponent) session.getAttribute( chartId );

  if( chartChange ) {
    _chart.setChartType( chartType );
    _chart.setVisible( (chartLocation != null) && !chartLocation.equals( "none" ) );
    if (chartWidth > 0) {
      _chart.setChartWidth(chartWidth);
    } else {
      _chart.setChartWidth(500);    // 500 is the default that the ChartCompoent uses
    }
    if (chartHeight > 0) {
      _chart.setChartHeight(chartHeight);
    } else {
      _chart.setChartHeight(300); // 300 is the default that the ChartComponent uses
    }
    _chart.setChartTitle(chartTitle);
    _chart.setDrillThroughEnabled(chartDrillThroughEnabled);
    _chart.setFontName(chartTitleFontFamily);
    _chart.setFontStyle(chartTitleFontStyle);
    _chart.setFontSize(chartTitleFontSize);
    _chart.setHorizAxisLabel(chartHorizAxisLabel);
    _chart.setVertAxisLabel(chartVertAxisLabel);
    _chart.setAxisFontName(chartAxisLabelFontFamily);
    _chart.setAxisFontStyle(chartAxisLabelFontStyle);
    _chart.setAxisFontSize(chartAxisLabelFontSize);
    _chart.setAxisTickFontName(chartAxisTickFontFamily);
    _chart.setAxisTickFontStyle(chartAxisTickFontStyle);
    _chart.setAxisTickFontSize(chartAxisTickFontSize);
    _chart.setTickLabelRotate(chartAxisTickLabelRotation);
    _chart.setShowLegend(chartShowLegend);
    _chart.setLegendPosition(chartLegendLocation);
    _chart.setLegendFontName(chartLegendFontFamily);
    _chart.setLegendFontStyle(chartLegendFontStyle);
    _chart.setLegendFontSize(chartLegendFontSize);
    _chart.setShowSlicer(chartShowSlicer);
    _chart.setSlicerPosition(chartSlicerLocation);
    _chart.setSlicerAlignment(chartSlicerAlignment);
    _chart.setSlicerFontName(chartSlicerFontFamily);
    _chart.setSlicerFontStyle(chartSlicerFontStyle);
    _chart.setSlicerFontSize(chartSlicerFontSize);
    _chart.setBgColorR(chartBackgroundR);
    _chart.setBgColorG(chartBackgroundG);
    _chart.setBgColorB(chartBackgroundB);     
    }
  
  String wrappedChartId = "#{" + chartId + "}";
%> 
    <wcf:form id="<%=chartFormId%>"
      xmlUri="/WEB-INF/jpivot/chart/chartpropertiesform.xml"
      model="<%=wrappedChartId%>" visible="false" />
    <wcf:table
      id="<%=drillThroughTableId%>" visible="false"
      selmode="none" editable="true" />
      
<% 
    // define a toolbar
    
    if( options != null ) {
      session.removeAttribute( toolbarId ); //$NON-NLS-1$
    }
      String wrappedNaviVisible = "#{" + naviId + ".visible}";
      String wrappedMdxEditVisible = "#{" + mdxEditId + ".visible}";
      String wrappedSortFormVisible = "#{" + sortFormId + ".visible}";
      String wrappedTableLevelStyle = "#{" + tableId + ".extensions.axisStyle.levelStyle}";
      String wrappedTableHideSpans = "#{" + tableId + ".extensions.axisStyle.hideSpans}";
      String wrappedTableShowProperties = "#{" + tableId + ".rowAxisBuilder.axisConfig.propertyConfig.showProperties}";
      String wrappedTableNonEmptyButtonPressed = "#{" + tableId + ".extensions.nonEmpty.buttonPressed}";
      String wrappedTableSwapAxesButtonPressed = "#{" + tableId + ".extensions.swapAxes.buttonPressed}";
      String wrappedTableDrillMemberEnabled = "#{" + tableId + ".extensions.drillMember.enabled}";
      String wrappedTableDrillPositionEnabled = "#{" + tableId + ".extensions.drillPosition.enabled}";
      String wrappedTableDrillReplaceEnabled = "#{" + tableId + ".extensions.drillReplace.enabled}";
      String wrappedTableDrillThroughEnabled = "#{" + tableId + ".extensions.drillThrough.enabled}";
      String wrappedChartVisible = "#{" + chartId + ".visible}";
      String wrappedChartFormVisible = "#{" + chartFormId + ".visible}";
      String wrappedPrintFormVisible = "#{" + printFormId + ".visible}";
      String printExcel = "./Print?cube=" + pivotId + "&type=0";
      String printPdf = "./Print?cube=" + pivotId + "&type=1";  
      
 %> <wcf:toolbar id="<%=toolbarId%>"
      bundle="com.tonbeller.jpivot.toolbar.resources">
      <% if( options == null ) {

        %>
      <wcf:scriptbutton id="cubeNaviButton" tooltip="toolb.cube" img="cube"
        model="<%=wrappedNaviVisible%>" />
      <wcf:scriptbutton id="mdxEditButton" tooltip="toolb.mdx.edit"
        img="mdx-edit" model="<%=wrappedMdxEditVisible%>" />
      <wcf:scriptbutton id="sortConfigButton" tooltip="toolb.table.config"
        img="sort-asc" model="<%=wrappedSortFormVisible%>" />
      <wcf:separator />
      <wcf:scriptbutton id="levelStyle" tooltip="toolb.level.style"
        img="level-style" model="<%=wrappedTableLevelStyle%>" />
      <wcf:scriptbutton id="hideSpans" tooltip="toolb.hide.spans"
        img="hide-spans" model="<%=wrappedTableHideSpans%>" />
      <wcf:scriptbutton id="propertiesButton" tooltip="toolb.properties"
        img="properties" model="<%=wrappedTableShowProperties%>" />
      <wcf:scriptbutton id="nonEmpty" tooltip="toolb.non.empty"
        img="non-empty" model="<%=wrappedTableNonEmptyButtonPressed%>" />
      <wcf:scriptbutton id="swapAxes" tooltip="toolb.swap.axes"
        img="swap-axes" model="<%=wrappedTableSwapAxesButtonPressed%>" />
      <wcf:separator />
      <wcf:scriptbutton model="<%=wrappedTableDrillMemberEnabled%>"
        tooltip="toolb.navi.member" radioGroup="navi" id="drillMember"
        img="navi-member" />
      <wcf:scriptbutton model="<%=wrappedTableDrillPositionEnabled%>"
        tooltip="toolb.navi.position" radioGroup="navi" id="drillPosition"
        img="navi-position" />
      <wcf:scriptbutton model="<%=wrappedTableDrillReplaceEnabled%>"
        tooltip="toolb.navi.replace" radioGroup="navi" id="drillReplace"
        img="navi-replace" />
      <wcf:scriptbutton model="<%=wrappedTableDrillThroughEnabled%>"
        tooltip="toolb.navi.drillthru" id="drillThrough01"
        img="navi-through" />
      <wcf:separator />
      <wcf:scriptbutton id="chartButton01" tooltip="toolb.chart"
        img="chart" model="<%=wrappedChartVisible%>" />
      <wcf:scriptbutton id="chartPropertiesButton01"
        tooltip="toolb.chart.config" img="chart-config"
        model="<%=wrappedChartFormVisible%>" />
      <wcf:separator />
      <wcf:scriptbutton id="printPropertiesButton01"
        tooltip="toolb.print.config" img="print-config"
        model="<%=wrappedPrintFormVisible%>" />
      <wcf:imgbutton id="printpdf" tooltip="toolb.print" img="print"
        href="<%= printPdf %>" />
      <wcf:imgbutton id="printxls" tooltip="toolb.excel" img="excel"
        href="<%= printExcel %>" />
      <% } else {
    Iterator iterator = options.iterator();
    while( iterator.hasNext() ) {
      String optionName = (String) iterator.next();
      if( "cube-nav".equals( optionName ) ) { %>
      <wcf:scriptbutton id="cubeNaviButton" tooltip="toolb.cube" img="cube"
        model="<%=wrappedNaviVisible%>" />
      <%  } else
      if( "mdx-edit".equals( optionName ) ) { %>
      <wcf:scriptbutton id="mdxEditButton" tooltip="toolb.mdx.edit"
        img="mdx-edit" model="<%=wrappedMdxEditVisible%>" />
      <%  } else
      if( "sort-conf".equals( optionName ) ) { %>
      <wcf:scriptbutton id="sortConfigButton" tooltip="toolb.table.config"
        img="sort-asc" model="<%=wrappedSortFormVisible%>" />
      <%  } else
      if( "spacer".equals( optionName ) ) { %>
      <wcf:separator />
      <%  } else
      if( "level-style".equals( optionName ) ) { %>
      <wcf:scriptbutton id="levelStyle" tooltip="toolb.level.style"
        img="level-style" model="<%=wrappedTableLevelStyle%>" />
      <%  } else
      if( "hide-spans".equals( optionName ) ) { %>
      <wcf:scriptbutton id="hideSpans" tooltip="toolb.hide.spans"
        img="hide-spans" model="<%=wrappedTableHideSpans%>" />
      <%  } else
      if( "properties".equals( optionName ) ) { %>
      <wcf:scriptbutton id="propertiesButton" tooltip="toolb.properties"
        img="properties" model="<%=wrappedTableShowProperties%>" />
      <%  } else
      if( "non-empty".equals( optionName ) ) { %>
      <wcf:scriptbutton id="nonEmpty" tooltip="toolb.non.empty"
        img="non-empty" model="<%=wrappedTableNonEmptyButtonPressed%>" />
      <%  } else
      if( "swap-axes".equals( optionName ) ) { %>
      <wcf:scriptbutton id="swapAxes" tooltip="toolb.swap.axes"
        img="swap-axes" model="<%=wrappedTableSwapAxesButtonPressed%>" />
      <%  } else
      if( "drill-member".equals( optionName ) ) { %>
      <wcf:scriptbutton model="<%=wrappedTableDrillMemberEnabled%>"
        tooltip="toolb.navi.member" radioGroup="navi" id="drillMember"
        img="navi-member" />
      <%  } else
      if( "drill-position".equals( optionName ) ) { %>
      <wcf:scriptbutton model="<%=wrappedTableDrillPositionEnabled%>"
        tooltip="toolb.navi.position" radioGroup="navi" id="drillPosition"
        img="navi-position" />
      <%  } else
      if( "drill-replace".equals( optionName ) ) { %>
      <wcf:scriptbutton model="<%=wrappedTableDrillReplaceEnabled%>"
        tooltip="toolb.navi.replace" radioGroup="navi" id="drillReplace"
        img="navi-replace" />
      <%  } else
      if( "drill-thru".equals( optionName ) ) { %>
      <wcf:scriptbutton model="<%=wrappedTableDrillThroughEnabled%>"
        tooltip="toolb.navi.drillthru" id="drillThrough01"
        img="navi-through" />
      <%  } else
      if( "chart".equals( optionName ) ) { %>
      <wcf:scriptbutton id="chartButton01" tooltip="toolb.chart"
        img="chart" model="<%=wrappedChartVisible%>" />
      <%  } else
      if( "chart-conf".equals( optionName ) ) { %>
      <wcf:scriptbutton id="chartPropertiesButton01"
        tooltip="toolb.chart.config" img="chart-config"
        model="<%=wrappedChartFormVisible%>" />
      <%  } else
    if( "print-conf".equals( optionName ) ) { %>
      <wcf:scriptbutton id="printPropertiesButton01"
        tooltip="toolb.print.config" img="print-config"
        model="<%=wrappedPrintFormVisible%>" />
      <%  } else
    if( "print-pdf".equals( optionName ) ) { %>
      <wcf:imgbutton id="printpdf" tooltip="toolb.print" img="print"
        href="<%= printPdf %>" />
      <%  } else
    if( "excel".equals( optionName ) ) { %>
      <wcf:imgbutton id="printxls" tooltip="toolb.excel" img="excel"
        href="<%= printExcel %>" />
      <%  } 

  }
   } 
%>
    </wcf:toolbar> 
    <%-- ****************************************************************************************** --%>
    <%-- ******************                   SAVE BUTTONS               ************************** --%>
    <%-- ****************************************************************************************** --%>

    <div id="folder-options" style="display: block">
    <table cellpadding="0" cellspacing="0">
      <tr>
        <% if( authenticated ) { %>
        <td><span id="folder-down" style="display: block"> <img
          src="./jpivot/toolbar/jpivot_save.png" onclick="javascript:save();"
          alt="Save" title="Save" /> </span></td>
        <td><span id="folder-up" style="display: block"> <img
          src="./jpivot/toolbar/jpivot_saveas.png"
          onclick="javascript:saveAs();" alt="Save As" title="Save As" /> </span></td>
        <% } %>

        <%-- ****************************************************************************************** --%>
        <%-- ****************************************************************************************** --%>
        <%-- ****************************************************************************************** --%>


        <td><%-- render toolbar --%> <wcf:render ref="<%=toolbarId%>"
          xslUri="/WEB-INF/jpivot/toolbar/htoolbar.xsl" xslCache="true" /></td>
      </tr>
    </table>
    </div>


    <%-- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX --%>

    <div id="browser.modalDiv" class='browser'>
    <%-- ======================================================
     ==  SAVEAS DIALOG                                   ==
     ====================================================== --%>
    <div id="browser.saveasDialog" style="display: none; position: absolute; top: 100px; left: 200px; height: 25px;">
    <table border="0" cellspacing="0" cellpadding="0" class="popupDialog_table">
      <tr>
        <td class="popupDialog_header">
          <div id="browser.titleBar" class="popupDialogTitleBar" onmouseover="this.onmousedown=Dialog.dragIsDown;" ondragstart="return false;" onselectstart="return false;"></div>
        </td>
      </tr>
      <tr>
        <td valign="top" style="padding: 15px;">
        <table style="width: 40em; height: 100%;" border="0" cellspacing="2px" cellpadding="2px">
          <tr>
            <td id="saveDlgSaveAsPrompt" style='width: 25%'>Save As:</td>
            <td style='width: 75%'><input type="text" id="browser.saveAsNameInputText" tabindex='0' name="textfield" class="browserSaveAsText" /></td>
          </tr>
          <tr>
            <td id="saveDlgWherePrompt">Where:</td>
            <td>
            <table style='width: 100%;' border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td style="width: 100%; padding-right: 5px;" id="browser.comboContainer"></td>
                <td><img id='browser.upImg' src="adhoc/images/up.png" alt="up" /></td>
              </tr>
            </table>
            </td>
          </tr>
          <tr>
            <td id="saveDlgSelectSltnTitle" colspan='2'>Select a Solution</td>
          </tr>
          <tr>
            <td id="browser.solutionFolderListTd" height="100%" colspan='2'>
            </td>
          </tr>
        </table>
        </td>
      </tr>
      <tr>
        <td style="border-top: 1px solid #818f49; background-color: #ffffff;">
        <table border="0" cellpadding="0" cellspacing="0" align="right">
          <tr>
            <td id="browser.saveBtnContainer" width="75"></td>
            <td id="browser.cancelBtnContainer" width="85"></td>
          </tr>
        </table>
        </td>
      </tr>
    </table>
    </div>
  <%-- ======================================================
     ==  END SAVEAS DIALOG                               ==
     ====================================================== --%>
</div>
    <%-- XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX --%>


    <script type="text/javascript">
      var saveMessage = '<%= ESAPI.encoder().encodeForJavaScript(saveMessage) %>';
    </script> <% 
  
  switch (saveResult) {
    case ISolutionRepository.FILE_ADD_SUCCESSFUL: 

      if ("saveAs".equals(saveAction)){
        
          // If performing a save as.. , we need to reload the view with the newly saved 
          // action sequence.
        ActionInfo info = ActionInfo.parseActionString(request.getParameter("save-path")+ "/" + request.getParameter("save-file"));
        String fileName = info.getActionName();
        fileName = fileName.endsWith(AnalysisSaver.SUFFIX) ? fileName : fileName+AnalysisSaver.SUFFIX;

  %> <script type="text/javascript">
          var path = encodeURIComponent( "<%= ESAPI.encoder().encodeForJavaScript(info.getPath()) %>" );
          var fileName = encodeURIComponent( "<%= ESAPI.encoder().encodeForJavaScript(fileName) %>" );
          var solutionName = encodeURIComponent( "<%= ESAPI.encoder().encodeForJavaScript(info.getSolutionName()) %>" );
          var uri = "ViewAction?solution=" + solutionName + "&path=" + path + "&action=" + fileName;
          document.location.href = uri;
        </script> <%
      }
      break;
    case ISolutionRepository.FILE_EXISTS:
    break;
    case ISolutionRepository.FILE_ADD_FAILED:
      break;
    case ISolutionRepository.FILE_ADD_INVALID_PUBLISH_PASSWORD:
      break;
    case ISolutionRepository.FILE_ADD_INVALID_USER_CREDENTIALS:
      break;
    case 0:
      saveMessage="";
      session.setAttribute( "save-message-01", saveMessage); //$NON-NLS-1$
      break;
  } 
    %>


    <div id="internal_content">
      <%
      // if there was an overflow, show error message
      // note, if internal error is caused by query.getResult(),
      // no usable log messages make it to the user or the log system

      if (_olapModel != null) {
        try {
          _olapModel.getResult();
          if (_olapModel.getResult().isOverflowOccured()) {
            %><p><strong style="color: red">Resultset overflow occured</strong></p><%
          }
        } catch (Throwable t) {
            t.printStackTrace();
          %><p><strong style="color: red">Error Occurred While getting Resultset</strong></p><%
        }
      } 
      %>
    <%-- render navigator --%>
    <div id="<%=ESAPI.encoder().encodeForHTMLAttribute(naviId)%>div"><wcf:render ref="<%=naviId%>"
      xslUri="/WEB-INF/jpivot/navi/navigator.xsl" xslCache="true" /></div>

<%  if (_mdxEdit.isVisible()) { %>
    <%    if ("true".equalsIgnoreCase(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { %>
    <h3>MDX Query Viewer <font color="red">(editing disabled)</font></h3>
    <% } else { %>
    <h3>MDX Query Editor</h3>
    <% } %>
    <%-- edit mdx --%>
    <wcf:render ref="<%=mdxEditId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true" /> <% } %> 
    <%-- sort properties --%>
    <wcf:render ref="<%=sortFormId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true" /> 
    <%-- chart properties --%> 
    <wcf:render ref="<%=chartFormId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true" /> 
    <%-- print properties --%>
    <wcf:render ref="<%=printFormId%>" xslUri="/WEB-INF/wcf/wcf.xsl" xslCache="true" />

    <table border="0">
      <tr>
        <td></td>
        <td>
        <% 
            boolean chartRendered = false;
              if("top".equals(chartLocation) ) { %> <wcf:render ref="<%=chartId%>"
                xslUri="/WEB-INF/jpivot/chart/chart.xsl"
          xslCache="true" /> <% 
              chartRendered = true;
            } 
          %>
        </td>
        <td></td>
      </tr>
      <tr>
        <td valign="top">
        <% if("left".equals(chartLocation) && !chartRendered) { %> <wcf:render ref="<%=chartId%>"
            xslUri="/WEB-INF/jpivot/chart/chart.xsl" xslCache="true" /> <% 
            chartRendered = true;
          } %>
        </td>
        <td valign="top"><!-- render the table --> <% if (showGrid) { %>
        <p><wcf:render ref="<%=tableId%>"
          xslUri="/WEB-INF/jpivot/table/mdxtable.xsl" xslCache="true" /> <% } %>
        
        <p><font size="2"> Slicer: <wcf:render ref="<%=tableId%>"
          xslUri="/WEB-INF/jpivot/table/mdxslicer.xsl" xslCache="true" /> </font>
        <p><!-- drill through table --> <wcf:render
          ref="<%=drillThroughTableId%>" xslUri="/WEB-INF/wcf/wcf.xsl"
          xslCache="true" />
        </td>
        <td valign="top">
        <% if("right".equals(chartLocation) && !chartRendered) { %> <wcf:render
          ref="<%=chartId%>" xslUri="/WEB-INF/jpivot/chart/chart.xsl"
          xslCache="true" /> <% 
            chartRendered = true;
          } %>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
        <% 
        if(("bottom".equals(chartLocation) || _chart.isVisible()) && !chartRendered) { %>
        <wcf:render ref="<%=chartId%>"
          xslUri="/WEB-INF/jpivot/chart/chart.xsl" xslCache="true" /> <% 
            chartRendered = true;
        } %>
        </td>
        <td></td>
      </tr>
      <table>
        <% } %>
      </table>
      </table>
      </div>
      </form>
      </table>
      </div>
  <div id="deprecatedWarning" style="margin: auto; position: absolute; bottom: 2px; width: 100%">
  <table width="480px" align="center" style="background-color: #fffdd5; border-style: solid; border-color: #dcb114; border-width= 1px; font: normal .70em Tahoma, 'Trebuchet MS', Arial">
    <tr>
      <td>
        <img src="./jpivot/navi/warning.png"/>
      </td>
      <td>
        JPivot has been replace by Pentaho Analyzer.<br/>
        It is provided as a convenience but will no longer be enhanced or offically supported by Pentaho.
      </td>
    </tr>
  </table>
</div>
</body>

</html>
<% 
   } catch (Throwable t ) {
     %> An error occurred while rendering Pivot.jsp. Please see the log for details. <%
  // TODO log an error
  t.printStackTrace();
   } finally {
      PentahoSystem.systemExitPoint();      
   }
%>
<%!

  private IRuntimeContext getRuntimeForQuery( String actionReference, HttpServletRequest request, IPentahoSession userSession ) {

    ActionInfo actionInfo = ActionInfo.parseActionString( actionReference );
    if( actionInfo == null ) {
      return null;
    }
    return getRuntimeForQuery( actionInfo.getSolutionName(), actionInfo.getPath(), actionInfo.getActionName(), request, userSession );

  }

  private IRuntimeContext getRuntimeForQuery( String solutionName, String actionPath, String actionName, HttpServletRequest request, IPentahoSession userSession ) {
      String processId = "PivotView"; //$NON-NLS-1$
      String instanceId = request.getParameter( "instance-id" ); //$NON-NLS-1$
      boolean doMessages = "true".equalsIgnoreCase( request.getParameter("debug" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SimpleOutputHandler outputHandler = new SimpleOutputHandler( outputStream, true );
    ISolutionEngine solutionEngine = PentahoSystem.get(ISolutionEngine.class, userSession );
    solutionEngine.init( userSession );
    IRuntimeContext context = null;
    ArrayList messages = new ArrayList();
    HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider( request );
    HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( userSession );
    HashMap parameterProviders = new HashMap();
    requestParameters.setParameter( PivotViewComponent.MODE, PivotViewComponent.EXECUTE ); //$NON-NLS-1$ //$NON-NLS-2$
    parameterProviders.put( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
    parameterProviders.put( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$

    context = solutionEngine.execute( solutionName, actionPath, actionName, Messages.getInstance().getString("BaseTest.DEBUG_JUNIT_TEST"), false, true, instanceId, false, parameterProviders, outputHandler, null, urlFactory, messages ); //$NON-NLS-1$

    if( context != null && context.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
      return context;
    } else {
      return null;
    }
  }

%><%
 } finally {
    wcfcontext.invalidate();
 }

%>
