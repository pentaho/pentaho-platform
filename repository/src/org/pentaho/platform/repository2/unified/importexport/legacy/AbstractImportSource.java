/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Feb 7, 2011 
 * @author wseyler
 */


package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository2.unified.importexport.ImportSource;

/**
 * @author wseyler
 *
 */
public abstract class AbstractImportSource implements ImportSource {
  /**
   * Keys are extensions and values are MIME types.
   */
  protected IUnifiedRepository unifiedRepository;
  protected Map<String, ImportSource> sourceTypes = new HashMap<String, ImportSource>();
  protected Map<String, String> mimeTypes = new HashMap<String, String>();
  protected List<ImportSource> dependentImportSources = new ArrayList<ImportSource>();
  protected List<IRepositoryFileBundle> files = new ArrayList<IRepositoryFileBundle>();  
  
  public AbstractImportSource() {
    super();
    
    mimeTypes.put("prpt", "application/zip"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("mondrian.xml", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("gif", "image/gif"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("css", "text/css"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("html", "text/html"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("htm", "text/html"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("jpg", "image/jpeg"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("jpeg", "image/jpeg"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("js", "text/javascript"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("cfg.xml", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("jrxml", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("kjb", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("ktr", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("png", "image/png"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("properties", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("report", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("rptdesign", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("svg", "image/svg+xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("url", "application/internet-shortcut"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("sql", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xaction", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xanalyzer", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xcdf", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xdash", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xmi", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xml", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xreportspec", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("waqr.xaction", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("waqr.xreportspec", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("waqr.xml", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("xwaqr", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put("cda", "text/xml"); //$NON-NLS-1$ //$NON-NLS-2$
    mimeTypes.put(null, null);
  }
  
  public void addFile(IRepositoryFileBundle file) {
  }
  
  public List<ImportSource> getDependentImportSources() {
	  return dependentImportSources;
  }
  
  protected ImportSource resolveDependentImportSource(String key) {
	  if(sourceTypes.isEmpty()) {
		  initializeSourceTypes();
	  }
	  ImportSource importSource = sourceTypes.get(key);
	  if(importSource != null) {
		  if(!dependentImportSources.contains(importSource)) {
			  dependentImportSources.add(importSource);
		  }
	  }
	  return importSource;
  }
  
  private void initializeSourceTypes() {
	  MondrianSchemaImportSource importSource = new MondrianSchemaImportSource();
	  sourceTypes.put(".mondrian.xml", importSource);
	  sourceTypes.put("datasources.xml", importSource);
  }
  
  public String getRequiredCharset() {
	return null;
  }

  public void setOwnerName(String ownerName) {
  }

  public void setRequiredCharset(String charset) {
  }
	
  public IRepositoryFileBundle getFile(String path) {
	return null;
  }
	
  public void initialize(IUnifiedRepository unifidedRepository) {
	  this.unifiedRepository = unifidedRepository;
  }
}
