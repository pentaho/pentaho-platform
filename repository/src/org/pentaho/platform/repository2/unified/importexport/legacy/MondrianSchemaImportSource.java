/*
 * Copyright 2011 Pentaho Corporation.  All rights reserved. 
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
 * @created Jan 28, 2011 
 * @author wseyler
 */

package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.io.IOException;

/**
 * @author Ezequiel Cuellar
 * 
 */
public class MondrianSchemaImportSource extends AbstractImportSource {

	public MondrianSchemaImportSource() {
	}

	@Override
	public IRepositoryFileBundle getFile(String path) {
		return null;
	}

	@Override
	public Iterable<IRepositoryFileBundle> getFiles() throws IOException {
		return super.files;
	}

	@Override
	public String getRequiredCharset() {
		return null;
	}

	@Override
	public void setOwnerName(String ownerName) {
	}

	@Override
	public void setRequiredCharset(String charset) {
	}

	@Override
	public void addFile(IRepositoryFileBundle file) {
		super.files.add(file);
	}

	public String getUploadDir() {
		return "/etc/mondrian";
	}
}
