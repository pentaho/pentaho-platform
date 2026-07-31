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

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates user (request) supplied SQL before it is handed to a JDBC connection.
 * <p/>
 * The validator is deliberately <i>fail closed</i>: a statement is only accepted when it can be proven to be a
 * single, read-only <code>SELECT</code> (or <code>WITH ... SELECT</code>) statement. Anything that cannot be parsed,
 * that contains more than one statement, that contains comments, or that references a well known
 * time-based/out-of-band injection primitive is rejected.
 * <p/>
 * Validation happens in three layers:
 * <ol>
 * <li>Lexical: string literals are masked, then comments, stacked statements and unterminated literals are
 * rejected.</li>
 * <li>Denylist: known dangerous keywords/functions (e.g. <code>DBMS_SESSION</code>, <code>pg_sleep</code>,
 * <code>xp_cmdshell</code>, DML/DDL verbs) are rejected.</li>
 * <li>Grammar: the statement is parsed and must resolve to exactly one {@link Select}.</li>
 * </ol>
 */
@SuppressWarnings( "deprecation" ) public final class SqlQueryValidator {
  /**
   * Upper bound for the accepted SQL length. Bounds the work done by the parser.
   */
  public static final int MAX_QUERY_LENGTH = 8192;

  /**
   * Maximum time, in milliseconds, the SQL parser may spend on a single statement.
   */
  public static final long PARSER_TIMEOUT_MILLIS = 2000L;

  /**
   * Maximum number of parentheses that may wrap the top level <code>SELECT</code>. Bounds the unwrapping loop.
   */
  private static final int MAX_PARENTHESES_DEPTH = 16;

  /**
   * Keywords and functions that must never appear in a chart query. These cover DML/DDL, procedural blocks and the
   * usual time-based / out-of-band SQL injection primitives across Oracle, PostgreSQL, MySQL, MSSQL and DB2.
   */
  private static final String[] DENIED_TOKENS = {
    // procedural / statement execution ("end" is intentionally absent: CASE ... END is legitimate)
    "begin", "declare", "exec", "execute", "call", "perform", "prepare", "deallocate",
    // DML / DDL / transaction control
    "insert", "update", "delete", "merge", "upsert", "drop", "alter", "create", "truncate", "rename",
    "grant", "revoke", "commit", "rollback", "savepoint", "shutdown", "set", "reset", "use", "lock", "vacuum",
    "analyze", "reindex", "cluster", "copy", "load", "import", "export", "backup", "restore", "kill",
    // result redirection / file access
    "into", "outfile", "dumpfile", "infile", "load_file", "lo_import", "lo_export", "pg_read_file",
    "pg_read_binary_file", "pg_ls_dir", "bfilename", "openrowset", "opendatasource", "openquery", "openxml",
    // time based blind injection
    "sleep", "pg_sleep", "benchmark", "waitfor", "generate_series", "randomblob",
    // vendor packages commonly abused for RCE / SSRF / DoS
    "dbms_lock", "dbms_session", "dbms_pipe", "dbms_scheduler", "dbms_job", "dbms_xmlgen", "dbms_sql",
    "dbms_advisor", "dbms_ldap", "dbms_output", "utl_http", "utl_file", "utl_smtp", "utl_tcp", "utl_inaddr",
    "httpuritype", "xmltype", "extractvalue", "updatexml", "dblink", "dbms_utility",
    "xp_cmdshell", "xp_dirtree", "xp_fileexist", "xp_regread", "sp_executesql", "sp_oacreate", "sp_oamethod",
    "sp_makewebtask", "sys_exec", "sys_eval", "sys_context",
    // set operations - a chart query must read from a single result shape
    "union", "intersect", "minus", "except"
  };

  private static final Pattern DENIED_TOKEN_PATTERN = buildDeniedTokenPattern();

  private SqlQueryValidator() {
    // static utility
  }

  /**
   * Convenience wrapper around {@link #validateReadOnlySelect(String)}.
   *
   * @param sql the statement to inspect, may be <code>null</code>
   * @return <code>true</code> when the statement is a single read-only <code>SELECT</code>
   */
  public static boolean isReadOnlySelect( final String sql ) {
    try {
      validateReadOnlySelect( sql );
      return true;
    } catch ( SqlValidationException e ) {
      return false;
    }
  }

  /**
   * Verifies that the supplied SQL is a single, read-only <code>SELECT</code> statement.
   *
   * @param sql the statement to inspect
   * @throws SqlValidationException when the statement is empty, malformed, contains comments, contains more than one
   *                                statement, uses a denied keyword/function or is not a <code>SELECT</code>
   */
  public static void validateReadOnlySelect( final String sql ) throws SqlValidationException {
    if ( sql == null || sql.trim().isEmpty() ) {
      throw new SqlValidationException( "The query is empty." );
    }

    if ( sql.length() > MAX_QUERY_LENGTH ) {
      throw new SqlValidationException( "The query exceeds the maximum allowed length of " + MAX_QUERY_LENGTH
        + " characters." );
    }

    // Layer 1 - lexical analysis over a copy where the contents of string literals are blanked out, so that data
    // never influences the structural checks below.
    final String masked = maskLiterals( sql );

    rejectComments( masked );
    rejectStackedStatements( masked );

    // Layer 2 - denylist of tokens that have no place in a read-only chart query.
    rejectDeniedTokens( masked );

    // Layer 3 - the statement has to parse, and it has to parse to exactly one plain SELECT.
    rejectNonSelect( sql );
  }

