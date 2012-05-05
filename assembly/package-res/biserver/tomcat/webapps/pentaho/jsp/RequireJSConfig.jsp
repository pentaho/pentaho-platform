<%@ page language="java" 
	import="org.pentaho.platform.api.engine.IPluginManager,
			org.pentaho.platform.engine.core.system.PentahoSystem,
			javax.servlet.ServletException,
			javax.servlet.http.HttpServletRequest,
			javax.servlet.http.HttpServletResponse,
			java.io.IOException,
			java.util.List" %><%

  response.setContentType("text/javascript");

%><%!

	public String getPluginPaths(HttpServletRequest request) throws IOException {
    IPluginManager manager = PentahoSystem.get(IPluginManager.class);
    List<String> plugins = manager.getRegisteredPlugins();
    boolean first = true;	
    StringBuilder output = new StringBuilder();

    for (String plugin : plugins) {
      String amdRoot = (String) manager.getPluginSetting(plugin, "amd-root-path", null);
      String amdRootName = (String) manager.getPluginSetting(plugin, "amd-root-namespace", null);

      String[] amdRootSplit = amdRoot == null ? null : amdRoot.split(",");
      String[] amdRootNameSplit = amdRootName == null ? null : amdRootName.split(",");
      if (amdRootSplit != null && amdRootNameSplit != null) {
        if (amdRootSplit.length != amdRootNameSplit.length) {
          throw new IOException("Length mismatch: amd-root-path & amd-root-namespace properties should have the same number of declarations");
        }
        for (int i = 0; i < amdRootSplit.length; i ++) {
          if(!first){
            output.append(",\n      ");
          } else {
            first = false;
          }

          appendAmdPath(output, request.getContextPath(), amdRootSplit[i], amdRootNameSplit[i]);
        }
      }
    }
    return output.toString();
  }

	private void appendAmdPath(StringBuilder output, String contextPath, String amdRoot, String amdRootName) {
    if(amdRoot != null && amdRootName != null){
      output.append("'"+amdRootName.trim()+ "' : '"+contextPath+"/"+amdRoot.trim()+"'");
    }
  }

%><% try { %>
pen.require.config({
  waitSeconds: 30,
    paths: {
      <%= getPluginPaths(request) %>
  }
});
<% } catch (IOException e) { %>
  <%= e.getMessage() %>
<% } %>