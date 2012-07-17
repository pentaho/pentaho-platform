package org.pentaho.platform.scheduler2.quartz;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * This Subclasses of the embeddedQuartzSystemListener is to prevent the check for
 * database creation and calling of the creation script, which we only want to
 * happen with people running the H2 embedded database.
 *
 * User: nbaker
 * Date: 7/17/12
 */
public class StandaloneQuartzSystemListener extends EmbeddedQuartzSystemListener{

  @Override
  protected boolean verifyQuartzIsConfigured(DataSource ds) throws SQLException {
    return true;
  }
}