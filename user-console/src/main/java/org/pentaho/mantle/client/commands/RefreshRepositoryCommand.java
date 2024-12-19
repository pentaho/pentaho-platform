/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.commands;

import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

public class RefreshRepositoryCommand extends AbstractCommand {

  private static native void setupNativeHooks( RefreshRepositoryCommand cmd )
  /*-{
    $wnd.mantle_refreshRepository = function() {
      cmd.@org.pentaho.mantle.client.commands.RefreshRepositoryCommand::execute(Z)(false);
    }
  }-*/;

  public RefreshRepositoryCommand() {
    setupNativeHooks( this );
  }

  public void execute() {
    super.execute();
    RepositoryFileTreeManager.getInstance().beforeFetchRepositoryFileTree();
  }

  protected void performOperation( final boolean feedback ) {
    RepositoryFileTreeManager.getInstance().fetchRepositoryFileTree( true, null, null,
        SolutionBrowserPanel.getInstance().getSolutionTree().isShowHiddenFiles() );
  }

  protected void performOperation() {
    performOperation( true );
  }

}
