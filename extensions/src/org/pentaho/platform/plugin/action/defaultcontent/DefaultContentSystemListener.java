package org.pentaho.platform.plugin.action.defaultcontent;

import java.io.File;
import java.util.concurrent.Callable;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.services.importer.ArchiveLoader;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.util.logging.Logger;

public class DefaultContentSystemListener implements IPentahoSystemListener {

	private static final String DEFAULT_CONTENT_FOLDER = "system/default-content";

	@Override
	public void shutdown() {	
	}

	@Override
	public boolean startup(IPentahoSession arg0) {
		
		try {
			SecurityHelper.getInstance().runAsSystem(new Callable<Void>() {
				
				@Override
		        public Void call() throws Exception {
					Logger.info(this.getClass().getName(), "Default content importer has started");
						
					//  get a File reference to the directory
					String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(DEFAULT_CONTENT_FOLDER);
					File directory = new File(solutionPath);
						
					//  Instantiate the importer
					IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
					ArchiveLoader archiveLoader = new ArchiveLoader(importer); 
					archiveLoader.loadAll(directory, ArchiveLoader.ZIPS_FILTER);
					return null;
				}
			});
		}
		catch (Exception e) {
			Logger.error(this.getClass().getName(), e.getMessage());
		}
		
		//  we return true even of an exception is caught.
		//  if we would return a false then the server will not start.
		return true;
	}
}