/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Nov 5, 2005
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.mondrian;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class PivotViewComponent extends ComponentBase {

  public static final String MODE = "mode"; //$NON-NLS-1$

  public static final String MODEL = "model"; //$NON-NLS-1$

  public static final String OPTIONS = "options"; //$NON-NLS-1$

  public static final String CONNECTION = "connection"; //$NON-NLS-1$

  public static final String TITLE = "title"; //$NON-NLS-1$

  public static final String URL = "url"; //$NON-NLS-1$

  public static final String VIEWER = "viewer"; //$NON-NLS-1$

  public static final String EXECUTE = "execute"; //$NON-NLS-1$

  public static final String SHOWGRID = "showgrid"; //$NON-NLS-1$

  public static final String CHARTTYPE = "charttype"; //$NON-NLS-1$

  public static final String CHARTLOCATION = "chartlocation"; //$NON-NLS-1$

  public static final String CHARTWIDTH = "chartwidth"; //$NON-NLS-1$

  public static final String CHARTHEIGHT = "chartheight"; //$NON-NLS-1$

  public static final String CHARTDRILLTHROUGHENABLED = "chartdrillthroughenabled"; //$NON-NLS-1$

  public static final String CHARTTITLE = "charttitle"; //$NON-NLS-1$

  public static final String CHARTTITLEFONTFAMILY = "charttitlefontfamily"; //$NON-NLS-1$

  public static final String CHARTTITLEFONTSTYLE = "charttitlefontstyle"; //$NON-NLS-1$

  public static final String CHARTTITLEFONTSIZE = "charttitlefontsize"; //$NON-NLS-1$

  public static final String CHARTHORIZAXISLABEL = "charthorizaxislabel"; //$NON-NLS-1$

  public static final String CHARTVERTAXISLABEL = "chartvertaxislabel"; //$NON-NLS-1$

  public static final String CHARTAXISLABELFONTFAMILY = "chartaxislabelfontfamily"; //$NON-NLS-1$

  public static final String CHARTAXISLABELFONTSTYLE = "chartaxislabelfontstyle"; //$NON-NLS-1$

  public static final String CHARTAXISLABELFONTSIZE = "chartaxislabelfontsize"; //$NON-NLS-1$

  public static final String CHARTAXISTICKFONTFAMILY = "chartaxistickfontfamily"; //$NON-NLS-1$

  public static final String CHARTAXISTICKFONTSTYLE = "chartaxistickfontstyle"; //$NON-NLS-1$

  public static final String CHARTAXISTICKFONTSIZE = "chartaxistickfontsize"; //$NON-NLS-1$

  public static final String CHARTAXISTICKLABELROTATION = "chartaxisticklabelrotation"; //$NON-NLS-1$

  public static final String CHARTSHOWLEGEND = "chartshowlegend"; //$NON-NLS-1$

  public static final String CHARTLEGENDLOCATION = "chartlegendlocation"; //$NON-NLS-1$

  public static final String CHARTLEGENDFONTFAMILY = "chartlegendfontfamily"; //$NON-NLS-1$

  public static final String CHARTLEGENDFONTSTYLE = "chartlegendfontstyle"; //$NON-NLS-1$

  public static final String CHARTLEGENDFONTSIZE = "chartlegendfontsize"; //$NON-NLS-1$

  public static final String CHARTSHOWSLICER = "chartshowslicer"; //$NON-NLS-1$

  public static final String CHARTSLICERLOCATION = "chartslicerlocation"; //$NON-NLS-1$

  public static final String CHARTSLICERALIGNMENT = "chartsliceralignment"; //$NON-NLS-1$

  public static final String CHARTSLICERFONTFAMILY = "chartslicerfontfamily"; //$NON-NLS-1$

  public static final String CHARTSLICERFONTSTYLE = "chartslicerfontstyle"; //$NON-NLS-1$

  public static final String CHARTSLICERFONTSIZE = "chartslicerfontsize"; //$NON-NLS-1$

  public static final String CHARTBACKGROUNDR = "chartbackgroundr"; //$NON-NLS-1$

  public static final String CHARTBACKGROUNDG = "chartbackgroundg"; //$NON-NLS-1$

  public static final String CHARTBACKGROUNDB = "chartbackgroundb"; //$NON-NLS-1$

  public static final String ROLE = "role"; //$NON-NLS-1$

  public static final String CUBE = "cube"; //$NON-NLS-1$

  private static final long serialVersionUID = -327755990995067478L;

  private static final Collection ignoreInputs = Arrays.asList(new String[] { PivotViewComponent.MODE,
      StandardSettings.SQL_QUERY, StandardSettings.QUERY_NAME, PivotViewComponent.VIEWER });

  @Override
  public Log getLogger() {
    return LogFactory.getLog(PivotViewComponent.class);
  }

  @Override
  protected boolean validateAction() {

    if (!isDefinedOutput(PivotViewComponent.OPTIONS)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0001_OPTIONS_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.MODEL)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0002_MODEL_NOT_DEFIEND")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.CONNECTION)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0003_CONNECTION_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(StandardSettings.MDX_QUERY)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0004_MDX_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.TITLE)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0007_TITLE_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedInput(PivotViewComponent.MODE)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0005_MODE_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedOutput(PivotViewComponent.URL)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0008_URL_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (!isDefinedInput(StandardSettings.SQL_QUERY) && !isDefinedInput(StandardSettings.QUERY_NAME)) {
      error(Messages.getInstance().getErrorString("PivotView.ERROR_0009_QUERY_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }

    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
  }

  @SuppressWarnings("deprecation")
  @Override
  protected boolean executeAction() throws Throwable {

    Set inputNames = getInputNames();
    Set outputNames = getOutputNames();

    String mode = getInputStringValue(PivotViewComponent.MODE);
    if (!mode.equals(PivotViewComponent.EXECUTE)) {
      // assume this is a redirect
      if (!isDefinedOutput(PivotViewComponent.URL)) {
        // we need the viewer output
        error(Messages.getInstance().getString("PivotView.ERROR_0006_VIEWER_NOT_DEFINED")); //$NON-NLS-1$
        return false;
      }
      String viewer = getInputStringValue(PivotViewComponent.VIEWER);
      if (viewer.indexOf('?') == -1) {
        viewer += "?solution=" + getSolutionName() + "&path=" + getSolutionPath() + "&action=" + getActionName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else {
        viewer += "solution=" + getSolutionName() + "&path=" + getSolutionPath() + "&action=" + getActionName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }

      for (Iterator it = inputNames.iterator(); it.hasNext();) {
        String name = (String) it.next();
        if (!PivotViewComponent.ignoreInputs.contains(name)) {
          viewer += "&" + name + "=" + URLEncoder.encode(getInputStringValue(name), LocaleHelper.getSystemEncoding()); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }

      setOutputValue(PivotViewComponent.URL, PentahoRequestContextHolder.getRequestContext().getContextPath() + viewer);
      return true;
    }

    String roleName = null;
    if (isDefinedInput(PivotViewComponent.ROLE)) {
      roleName = getInputStringValue(PivotViewComponent.ROLE);
      if (isDefinedOutput(PivotViewComponent.ROLE)) {
        setOutputValue(PivotViewComponent.ROLE, roleName);
      }
    }

    // process the model
    String model = getInputStringValue(StandardSettings.DATA_MODEL);
    if (!model.startsWith("solution:") && !model.startsWith("http:")) { //$NON-NLS-1$ //$NON-NLS-2$
      model = "solution:" + model; //$NON-NLS-1$
    }

    setOutputValue(StandardSettings.DATA_MODEL, model);

    if (isDefinedOutput(PivotViewComponent.CHARTTYPE)) {
      if (isDefinedInput(PivotViewComponent.CHARTTYPE)) {
        setOutputValue(PivotViewComponent.CHARTTYPE, getInputStringValue(PivotViewComponent.CHARTTYPE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.SHOWGRID)) {
      if (isDefinedInput(PivotViewComponent.SHOWGRID)) {
        setOutputValue(PivotViewComponent.SHOWGRID, getInputStringValue(PivotViewComponent.SHOWGRID));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTLOCATION)) {
      if (isDefinedInput(PivotViewComponent.CHARTLOCATION)) {
        setOutputValue(PivotViewComponent.CHARTLOCATION, getInputStringValue(PivotViewComponent.CHARTLOCATION));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTWIDTH)) {
      if (isDefinedInput(PivotViewComponent.CHARTWIDTH)) {
        setOutputValue(PivotViewComponent.CHARTWIDTH, getInputStringValue(PivotViewComponent.CHARTWIDTH));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTHEIGHT)) {
      if (isDefinedInput(PivotViewComponent.CHARTHEIGHT)) {
        setOutputValue(PivotViewComponent.CHARTHEIGHT, getInputStringValue(PivotViewComponent.CHARTHEIGHT));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTDRILLTHROUGHENABLED)) {
      if (isDefinedInput(PivotViewComponent.CHARTDRILLTHROUGHENABLED)) {
        setOutputValue(PivotViewComponent.CHARTDRILLTHROUGHENABLED,
            getInputStringValue(PivotViewComponent.CHARTDRILLTHROUGHENABLED));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTTITLE)) {
      if (isDefinedInput(PivotViewComponent.CHARTTITLE)) {
        setOutputValue(PivotViewComponent.CHARTTITLE, getInputStringValue(PivotViewComponent.CHARTTITLE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTTITLEFONTFAMILY)) {
      if (isDefinedInput(PivotViewComponent.CHARTTITLEFONTFAMILY)) {
        setOutputValue(PivotViewComponent.CHARTTITLEFONTFAMILY,
            getInputStringValue(PivotViewComponent.CHARTTITLEFONTFAMILY));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTTITLEFONTSTYLE)) {
      if (isDefinedInput(PivotViewComponent.CHARTTITLEFONTSTYLE)) {
        setOutputValue(PivotViewComponent.CHARTTITLEFONTSTYLE,
            getInputStringValue(PivotViewComponent.CHARTTITLEFONTSTYLE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTTITLEFONTSIZE)) {
      if (isDefinedInput(PivotViewComponent.CHARTTITLEFONTSIZE)) {
        setOutputValue(PivotViewComponent.CHARTTITLEFONTSIZE,
            getInputStringValue(PivotViewComponent.CHARTTITLEFONTSIZE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTHORIZAXISLABEL)) {
      if (isDefinedInput(PivotViewComponent.CHARTHORIZAXISLABEL)) {
        setOutputValue(PivotViewComponent.CHARTHORIZAXISLABEL,
            getInputStringValue(PivotViewComponent.CHARTHORIZAXISLABEL));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTVERTAXISLABEL)) {
      if (isDefinedInput(PivotViewComponent.CHARTVERTAXISLABEL)) {
        setOutputValue(PivotViewComponent.CHARTVERTAXISLABEL,
            getInputStringValue(PivotViewComponent.CHARTVERTAXISLABEL));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISLABELFONTFAMILY)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISLABELFONTFAMILY)) {
        setOutputValue(PivotViewComponent.CHARTAXISLABELFONTFAMILY,
            getInputStringValue(PivotViewComponent.CHARTAXISLABELFONTFAMILY));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISLABELFONTSTYLE)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISLABELFONTSTYLE)) {
        setOutputValue(PivotViewComponent.CHARTAXISLABELFONTSTYLE,
            getInputStringValue(PivotViewComponent.CHARTAXISLABELFONTSTYLE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISLABELFONTSIZE)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISLABELFONTSIZE)) {
        setOutputValue(PivotViewComponent.CHARTAXISLABELFONTSIZE,
            getInputStringValue(PivotViewComponent.CHARTAXISLABELFONTSIZE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISTICKFONTFAMILY)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISTICKFONTFAMILY)) {
        setOutputValue(PivotViewComponent.CHARTAXISTICKFONTFAMILY,
            getInputStringValue(PivotViewComponent.CHARTAXISTICKFONTFAMILY));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISTICKFONTSTYLE)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISTICKFONTSTYLE)) {
        setOutputValue(PivotViewComponent.CHARTAXISTICKFONTSTYLE,
            getInputStringValue(PivotViewComponent.CHARTAXISTICKFONTSTYLE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISTICKFONTSIZE)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISTICKFONTSIZE)) {
        setOutputValue(PivotViewComponent.CHARTAXISTICKFONTSIZE,
            getInputStringValue(PivotViewComponent.CHARTAXISTICKFONTSIZE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTAXISTICKLABELROTATION)) {
      if (isDefinedInput(PivotViewComponent.CHARTAXISTICKLABELROTATION)) {
        setOutputValue(PivotViewComponent.CHARTAXISTICKLABELROTATION,
            getInputStringValue(PivotViewComponent.CHARTAXISTICKLABELROTATION));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSHOWLEGEND)) {
      if (isDefinedInput(PivotViewComponent.CHARTSHOWLEGEND)) {
        setOutputValue(PivotViewComponent.CHARTSHOWLEGEND, getInputStringValue(PivotViewComponent.CHARTSHOWLEGEND));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTLEGENDLOCATION)) {
      if (isDefinedInput(PivotViewComponent.CHARTLEGENDLOCATION)) {
        setOutputValue(PivotViewComponent.CHARTLEGENDLOCATION,
            getInputStringValue(PivotViewComponent.CHARTLEGENDLOCATION));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTLEGENDFONTFAMILY)) {
      if (isDefinedInput(PivotViewComponent.CHARTLEGENDFONTFAMILY)) {
        setOutputValue(PivotViewComponent.CHARTLEGENDFONTFAMILY,
            getInputStringValue(PivotViewComponent.CHARTLEGENDFONTFAMILY));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTLEGENDFONTSTYLE)) {
      if (isDefinedInput(PivotViewComponent.CHARTLEGENDFONTSTYLE)) {
        setOutputValue(PivotViewComponent.CHARTLEGENDFONTSTYLE,
            getInputStringValue(PivotViewComponent.CHARTLEGENDFONTSTYLE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTLEGENDFONTSIZE)) {
      if (isDefinedInput(PivotViewComponent.CHARTLEGENDFONTSIZE)) {
        setOutputValue(PivotViewComponent.CHARTLEGENDFONTSIZE,
            getInputStringValue(PivotViewComponent.CHARTLEGENDFONTSIZE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSHOWSLICER)) {
      if (isDefinedInput(PivotViewComponent.CHARTSHOWSLICER)) {
        setOutputValue(PivotViewComponent.CHARTSHOWSLICER, getInputStringValue(PivotViewComponent.CHARTSHOWSLICER));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSLICERLOCATION)) {
      if (isDefinedInput(PivotViewComponent.CHARTSLICERLOCATION)) {
        setOutputValue(PivotViewComponent.CHARTSLICERLOCATION,
            getInputStringValue(PivotViewComponent.CHARTSLICERLOCATION));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSLICERALIGNMENT)) {
      if (isDefinedInput(PivotViewComponent.CHARTSLICERALIGNMENT)) {
        setOutputValue(PivotViewComponent.CHARTSLICERALIGNMENT,
            getInputStringValue(PivotViewComponent.CHARTSLICERALIGNMENT));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSLICERFONTFAMILY)) {
      if (isDefinedInput(PivotViewComponent.CHARTSLICERFONTFAMILY)) {
        setOutputValue(PivotViewComponent.CHARTSLICERFONTFAMILY,
            getInputStringValue(PivotViewComponent.CHARTSLICERFONTFAMILY));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSLICERFONTSTYLE)) {
      if (isDefinedInput(PivotViewComponent.CHARTSLICERFONTSTYLE)) {
        setOutputValue(PivotViewComponent.CHARTSLICERFONTSTYLE,
            getInputStringValue(PivotViewComponent.CHARTSLICERFONTSTYLE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTSLICERFONTSIZE)) {
      if (isDefinedInput(PivotViewComponent.CHARTSLICERFONTSIZE)) {
        setOutputValue(PivotViewComponent.CHARTSLICERFONTSIZE,
            getInputStringValue(PivotViewComponent.CHARTSLICERFONTSIZE));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTBACKGROUNDR)) {
      if (isDefinedInput(PivotViewComponent.CHARTBACKGROUNDR)) {
        setOutputValue(PivotViewComponent.CHARTBACKGROUNDR, getInputStringValue(PivotViewComponent.CHARTBACKGROUNDR));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTBACKGROUNDG)) {
      if (isDefinedInput(PivotViewComponent.CHARTBACKGROUNDG)) {
        setOutputValue(PivotViewComponent.CHARTBACKGROUNDG, getInputStringValue(PivotViewComponent.CHARTBACKGROUNDG));
      }
    }

    if (isDefinedOutput(PivotViewComponent.CHARTBACKGROUNDB)) {
      if (isDefinedInput(PivotViewComponent.CHARTBACKGROUNDB)) {
        setOutputValue(PivotViewComponent.CHARTBACKGROUNDB, getInputStringValue(PivotViewComponent.CHARTBACKGROUNDB));
      }
    }

    // process the data source connection
    String dataSource = getInputStringValue(StandardSettings.JNDI);
    setOutputValue(StandardSettings.CONNECTION, dataSource); 

    // process the query
    String queryName = StandardSettings.SQL_QUERY;
    if (inputNames.contains(StandardSettings.QUERY_NAME)) {
      queryName = getInputStringValue(StandardSettings.QUERY_NAME);
    }
    String query = getInputStringValue(queryName);

    // if query = "default", generate a query
    if (query.equals(StandardSettings.DEFAULT)) {
      // get the default cube.  This is only useful if the schema contains more
      String cube = getInputStringValue(PivotViewComponent.CUBE);
      // we need to generate a query.
      query = MondrianModelComponent.getInitialQuery(model, dataSource, cube, roleName, getSession());
      
      if (query == null) {
        error(Messages.getInstance().getErrorString("PivotView.ERROR_0010_QUERY_GENERATION_FAILED")); //$NON-NLS-1$
        return false;
      }
    }

    String mdx = applyInputsToFormat(query);
    setOutputValue(StandardSettings.MDX_QUERY, mdx);

    String title = getInputStringValue(PivotViewComponent.TITLE);
    setOutputValue(PivotViewComponent.TITLE, title);

    // now process the options
    ArrayList options = new ArrayList();
    Element optionsNode = (Element) getComponentDefinition().selectSingleNode("options"); //$NON-NLS-1$
    List optionNodes = optionsNode.elements();
    Iterator optionsIterator = optionNodes.iterator();
    while (optionsIterator.hasNext()) {
      Element optionNode = (Element) optionsIterator.next();
      options.add(optionNode.getName());
    }
    if (options.size() > 0) {
      if (outputNames.contains(PivotViewComponent.OPTIONS)) {
        setOutputValue(PivotViewComponent.OPTIONS, options);
      } else {
        error(Messages.getInstance().getErrorString("PivotView.ERROR_0001_OPTIONS_NOT_DEFINED")); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

}