  /**
   * Replaces the content of quoted literals/identifiers with blanks so that structural checks are not confused by
   * user data. An unterminated literal is treated as an attack attempt.
   */
  private static String maskLiterals( final String sql ) throws SqlValidationException {
    final char[] chars = sql.toCharArray();
    final StringBuilder masked = new StringBuilder( chars.length );

    int i = 0;

    while ( i < chars.length ) {
      final char current = chars[ i ];

      if ( current == '\'' || current == '"' || current == '`' ) {
        masked.append( current );
        i++;
        boolean closed = false;

        while ( i < chars.length ) {
          final char inner = chars[ i ];

          if ( inner == '\\' && i + 1 < chars.length ) {
            // escaped character - blank both out
            masked.append( ' ' ).append( ' ' );
            i += 2;
            continue;
          }

          if ( inner == current ) {
            if ( i + 1 < chars.length && chars[ i + 1 ] == current ) {
              // doubled quote, i.e. an escaped quote inside the literal
              masked.append( ' ' ).append( ' ' );
              i += 2;
              continue;
            }

            masked.append( current );
            i++;
            closed = true;
            break;
          }

          masked.append( inner == '\n' || inner == '\r' ? inner : ' ' );
          i++;
        }

        if ( !closed ) {
          throw new SqlValidationException( "The query contains an unterminated quoted literal." );
        }
      } else {
        masked.append( current );
        i++;
      }
    }

    return masked.toString();
  }

  private static void rejectComments( final String masked ) throws SqlValidationException {
    if ( masked.contains( "--" ) || masked.contains( "/*" ) || masked.contains( "*/" ) || masked.indexOf( '#' ) >= 0 ) {
      throw new SqlValidationException( "SQL comments are not allowed in the query." );
    }
  }

  private static void rejectStackedStatements( final String masked ) throws SqlValidationException {
    final int separator = masked.indexOf( ';' );

    if ( separator >= 0 && !masked.substring( separator + 1 ).trim().isEmpty() ) {
      throw new SqlValidationException( "Only a single statement is allowed in the query." );
    }
  }

  private static void rejectDeniedTokens( final String masked ) throws SqlValidationException {
    final Matcher matcher = DENIED_TOKEN_PATTERN.matcher( masked );

    if ( matcher.find() ) {
      throw new SqlValidationException( "The query uses a keyword or function that is not allowed: "
        + matcher.group() );
    }
  }

  private static void rejectNonSelect( final String sql ) throws SqlValidationException {
    final Statements statements;

    try {
      statements = CCJSqlParserUtil.parseStatements( sql,
        parser -> parser.withTimeOut( PARSER_TIMEOUT_MILLIS ).withAllowComplexParsing( true ) );
    } catch ( Exception e ) {
      // Anything the parser cannot understand is rejected, including PL/SQL blocks and vendor extensions.
      throw new SqlValidationException( "The query could not be parsed as a valid SQL statement.", e );
    }

    if ( statements == null || statements.getStatements() == null || statements.getStatements().size() != 1 ) {
      throw new SqlValidationException( "Only a single statement is allowed in the query." );
    }

    final Statement statement = statements.getStatements().get( 0 );

    if ( !( statement instanceof Select select ) ) {
      throw new SqlValidationException( "Only SELECT statements are allowed in the query." );
    }

    // Select is abstract: PlainSelect, SetOperationList, Values, ParenthesedSelect, LateralSubSelect and
    // TableStatement are all Selects. Only a (possibly parenthesised) plain SELECT is accepted, so that set
    // operations, VALUES lists and TABLE statements are rejected regardless of how they are wrapped.
    Select unwrapped = select;
    int depth = 0;

    while ( unwrapped instanceof ParenthesedSelect parenthesed ) {
      if ( ++depth > MAX_PARENTHESES_DEPTH ) {
        throw new SqlValidationException( "The query nests parentheses too deeply." );
      }

      unwrapped = parenthesed.getSelect();

      if ( unwrapped == null ) {
        throw new SqlValidationException( "Only SELECT statements are allowed in the query." );
      }
    }

    if ( !( unwrapped instanceof PlainSelect ) ) {
      throw new SqlValidationException( "Only a single plain SELECT statement is allowed in the query; set "
        + "operations, VALUES lists and TABLE statements are not allowed." );
    }
  }

  private static Pattern buildDeniedTokenPattern() {
    final StringBuilder regex = new StringBuilder( "\\b(?:" );

    for ( int i = 0; i < DENIED_TOKENS.length; i++ ) {
      if ( i > 0 ) {
        regex.append( '|' );
      }

      regex.append( Pattern.quote( DENIED_TOKENS[ i ] ) );
    }

    regex.append( ")\\b" );

    return Pattern.compile( regex.toString(), Pattern.CASE_INSENSITIVE );
  }
}
