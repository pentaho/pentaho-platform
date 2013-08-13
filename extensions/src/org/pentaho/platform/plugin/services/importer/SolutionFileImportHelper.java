package org.pentaho.platform.plugin.services.importer;

import java.util.ArrayList;
import java.util.List;

public class SolutionFileImportHelper {

	private List<String> hiddenExtensionList;
	private List<String> approvedExtensionList;

	SolutionFileImportHelper(List<String> hiddenExtensionList, List<String> approvedExtensionList) {
		this.hiddenExtensionList = new ArrayList<String>(hiddenExtensionList);
		this.approvedExtensionList = new ArrayList<String>(approvedExtensionList);
	}
	
	public boolean isInApprovedExtensionList(String fileName) {
		boolean isInTheApprovedExtensionList = false;
		for (String extension : approvedExtensionList) {
			if (fileName.endsWith(extension)) {
				isInTheApprovedExtensionList = true;
				break;
			}
		}
		return isInTheApprovedExtensionList;
	}
	

	public boolean isInHiddenList(String fileName) {
		boolean isInTheHiddenList = false;
		for (String extension : hiddenExtensionList) {
			if (fileName.endsWith(extension)) {
				isInTheHiddenList = true;
				break;
			}
		}
		return isInTheHiddenList;
	}
}
