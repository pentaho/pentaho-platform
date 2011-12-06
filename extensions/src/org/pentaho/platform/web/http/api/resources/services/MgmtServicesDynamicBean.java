package org.pentaho.platform.web.http.api.resources.services;

import javax.management.DynamicMBean;

public interface MgmtServicesDynamicBean extends DynamicMBean {

	public String getId();

	public String getDisplayName();
	
	public String getJmxName();
	
	public void reset();
}
