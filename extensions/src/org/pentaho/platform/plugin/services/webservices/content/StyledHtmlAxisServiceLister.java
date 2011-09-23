package org.pentaho.platform.plugin.services.webservices.content;

import java.io.OutputStream;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;

public class StyledHtmlAxisServiceLister extends HtmlAxisServiceLister {

  private static final long serialVersionUID = 6592498636085258801L;

  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out ) throws Exception {
  
    // write out the style sheet and the HTML document
    
    out.write( "<html>\n<head>".getBytes() ); //$NON-NLS-1$

    out.write( "<STYLE TYPE=\"text/css\" MEDIA=\"screen\">\n<!--\n".getBytes() ); //$NON-NLS-1$
    
//    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);

    //FIXME: get style from system solution
//    byte bytes[] = resLoader.getResourceAsBytes(this.getClass(), "resources/style.css" ); //$NON-NLS-1$
//    out.write( bytes );
    
    out.write( "\n-->\n</STYLE>\n".getBytes() ); //$NON-NLS-1$
    
    out.write( "</head>\n<body>\n".getBytes() ); //$NON-NLS-1$

    // get the list of services from the core ListServices
    super.createContent(axisConfiguration, context, out);
    
    out.write( "\n</html>\n".getBytes() ); //$NON-NLS-1$
    
  }
  
}
