<%@ page language="java"
	import="org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper,
			org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog,
			org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube,
			java.util.List,
			org.pentaho.platform.engine.core.system.PentahoSystem,
	 org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	 org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	 org.pentaho.platform.api.engine.IPentahoSession,
	 org.pentaho.platform.web.http.WebTemplateHelper,
	 org.pentaho.platform.api.engine.IUITemplater,
	 org.springframework.web.context.support.WebApplicationContextUtils,
	 org.springframework.web.context.WebApplicationContext,
	 org.pentaho.platform.web.servlet.AnalysisViewService,
   org.pentaho.platform.engine.core.system.PentahoSessionHolder
	 "%>
<%
       /*
       * Copyright 2008 - 2010 Pentaho Corporation.  All rights reserved. 
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
       * @author James Dixon
       * 
       */

       /*
       * This jsp is deprecated and should no longer be used. It has been removed from the web.xml.
       */
       
       IPentahoSession userSession = PentahoSessionHolder.getSession();

      String intro = "";
      String footer = "";
      IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession);
      if (templater != null) {
        String sections[] = templater.breakTemplate("template.html", "", userSession); //$NON-NLS-1$ //$NON-NLS-2$
        if (sections != null && sections.length > 0) {
          intro = sections[0];
        }
        if (sections != null && sections.length > 1) {
          footer = sections[1];
        }
      } else {
        intro = Messages.getInstance().getString("UI.ERROR_0002_BAD_TEMPLATE_OBJECT");
      }

      List<MondrianCatalog> catalogs = MondrianCatalogHelper.getInstance().listCatalogs(
          PentahoSessionHolder.getSession(), true);
%>

<script type="text/javascript">
	
	var Schema = function(){
		this.name = '';
		this.cubes = [];
	}
	
	var temp;
	var schemas = [];
	<%
	
	
	if(catalogs != null){
		for(MondrianCatalog cat : catalogs){
		  
		  out.println("temp = new Schema();");
		  out.println(String.format("temp.name = '%s';", cat.getName()));
		  
		  for(MondrianCube cube : cat.getSchema().getCubes()){
			  out.println(String.format("temp.cubes.push('%s');", cube.getName()));
		  }
		  out.println(String.format("schemas['%s'] = temp;", cat.getName()));
		}
	}
	%>
</script>

<link href="adhoc/styles/repositoryBrowserStyles.css" rel="stylesheet"
	type="text/css" />

<script src="js/ajaxslt0.7/xmltoken.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/util.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/dom.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/xpath.js" type="text/javascript"></script>
<script src="js/ajaxslt0.7/xslt.js" type="text/javascript"></script>

<script src="js/xslt_script.js" type="text/javascript"></script>
<script src="js/pentaho-ajax.js" type="text/javascript"></script>
<script src="js/utils.js" type="text/javascript"></script>
<script type="text/javascript">
		djConfig = { isDebug: false};
	</script>

<script src="js/dojo.js" type="text/javascript"></script>

<script type="text/javascript">
		dojo.registerModulePath("adhoc", "../adhoc/js");
	</script>

<script src="adhoc/js/common/ui/messages/Messages.js"
	type="text/javascript"></script>

<script type="text/javascript">
		Messages.addBundle("adhoc.ui.messages", "message_strings");
	</script>

<script src="adhoc/js/common/ui/MessageCtrl.js" type="text/javascript"></script>
<script src="adhoc/js/common/server/WebServiceProxy.js"
	type="text/javascript"></script>
<script src="adhoc/js/common/util/StringUtils.js" type="text/javascript"></script>
<script src="adhoc/js/common/util/Status.js" type="text/javascript"></script>
<script src="adhoc/js/common/util/XmlUtil.js" type="text/javascript"></script>

<script src="adhoc/js/model/SolutionRepository.js"
	type="text/javascript"></script>

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
<script src="js/pivot/PivotRepositoryBrowserController.js"
	type="text/javascript"></script>

<%=intro%>

<form name="frm" method="GET" action="AnalysisViewService?"
	onSubmit="return false"><input type="hidden" name="component"
	value="createNewView" />

