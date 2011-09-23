package org.pentaho.platform.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;

/**
 * Servlet for handling requests for Schedule-produced content
 *
 * web.servlet name="SubscriptionContentAction" display-name="SubscriptionContentAction" description="Servlet for handling requests for Schedule-produced content" web.servlet-mapping url-pattern="/SubscriptionContentAction"
 */
public class SubscriptionContentAction extends ServletBase {

  private static final long serialVersionUID = 7371241371872967841L;

  private static final Log logger = LogFactory.getLog(SubscriptionContentAction.class);

  @Override
  public Log getLogger() {
    return logger;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PentahoSystem.systemEntryPoint();
    try {
      IPentahoSession userSession = getPentahoSession(request);
      OutputStream outputStream = getOutputStream(response, doMessages(request));
      IParameterProvider requestParameters = new HttpRequestParameterProvider(request);
      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
      SimpleUrlFactory urlFactory = new SimpleUrlFactory(requestContext.getContextPath() + "SubscriptionContentAction?"); //$NON-NLS-1$

      String subscribeAction = request.getParameter("subscribe"); //$NON-NLS-1$

      if ("run".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
        HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
        SubscriptionHelper.runSubscription(name, userSession, sessionParameters, urlFactory, outputHandler);
      } else if ("archived".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        int pos = name.lastIndexOf(':');
        if (pos != -1) {
          String fileId = name.substring(pos + 1);
          name = name.substring(0, pos);
          HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
          SubscriptionHelper.getArchived(name, fileId, userSession, outputHandler);
        }
      } else if ("archive".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
        HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
        IContentItem contentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT,
            null, null, "text/html"); //$NON-NLS-1$
        outputStream = contentItem.getOutputStream(name);
        String resp = null;
        try {
          resp = SubscriptionHelper.createSubscriptionArchive(name, userSession, null, sessionParameters);
          outputStream.write(resp.getBytes());
          contentItem.closeOutputStream();
        } catch (BackgroundExecutionException bex) {
          resp = bex.getLocalizedMessage();
          error(Messages.getInstance().getErrorString("ViewAction.ERROR_0003_UNABLE_TO_CREATE_SUBSCRIPTION_ARCHIVE")); //$NON-NLS-1$
          outputStream.write(resp.getBytes());
          contentItem.closeOutputStream();
        }
      } else if ("save".equals(subscribeAction)) { //$NON-NLS-1$
        String solutionName = requestParameters.getStringParameter("solution", null); //$NON-NLS-1$
        String actionPath = requestParameters.getStringParameter("path", null); //$NON-NLS-1$
        String actionName = requestParameters.getStringParameter("action", null); //$NON-NLS-1$
        //        String actionReference = solutionName + "/" + actionPath + "/" + actionName; //$NON-NLS-1$ //$NON-NLS-2$
        //        The above code would result in an extra slash if there was no actionpath.  The code below detects this.
        //        PPP-2337
        String actionReference = solutionName;
        if (actionPath != null && actionPath.length() > 0) {
          actionReference += ("/" + actionPath); //$NON-NLS-1$
        }
        actionReference += ("/" + actionName); //$NON-NLS-1$
        // HttpSessionParameterProvider sessionParameters = new
        // HttpSessionParameterProvider( userSession );
        String result = SubscriptionHelper.saveSubscription(requestParameters, actionReference, userSession);
        outputStream.write(result.getBytes());
      } else if ("edit".equals(subscribeAction)) { //$NON-NLS-1$
        // TODO
        // get the action information from the subscription
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$ 
        SubscriptionHelper.editSubscription(name, userSession, urlFactory, outputStream);
        /*
         * 
         * SimpleParameterSetter parameters = new SimpleParameterSetter(); String result = SubscriptionHelper.getSubscriptionParameters( name, parameters,
         * userSession ); outputPreference = IOutputHandler.OUTPUT_TYPE_PARAMETERS; requestParameters = parameters; SubscriptionHelper.editSubscription if( result !=
         * null ) { outputStream.write(result.getBytes()); return; }
         */
      } else if ("delete".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        String result = SubscriptionHelper.deleteSubscription(name, userSession);
        outputStream.write(result.getBytes());
      } else if ("delete-archived".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        int pos = name.lastIndexOf(':');
        if (pos != -1) {
          String fileId = name.substring(pos + 1);
          name = name.substring(0, pos);
          String result = SubscriptionHelper.deleteSubscriptionArchive(name, fileId, userSession);
          outputStream.write(result.getBytes());
        }
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  protected OutputStream getOutputStream(final HttpServletResponse response, final boolean doMessages)
      throws ServletException, IOException {
    OutputStream outputStream = null;
    if (doMessages) {
      outputStream = new ByteArrayOutputStream();
    } else {
      outputStream = response.getOutputStream();
    }

    return outputStream;
  }

  protected boolean doMessages(final HttpServletRequest request) {
    return "true".equalsIgnoreCase(request.getParameter("debug")); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
