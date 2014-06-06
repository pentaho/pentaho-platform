<%@
        page language="java"
             import="org.pentaho.platform.web.jsp.messages.Messages"%>
<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=Messages.getInstance().getString("UI.PUC.DOCS.TITLE")%></title>
<script type="text/javascript" src="jquery.min.js"></script>
<script type="text/javascript">

$('document').ready(function(){

   // Toggle display panel w/ animation
   $('.title_bar').click(function(){
       $(this).find('span').toggleClass('open');
       $(this).next().slideToggle('slow');
       return false;
   });
   
   
   // Toggle display article description
   $('.article').click(function(){
       $(this).parent().find('.disc').slideToggle('80');
       $(this).toggleClass('open');
       return false;
   });


   // Open all panels
   $('.open-all').click(function(){
       $('.panel').slideDown('slow');
       $('.title_bar span').addClass('open');
       return false;
   });


   // Close all panels
   $('.collapse-all').click(function(){
       $('.panel').slideUp('slow');
       $('.title_bar span').removeClass('open');
       return false;
   });
   
});

</script>
<style type="text/css">
@font-face {
  font-family: 'OpenSansRegular';
  src: url('opensans-regular.eot');
  src: url('opensans-regular.eot') format('embedded-opentype'),
  url('opensans-regular.woff') format('woff'),
  url('opensans-regular.ttf') format('truetype'),
  url('opensans-regular.svg#OpenSansRegular') format('svg');
}
@font-face {
  font-family: 'OpenSansLight';
  src: url('opensans-light.eot');
  src: url('opensans-light.eot') format('embedded-opentype'),
  url('opensans-light.woff') format('woff'),
  url('opensans-light.ttf') format('truetype'),
  url('opensans-light.svg#OpenSansLight') format('svg');
}
@font-face {
  font-family: 'OpenSansItalic';
  src: url('opensans-italic.eot');
  src: url('opensans-italic.eot') format('embedded-opentype'),
  url('opensans-italic.woff') format('woff'),
  url('opensans-italic.ttf') format('truetype'),
  url('opensans-italic.svg#OpenSansItalic') format('svg');
}
@font-face {
  font-family: 'OpenSansBold';
  src: url('opensans-bold.eot');
  src: url('opensans-bold.eot') format('embedded-opentype'),
  url('opensans-bold.woff') format('woff'),
  url('opensans-bold.ttf') format('truetype'),
  url('opensans-bold.svg#OpenSansBold') format('svg');
}


/************
MIGUEL STYLES
************/

.panel, .disc {
  display:none;
}
.title_bar {
  cursor:pointer;
}
.title_bar span, .title_bar span.open {
  color:#FFF;
  background:url(disclose_arrow_right.png) no-repeat 0 3px;
  padding-left:15px;
}
.title_bar span.open {
  background:url(disclose_arrow_down.png) no-repeat 0 3px;
}
.article {
  background:url(disclose_arrow_right_blk.png) no-repeat 0 3px;
  padding-left:15px;
}
.article.open {
  background:url(disclose_arrow_down_blk.png) no-repeat 0 3px;
}
#plan {
  display: block;
}
/****************
END MIGUEL STYLES
****************/