<table border='0' cellpadding='0' cellspacing='0' width="100%"
	height="500">
	<tr>
		<td valign="top" align="center" style="padding-top:15px;">
		<h1>New Pentaho Analysis View</h1>

		<span style="text-align:center; color:red;"> <%
       Object errorMessage = request.getAttribute("errorMessage");
       if (errorMessage != null) {
         out.print("Error: ");
         out.print((String) errorMessage);
       }
 %> </span> <%
 if (catalogs == null || catalogs.size() == 0) {
 %> <br />
		<span style="text-align:center"> <%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.NO_CATALOGS_MESSAGE")%>
		</span> <%
 } else {
 %>
		<table border="0" cellpadding="0" cellspacing="0" width="350">
			<tr>
				<td width="10"><img
					src="/pentaho-style/images/grey_top_left.png" width="10" height="9" /></td>
				<td bgcolor="#dedede"><img src="/pentaho-style/images/Clr.png"
					width="341" height="2" /></td>
				<td width="10"><img
					src="/pentaho-style/images/grey_top_right.png" width="10"
					height="9" /></td>
			</tr>
			<tr>
				<td bgcolor="#dedede">&nbsp;</td>
				<td bgcolor="#dedede">


				<table width="100%" border='0' cellpadding='1' cellspacing='1'
					style="background-color:#dddddd">
					<tr>
						<td><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.TITLE")%></td>
					</tr>
					<tr>
						<td><input type="text" name="name" style="width:100%"
							value="<%= (request.getParameter("name") != null) ? request.getParameter("name") : "" %>" />
						</td>
					</tr>
					<tr>
						<td><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.DESCRIPTION")%>
						</td>
					</tr>
					<tr>
						<td><input type="text" name="descr" style="width:100%"
							value="<%= (request.getParameter("descr") != null) ? request.getParameter("descr") : "" %>" />
						</td>
					</tr>

					<tr>
						<td><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.FILENAME")%></td>
					</tr>
					<tr>
						<td><input type="text" name="actionName" style="width:100%"
							value="<%= (request.getParameter("actionName") != null) ? request.getParameter("actionName") : "" %>" />
						</td>
					</tr>
					<tr>
						<td>

						<table border="0" cellspacing="0" cellpadding="0" class=""
							width="100%">
							<tr style="display:none">
								<td class="popupDialog_header">
								<div id="browser.titleBar" class="popupDialogTitleBar"
									onmouseover="this.onmousedown=Dialog.dragIsDown;"
									ondragstart="return false;" onselectstart="return false;"></div>
								</td>
							</tr>
							<tr>
								<td valign="top" style="padding: 4px;">
								<table style="width:100%;height:100%;" border="0"
									cellspacing="2px" cellpadding="2px">
									<tr style="display: none">
										<td style='width:25%'>Save As:</td>
										<td style='width:75%'><input type="text"
											id="browser.saveAsNameInputText" tabindex='0'
											name="textfield" class="browserSaveAsText" /></td>
									</tr>
									<tr style="display:none">

										<td id="saveDlgWherePrompt" colspan="2"></td>
									</tr>
									<tr>
										<td style="white-space: nowrap"><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.WHERE")%></td>
										<td width="100%">
										<table style='width:100%;' border="0" cellspacing="0"
											cellpadding="0">
											<tr>
												<td style="width:100%;padding-right:5px;"
													id="browser.comboContainer"></td>
												<td><img id='browser.upImg' src="adhoc/images/up.png"
													alt="up" /></td>
											</tr>
										</table>
										</td>
									</tr>
									<tr style="display:none">
										<td id="saveDlgSelectSltnTitle" colspan='2'><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.SELECT_SOLUTION")%></td>
									</tr>
									<tr>
										<td id="browser.solutionFolderListTd" height="100%"
											colspan='2'></td>
									</tr>
								</table>
								</td>
							</tr>
							<tr style="display:none">
								<td
									style="border-top: 1px solid #818f49; background-color: #ffffff;">
								<table border="0" cellpadding="0" cellspacing="0" align="right">
									<tr>
										<td id="browser.saveBtnContainer" width="75"></td>
										<td id="browser.cancelBtnContainer" width="85"></td>
									</tr>
								</table>
								</td>
							</tr>
						</table>


						</td>
					</tr>
				</table>


				<fieldset><legend>Data Source</legend>
				<table border='0' cellpadding='1' cellspacing='1'
					style="background-color:#dddddd">
					<tr>
						<td><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.SCHEMA")%></td>
					</tr>
					<tr>
						<td><select id="schemas" name="schema" style="width:350"
							onChange="showCubes()">
						</select> <script>
													var schemaSelect = document.getElementById("schemas");
													for each(schema in schemas){
														var opt = new Option(schema.name,schema.name);
														if(schema.name == "<%= request.getParameter("schema")%>"){
															opt.selected = true;
														}
														schemaSelect.options[schemaSelect.options.length] = opt;
													}
												</script></td>
					</tr>
					<tr>
						<td><%=Messages.getInstance().getString("UI.ANALYSIS_VIEW.CUBE")%></td>
					</tr>
					<tr>
						<td><select style="width:350" id="cubes" name="cube" />
							<script>
													var cubeSelect = document.getElementById("cubes");
													function showCubes(){
														cubeSelect.options.length = 0;
														for each(cube in schemas[schemaSelect.value].cubes){
															var opt = new Option(cube,cube);
															if(cube== "<%= request.getParameter("cube")%>"){
																opt.selected = true;
															}
															cubeSelect.options[cubeSelect.options.length] = opt;
														}
													}
													showCubes();
												</script></td>
					</tr>
				</table>
				</fieldset>

				</td>
				<td bgcolor="#dedede">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="2" align="right" bgcolor="#dedede" style="width:150px;"><a
					href="#" onClick="postFrm()" class="button"><span>Create&nbsp;View</span></a></td>

				<td bgcolor="#dedede">&nbsp;</td>
			</tr>
			<tr>
				<td><img src="/pentaho-style/images/grey_bottom_left.png"
					width="10" height="10" /></td>
				<td bgcolor="#dedede"><img src="/pentaho-style/images/Clr.png"
					width="341" height="2" /></td>
				<td><img src="/pentaho-style/images/grey_bottom_right.png"
					width="10" height="10" /></td>
			</tr>
		</table>
		<%
		}
		%>
		</td>
	</tr>
</table>

<input type="hidden" name="solution" /> <input type="hidden"
	name="actionPath" /></form>

<script type="text/javascript">
		PivotRepositoryBrowserController.RE_FILE_FILTER = /########/;
	
		controller = new PivotRepositoryBrowserController();
		controller.open();
		
		function postFrm(){
			document.frm.solution.value = controller.repositoryBrowser.getSolutionName();
			document.frm.actionPath.value = controller.repositoryBrowser.getPath();
			document.frm.actionPath.value = controller.repositoryBrowser.getPath();
			
			
			//document.frm.actionName.value = controller.repositoryBrowser.getSelectedFileInfo().name;
			
			
			document.frm.submit();
		}
	</script>

<%=footer%>
