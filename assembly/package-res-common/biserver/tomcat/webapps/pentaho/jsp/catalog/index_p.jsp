<%--
To be used with PentahoPortalNavigationPortlet. (Not used in JBoss Portal 2.6 or greater.)
--%>

<div id="topmenu">
  <div id="mtm_menu">
    <a href="/portal/portal/pentaho/default" id="page_Home">Home</a>
    <a href="/portal/portal/pentaho/Getting%20Started" id="page_Getting Started">Getting Started</a>
    <a href="/portal/portal/pentaho/Reporting" id="page_Reporting">Reporting</a>
    <a href="/portal/portal/pentaho/Business%20Rules" id="page_Business Rules">Business Rules</a>
    <a href="/portal/portal/pentaho/Printing" id="page_Printing">Printing</a>
    <a href="/portal/portal/pentaho/Bursting" id="page_Bursting">Bursting</a>
    <a href="/portal/portal/pentaho/Dashboard" id="page_Dashboard">Dashboard</a>
    <a href="/portal/portal/pentaho/Datasource" id="page_DataSource">DataSource</a>
    <a href="/portal/portal/pentaho/Secure" id="page_Secure">Secure</a>
    <span class="portals_menu">
      <a href="javascript:void" id="portal_menu">Portals</a>
    </span>

  </div><!-- end mtm_menu -->
  <script type="text/javascript" language="javascript">
  //<![CDATA[
  function initMenu() {
    if (TransMenu.isSupported()) {
      TransMenu.initialize();

      document.getElementById("page_Home").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Home").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Getting Started").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Getting Started").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Reporting").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Reporting").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Business Rules").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Business Rules").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Printing").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Printing").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Bursting").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Bursting").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Dashboard").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Dashboard").onmouseout = function() { this.className = ""; }

      document.getElementById("page_DataSource").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_DataSource").onmouseout = function() { this.className = ""; }

      document.getElementById("page_Secure").onmouseover = function() { ms.hideCurrent(); this.className = "hover"; }
      document.getElementById("page_Secure").onmouseout = function() { this.className = ""; }

      menu_portals.onactivate = function() { document.getElementById("portal_menu").className = "hover"; };
      menu_portals.ondeactivate = function() { document.getElementById("portal_menu").className = ""; };
    }
  }
  if (TransMenu.isSupported()) {
    var ms = new TransMenuSet(TransMenu.direction.down, 1, 0, TransMenu.reference.bottomLeft);
    var menu_portals = ms.addMenu(document.getElementById("portal_menu"));
    menu_portals.addItem("default", "/portal/portal/default/default", "0");
    menu_portals.addItem("pentaho", "/portal/portal/pentaho/default", "0");
    TransMenu.renderAll();
  }
  initFunctions.push("initMenu()");// ]]>
  </script>
</div><!-- end topmenu -->