body {
  font-family: OpenSansRegular, Helvetica, Arial, sans-serif;
  font-size: 14px;
  color:#26363d;
}
#wrapper {
  margin-right: auto;
  margin-left: auto;
  margin-top: 0px;
  width: 800px;
}
#header {
  width: 100%;
  padding: 0 0 10px 0;
  background: transparent;
  background-image:url(color-bar.png);
  background-position:24px 70px;
  background-repeat:no-repeat;
  height: 80px;
}
#title {
  background: transparent;
  width: 580px;
  color: #1973bc;
  font-family: OpenSansLight, Helvetica, Arial, sans-serif;
  font-size: 28px;
  /*text-shadow: 0 1px 1px #000000;*/
  padding: 30px 0 0 24px;
  float: left;
}
#logo {
  width: 150px;
  background: transparent;
  float: right;
  text-align: right;
  padding: 0 20px 0 0;
}
#infoMap {
  border-left: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-bottom: 1px solid #ccc;
  width: 720px;
  padding: 20px;
  background-color: #FFF;
}
#footer{
  text-align: right; 
  padding: 8px 0 4px 0;  
  font-size: 12px;
}
.pentaho-rounded-panel2-shadowed {
  text-align: left;
  width: 760px;
  padding: 20px 20px 20px 20px;
}
.pentaho-background {
  background-color: transparent;
}
.pentaho-shine {

}
.pentaho-page-background {
  background: #f6f7f8;
}
#controls {
  text-align: right;
}
.panel-title {
  text-decoration: none;
  color: #FFF;
  font-size:14px;
  /*text-shadow: 0 1px 1px #000;*/
}
.title_bar {
  padding: 4px 4px 4px 6px;
  background-color: #908e8e;
  margin: 0 0 4px 0;
}
:-moz-any-link:focus {
 outline: none;
}
ul {
  padding: 0;
  margin: 0 0px 4px 18px; 
}
ul li {
  list-style: none;
  font-size: 14px;
  padding-bottom:5px;
  line-height: 1.45em;
  font-weight:400;
}
a {
  color: #005ca7;
  font-size: 14px;
}
#footer a {
  font-size: 12px;
}
a:hover {
  text-decoration: none
}
a:active {
}
a:visited {
  color:#1973bc;
}
.disc {
  padding: 2px 0 4px 16px;
  line-height: 1.25em;
}
#banner {
  width: 720px;
  padding: 20px;
  /*height: 90px;*/
