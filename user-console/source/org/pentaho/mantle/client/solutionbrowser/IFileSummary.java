package org.pentaho.mantle.client.solutionbrowser;

/**
 * Instances contain vital information about a solution file: its name, solution, path, and localized name. This 
 * interface allows {@code FileCommand} to know the file (whether it be a folder in the solution tree or a file in the 
 * files list) being acted on. 
 * 
 * @author mlowery
 */
public interface IFileSummary {
  String getLocalizedName();
  String getName();
  String getPath();
  String getSolution();
}
