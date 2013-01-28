package org.pentaho.platform.plugin.action.builtin;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.action.IStreamProcessingAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputHandler;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.util.messages.LocaleHelper;

public class ActionSequenceAction implements IStreamProcessingAction, IStreamingAction, IVarArgsAction {

  InputStream xactionDefInputStream;
  OutputStream xactionResultsOutputStream;
  Map<String, Object> xActionInputParams = new HashMap<String, Object>();
  
  public void setInputStream(InputStream arg0) {
    xactionDefInputStream = arg0;
  }

  public void execute() throws Exception {
    IOutputHandler outputHandler = null;
    if (xactionResultsOutputStream instanceof RepositoryFileOutputStream) {
      outputHandler = new RepositoryFileOutputHandler(((RepositoryFileOutputStream)xactionResultsOutputStream));
    } else {
      outputHandler = new SimpleOutputHandler(xactionResultsOutputStream, false);
    }
    IRuntimeContext rt = null;
    try {
      ISolutionEngine solutionEngine = PentahoSystem.get(ISolutionEngine.class, null);
      solutionEngine.setCreateFeedbackParameterCallback(null);
      solutionEngine.setLoggingLevel(ILogger.DEBUG);
      solutionEngine.setForcePrompt(false);
      
      ArrayList messages = new ArrayList();

      HashMap<String, Object> parameterProviders = new HashMap<String, Object>();
      parameterProviders.put(IParameterProvider.SCOPE_REQUEST, new SimpleParameterProvider(xActionInputParams));
      parameterProviders.put(IParameterProvider.SCOPE_SESSION, new PentahoSessionParameterProvider(PentahoSessionHolder.getSession()));
      String xactionPath = null;
      if (xactionDefInputStream instanceof RepositoryFileInputStream) {
        xactionPath = ((RepositoryFileInputStream)xactionDefInputStream).getFile().getPath();
      }
      rt = solutionEngine.execute(xactionPath,  this.getClass().getName(), false, true, null, true, parameterProviders, outputHandler, null, null, messages);

      if (!outputHandler.contentDone()) {
        if ((rt != null) && (rt.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS)) {
        	boolean isFlushed = false;
        	boolean isEmpty;
          if (xactionResultsOutputStream instanceof RepositoryFileOutputStream) {
          	RepositoryFileOutputStream repositoryFileOutputStream = (RepositoryFileOutputStream) xactionResultsOutputStream;
          	isFlushed = repositoryFileOutputStream.isFlushed();
          	isEmpty = repositoryFileOutputStream.size() > 0 ? false : true;
          	if (RepositoryFilenameUtils.getExtension(repositoryFileOutputStream.getFilePath()).isEmpty() && xactionResultsOutputStream.toString().isEmpty()) {
          		repositoryFileOutputStream.setFilePath(repositoryFileOutputStream.getFilePath() + ".html");
          	}
          } else {
          	isEmpty = xactionResultsOutputStream.toString().isEmpty();
          }
          
        	if (!isFlushed) {
        		if (isEmpty){
        			StringBuffer buffer = new StringBuffer();
        			PentahoSystem.get(IMessageFormatter.class, null).formatSuccessMessage(
        					"text/html", rt, buffer, false); //$NON-NLS-1$
        			xactionResultsOutputStream.write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
        		} 
       			xactionResultsOutputStream.close();
        	}
        } else {
          // we need an error message...
          StringBuffer buffer = new StringBuffer();
          PentahoSystem.get(IMessageFormatter.class, null).formatFailureMessage(
              "text/html", rt, buffer, messages); //$NON-NLS-1$
          	xactionResultsOutputStream.write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
          	xactionResultsOutputStream.close();
        }
      }
    } finally {
      if (rt != null) {
        rt.dispose();
      }
    }
  }

  public String getMimeType(String arg0) {
    return null;
  }

  public void setOutputStream(OutputStream arg0) {
    xactionResultsOutputStream = arg0;
  }

  public void setVarArgs(Map<String, Object> arg0) {
    xActionInputParams.clear();
    xActionInputParams.putAll(arg0);
  }

}