background: rgb(255,255,255); /* Old browsers */
background: -moz-linear-gradient(top,  rgba(255,255,255,1) 0%, rgba(248,248,248,1) 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(255,255,255,1)), color-stop(100%,rgba(248,248,248,1))); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top,  rgba(255,255,255,1) 0%,rgba(248,248,248,1) 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top,  rgba(255,255,255,1) 0%,rgba(248,248,248,1) 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top,  rgba(255,255,255,1) 0%,rgba(248,248,248,1) 100%); /* IE10+ */
background: linear-gradient(to bottom,  rgba(255,255,255,1) 0%,rgba(248,248,248,1) 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ffffff', endColorstr='#f8f8f8',GradientType=0 ); /* IE6-9 */

  border: 1px solid #ccc;
  -webkit-border-top-left-radius: 0px;
  -webkit-border-top-right-radius: 0px;
  -moz-border-radius-topleft: 0px;
  -moz-border-radius-topright: 0px;
  border-top-left-radius: 0px;
  border-top-right-radius: 0px;
}
.panel {
  width: 100%;
  line-height:1.5em;
  padding: 2px;
}
.style2 {
  /*font-size: .8em*/
  line-height:1.45;
  font-size: 14px;
  font-weight: 400;
  color: #444;
}
.bannerimage{
  padding: 0px 0px 0px 20px;
  width:122px;
}
.bannercontent{
  padding: 8px 0 0 0;
}
</style>
</head>
<body class="pentaho-page-background">
<div id="wrapper">
<!--Header-->
  <div id="header">
    <div id="title"><%=Messages.getInstance().getString("UI.PUC.DOCS.HEADER.TITLE")%></div>
    <div id="logo"><a href="http://www.pentaho.com" target="_blank"><img src="logo.png" alt="Pentaho Logo" border="0"/></a></div>
  </div>

  <!--Featured Content-->
  <div class="pentaho-rounded-panel2-shadowed pentaho-background pentaho-shine">
    <div id="banner">
      <table width="700" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td rowspan="2" class="bannerimage"><img src="pdf_icon.png" width="80" height="80" /></td>
          <td><%=Messages.getInstance().getString("UI.PUC.DOCS.HEADER")%></td>
        </tr>
        <tr>
          <td valign="top" class="bannercontent"><%=Messages.getInstance().getString("UI.PUC.DOCS.HEADER.VISIT_INFO", "<a href=\"http://help.pentaho.com\" target=\"_blank\">Pentaho InfoCenter</a>")%><br /></td>
        </tr>
      </table>
    </div>

  <!--Catagories and Content Matching InfoCenter Left Nav-->
    <div id="infoMap">
      <div id="controls"> <b><a class="collapse-all" href=""><img src="collapse_all.png" border="0" alt="<%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUTTON.COLLAPSE")%>" /></a> <a class="open-all" href=""><img src="expand_all.png" border="0" alt="<%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUTTON.EXPAND")%>" /></a></b>
      </div>
         
      <!-- Plan -->
      <p class="title_bar"><span class="open"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN")%></span></p>
      <div id="plan" class="panel" style="display:">      
        <ul>
          <li> <a href="" class="article"></a> <a href="components.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN.COMPONENTS")%></a><br />
            <div id="Components" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN.COMPONENTS.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="workflows.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN.WORKFLOWS")%></a><br />
            <div id="PentahoUserConsoleGuideU" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN.WORKFLOWS.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="supported_components.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN.SUPPORTED_COMPONENTS")%></a><br />
            <div id="SupportMatrix" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.MAKE_A_PLAN.SUPPORTED_COMPONENTS.DESC")%></div>
       </li>
        </ul>
      </div>   

      <!-- Install Took out the install documents that do not apply to CE -->
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL")%></span></p>
      <div id="install" class="panel">
        <ul>
            <li> <a href="" class="article"></a> <a href="install_ziptar.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.ZIPTAR")%></a><br />
            <div id="BAArchiveInstallation" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.ZIPTAR.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="install_client.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.CLIENT")%></a><br />
            <div id="BATools" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.CLIENT.DESC")%></div>
          </li>
            <li> <a href="" class="article"></a> <a href="install_pdi.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.PDI")%></a><br />
            <div id="DIArchiveInstallation" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.PDI.DESC")%></div>
          </li>          
          <li> <a href="" class="article"></a> <a href="install_client_pdi.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.CLIENT_PDI")%></a><br />
            <div id="DITools" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.INSTALL.CLIENT_PDI.DESC")%></div>
          </li>
        </ul>
      </div>

      <!-- Configure -->     
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.CONFIGURE")%></span></p>
      <div id="configure" class="panel">
        <ul>
          <li> <a href="" class="article"></a> <a href="config_ba_server.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.CONFIGURE.BA_SERVER")%></a><br />
            <div id="ConfigBA" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.CONFIGURE.BA_SERVER.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="config_pdi_server.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.CONFIGURE.PDI_SERVER")%></a><br />
            <div id="ConfigDI" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.CONFIGURE.PDI_SERVER.DESC")%></div>
          </li>
        </ul>
      </div>   

      <!-- Evaluate -->
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS")%></span></p>
      <div id="evaluation" class="panel">
        <ul>
          <li> <a href="" class="article"></a> <a href="getting_started_with_pentaho.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.PENTAHO")%></a><br />
            <div id="GettingStartedwithBISuite" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.PENTAHO.DESC")%></div>
          </li>
          <li><a href="" class="article"></a> <a href="getting_started_with_prd.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.PRD")%></a><br />
            <div id="GettingStartedwithPRD" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.PRD.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="getting_started_with_pdi.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.PDI")%></a><br />
            <div id="GettingStartedwithPDI" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.PDI.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="getting_started_with_instaview.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.INSTAVIEW")%></a><br />
            <div id="GettingStartedwithPDIInstaview" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUTORIALS.INSTAVIEW.DESC")%></div>
          </li>
        </ul>
      </div> 

      <!-- Build -->
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS")%></span></p>
      <div id="user" class="panel">
        <ul>
          <li><a href="" class="article"></a> <a href="getting_started_with_data_models.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.DATA_MODELS")%></a><br />
            <div id="PentahoDataSourceWizard" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.DATA_MODELS.DESC")%></div>
          </li>
          <li><a href="" class="article"></a> <a href="puc_user_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.PUC")%></a><br />
            <div id="PentahoUserConsole" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.PUC.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="report_designer_user_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.REPORTS")%></a><br />
            <div id="ReportDesignerUser" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.REPORTS.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="pdi_user_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.PDI")%></a><br />
            <div id="PDIUser" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.PDI.DESC")%>
            </div>
          </li>
          <li> <a href="" class="article"></a> <a href="bigdata_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.BIG_DATA")%></a><br />
            <div id="Bigdata" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.BUILD_SOLUTIONS.BIG_DATA_DESC")%></div>
          </li>
        </ul>
      </div>

      <!-- Model -->
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS")%></span></p>
      <div id="modeling" class="panel">
        <ul>
          <li> <a href="" class="article"></a> <a href="pme_user_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS.RELATIONAL")%></a><br />
            <div id="MetadataEditor" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS.RELATIONAL.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="analysis_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS.ANALYSIS")%></a><br />
            <div id="Analysis" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS.ANALYSIS.DESC")%></div>
          </li>
          <li> <a href="" class="article"></a> <a href="aggregation_designer_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS.AGGREGATION_DESIGNER")%></a><br />
            <div id="AggregationDesignerUser" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DATA_MODELS.AGGREGATION_DESIGNER.DESC")%></div>
          </li>     
        </ul>
      </div>
 
      <!-- Administrate -->
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE")%></span></p>
      <div id="admin" class="panel">
        <ul>
          <li><a href="" class="article"></a> <a href="admin_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.ADMIN")%></a><br />
            <div id="BISuiteAdminGuide" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.ADMIN.DESC")%></div>
          </li>
          <li><a href="" class="article"></a> <a href="security_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.SECURITY")%></a><br />
            <div id="BISuiteSecurityGuide" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.SECURITY.DESC")%><span class="pdf_link"></span></div>
          </li>
          <li><a href="" class="article"></a> <a href="performance_tuning_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.PERFORMANCE")%></a><br />
            <div id="BISuitePerformanceTuningGuide" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.PERFORMANCE.DESC")%></div>
          </li>          
          <li><a href="" class="article"></a> <a href="troubleshooting_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.TROUBLESHOOT")%></a><br />
            <div id="BISuiteTroubleshootingGuide" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.TROUBLESHOOT.DESC")%></div>
          </li>
          <li><a href="" class="article"></a> <a href="pdi_admin_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.PDI_ADMIN")%></a><br />
            <div id="PDIAdministratorsGuide" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.TUNE.PDI_ADMIN.DESC")%></div>
          </li>
        </ul>
      </div>

      <!-- Develop-->
      <p class="title_bar"><span><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP")%></span></p>
      <div id="dev" class="panel">
        <ul>
          <li><a href="" class="article"></a> <a href="dashboard_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.DASHBOARDS")%></a><br />
            <div id="CreatingPentahoDashboards" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.DASHBOARDS.DESC")%></div>
          </li>
          <li><a href="" class="article"></a> <a href="customizing_pentaho_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.CUSTOMIZING")%></a><br />
            <div id="CustomizingtheBISuite" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.CUSTOMIZING.DESC")%></div>
          </li>        
          <li><a href="" class="article"></a> <a href="integrating_biserver.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.INTEGRATING")%></a><br />
            <div id="IntegratingtheBIServer" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.INTEGRATING.DESC")%></div>
          </li>          
          <li><a href="" class="article"></a> <a href="reporting_embedders_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.REPORTING_EMBED")%></a><br />
            <div id="IntegratingtheBIServer" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.REPORTING_EMBED.DESC")%></div>
          </li>          
          <li><a href="" class="article"></a> <a href="pdi_embed_extend_guide.pdf" target="_blank"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.PDI_EMBED_EXTEND")%></a><br />
            <div id="ExtendingAndEmbeddingPDI" class="disc"><%=Messages.getInstance().getString("UI.PUC.DOCS.CONTENTS.DEVELOP.PDI_EMBED_EXTEND.DESC")%></div>
          </li>
        </ul>
      </div>

      <!-- Footer --> 
      <div id="footer">
          <%=Messages.getInstance().getString("UI.PUC.DOCS.FOOTER", "<a href=\"http://get.adobe.com/reader/\" target=\"_blank\">"+Messages.getInstance().getString("UI.PUC.DOCS.FOOTER.ADOBE_SITE")+"</a>")%>
      </div>
    </div>
  </div>
</div>
</body>
</html>
