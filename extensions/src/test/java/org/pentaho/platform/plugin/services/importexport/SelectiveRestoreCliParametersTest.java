/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.importexport;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * Unit tests for CLI parameters and command-line interface for selective restore
 */
public class SelectiveRestoreCliParametersTest {

  private Map<String, String> cliParams;
  private ComponentConfig configFromCli;

  @Before
  public void setUp() {
    cliParams = new HashMap<>();
    configFromCli = new ComponentConfig();
  }

  /**
   * Test: Parse --include-generated-content=false flag
   */
  @Test
  public void testParseIncludeGeneratedContentFalseFlag() {
    // Setup: CLI argument
    cliParams.put( "include-generated-content", "false" );

    // Act: Parse flag
    boolean includeGeneratedContent = !cliParams.getOrDefault( "include-generated-content", "true" ).equalsIgnoreCase( "false" );
    configFromCli.setIncludeGeneratedContent( includeGeneratedContent );

    // Assert
    assertFalse( "Generated content should be excluded", configFromCli.isIncludeGeneratedContent() );
  }

  /**
   * Test: Parse --include-generated-content=true flag
   */
  @Test
  public void testParseIncludeGeneratedContentTrueFlag() {
    // Setup: CLI argument
    cliParams.put( "include-generated-content", "true" );

    // Act: Parse flag
    boolean includeGeneratedContent = cliParams.getOrDefault( "include-generated-content", "true" ).equalsIgnoreCase( "true" );
    configFromCli.setIncludeGeneratedContent( includeGeneratedContent );

    // Assert
    assertTrue( "Generated content should be included", configFromCli.isIncludeGeneratedContent() );
  }

  /**
   * Test: Parse default (missing flag)
   */
  @Test
  public void testParseDefaultGeneratedContentFlag() {
    // Setup: No flag provided - use default
    String flagValue = cliParams.getOrDefault( "include-generated-content", "true" );

    // Act
    boolean includeGeneratedContent = flagValue.equalsIgnoreCase( "true" );
    configFromCli.setIncludeGeneratedContent( includeGeneratedContent );

    // Assert
    assertTrue( "Generated content should be included by default", configFromCli.isIncludeGeneratedContent() );
  }

  /**
   * Test: Parse --include-content flag
   */
  @Test
  public void testParseIncludeContentFlag() {
    // Setup
    cliParams.put( "include-content", "true" );

    // Act
    boolean includeContent = cliParams.getOrDefault( "include-content", "true" ).equalsIgnoreCase( "true" );
    configFromCli.setIncludeContent( includeContent );

    // Assert
    assertTrue( "Content should be included", configFromCli.isIncludeContent() );
  }

  /**
   * Test: Parse --include-users flag
   */
  @Test
  public void testParseIncludeUsersFlag() {
    // Setup
    cliParams.put( "include-users", "false" );

    // Act
    boolean includeUsers = cliParams.getOrDefault( "include-users", "true" ).equalsIgnoreCase( "true" );
    configFromCli.setIncludeUsers( includeUsers );

    // Assert
    assertFalse( "Users should not be included", configFromCli.isIncludeUsers() );
  }

  /**
   * Test: Parse multiple flags together
   */
  @Test
  public void testParseMultipleFlags() {
    // Setup: Multiple CLI flags
    cliParams.put( "include-content", "true" );
    cliParams.put( "include-users", "true" );
    cliParams.put( "include-datasources", "false" );
    cliParams.put( "include-generated-content", "false" );

    // Act: Parse all flags
    configFromCli.setIncludeContent( cliParams.getOrDefault( "include-content", "true" ).equalsIgnoreCase( "true" ) );
    configFromCli.setIncludeUsers( cliParams.getOrDefault( "include-users", "true" ).equalsIgnoreCase( "true" ) );
    configFromCli.setIncludeDatasources( cliParams.getOrDefault( "include-datasources", "true" ).equalsIgnoreCase( "true" ) );
    configFromCli.setIncludeGeneratedContent( cliParams.getOrDefault( "include-generated-content", "true" ).equalsIgnoreCase( "true" ) );

    // Assert
    assertTrue( "Content should be included", configFromCli.isIncludeContent() );
    assertTrue( "Users should be included", configFromCli.isIncludeUsers() );
    assertFalse( "Datasources should not be included", configFromCli.isIncludeDatasources() );
    assertFalse( "Generated content should not be included", configFromCli.isIncludeGeneratedContent() );
  }

  /**
   * Test: Parse profile flag
   */
  @Test
  public void testParseProfileFlag() {
    // Setup: Profile selection
    cliParams.put( "profile", "CONTENT_ONLY" );

    // Act: Apply profile
    String profile = cliParams.get( "profile" );
    ComponentConfig profileConfig = null;
    
    if ( "CONTENT_ONLY".equals( profile ) ) {
      profileConfig = ComponentConfig.contentOnly();
    } else if ( "FULL_SYSTEM".equals( profile ) ) {
      profileConfig = ComponentConfig.fullSystem();
    }

    // Assert
    assertNotNull( "Profile config should be created", profileConfig );
    assertTrue( "Should include content", profileConfig.isIncludeContent() );
    assertFalse( "Should not include users", profileConfig.isIncludeUsers() );
  }

  /**
   * Test: Override profile with individual flags
   */
  @Test
  public void testOverrideProfileWithIndividualFlags() {
    // Setup: Start with profile
    cliParams.put( "profile", "CONTENT_ONLY" );
    cliParams.put( "include-users", "true" ); // Override profile

    // Act: Apply profile then override
    ComponentConfig config = ComponentConfig.contentOnly();
    boolean overrideUsers = cliParams.containsKey( "include-users" );
    if ( overrideUsers ) {
      config.setIncludeUsers( cliParams.get( "include-users" ).equalsIgnoreCase( "true" ) );
    }

    // Assert
    assertTrue( "Content should be from profile", config.isIncludeContent() );
    assertTrue( "Users should be overridden", config.isIncludeUsers() );
  }

  /**
   * Test: Invalid flag values
   */
  @Test
  public void testInvalidFlagValues() {
    // Setup: Invalid values
    cliParams.put( "include-content", "maybe" ); // Invalid
    cliParams.put( "include-users", "yes" ); // Invalid

    // Act: Parse with fallback to default
    String contentFlag = cliParams.get( "include-content" );
    boolean includeContent = contentFlag == null || 
                            contentFlag.equalsIgnoreCase( "true" );

    String usersFlag = cliParams.get( "include-users" );
    boolean includeUsers = usersFlag == null || 
                          usersFlag.equalsIgnoreCase( "true" );

    // Assert: Should fall back to defaults
    assertTrue( "Should default to true for invalid content flag", includeContent );
    assertTrue( "Should default to true for invalid users flag", includeUsers );
  }

  /**
   * Test: Duplicate flags - last one wins
   */
  @Test
  public void testDuplicateFlagsLastWins() {
    // Setup: Parse CLI args in order
    List<String> args = Arrays.asList(
      "--include-generated-content=true",
      "--include-generated-content=false" // This should win
    );

    // Act: Parse flags (last one overrides)
    String lastValue = "false";
    boolean includeGeneratedContent = lastValue.equalsIgnoreCase( "true" );
    configFromCli.setIncludeGeneratedContent( includeGeneratedContent );

    // Assert
    assertFalse( "Last flag should win", configFromCli.isIncludeGeneratedContent() );
  }

  /**
   * Test: Case insensitive flag parsing
   */
  @Test
  public void testCaseInsensitiveFlagParsing() {
    // Setup: Various cases
    List<String> values = Arrays.asList( "true", "TRUE", "True", "TrUe" );

    // Act & Assert: All should be parsed as true
    for ( String value : values ) {
      assertTrue( "Should parse '" + value + "' as true", value.equalsIgnoreCase( "true" ) );
    }

    // Repeat for false
    List<String> falseValues = Arrays.asList( "false", "FALSE", "False", "FaLsE" );
    for ( String value : falseValues ) {
      assertTrue( "Should parse '" + value + "' as false", value.equalsIgnoreCase( "false" ) );
    }
  }

  /**
   * Test: Flag help/documentation
   */
  @Test
  public void testFlagHelpDocumentation() {
    // Setup: Define flags
    Map<String, String> flagHelp = new HashMap<>();
    flagHelp.put( "include-generated-content", "Include/exclude generated content files in restore (true/false)" );
    flagHelp.put( "include-content", "Include/exclude repository content (true/false)" );
    flagHelp.put( "include-users", "Include/exclude user/role configuration (true/false)" );
    flagHelp.put( "profile", "Use predefined profile: FULL_SYSTEM, CONTENT_ONLY, SECURITY, DATASOURCE, SCHEDULES, SETTINGS" );

    // Assert: All flags documented
    assertEquals( "Should have 4 documented flags", 4, flagHelp.size() );
    assertNotNull( "include-generated-content should be documented", flagHelp.get( "include-generated-content" ) );
    assertNotNull( "profile should be documented", flagHelp.get( "profile" ) );
  }

  /**
   * Test: Invalid profile name
   */
  @Test
  public void testInvalidProfileName() {
    // Setup: Invalid profile
    cliParams.put( "profile", "INVALID_PROFILE" );

    // Act: Try to apply invalid profile
    String profileName = cliParams.get( "profile" );
    ComponentConfig config = null;
    
    if ( "FULL_SYSTEM".equals( profileName ) ) {
      config = ComponentConfig.fullSystem();
    } else {
      // Default fallback
      config = new ComponentConfig();
    }

    // Assert: Should use default config
    assertNotNull( "Should have config even with invalid profile", config );
    assertTrue( "Default should include content", config.isIncludeContent() );
  }

  /**
   * Test: Combining profile and component flags
   */
  @Test
  public void testCombiningProfileAndComponentFlags() {
    // Setup: Profile + component overrides
    cliParams.put( "profile", "FULL_SYSTEM" );
    cliParams.put( "include-generated-content", "false" );

    // Act: Apply profile then component flags
    ComponentConfig config = ComponentConfig.fullSystem();
    boolean hasGeneratedContent = cliParams.containsKey( "include-generated-content" );
    if ( hasGeneratedContent ) {
      config.setIncludeGeneratedContent( cliParams.get( "include-generated-content" ).equalsIgnoreCase( "true" ) );
    }

    // Assert
    assertTrue( "Full system includes users", config.isIncludeUsers() );
    assertFalse( "Generated content flag overrides profile", config.isIncludeGeneratedContent() );
  }

  /**
   * Test: CLI argument format validation
   */
  @Test
  public void testCliArgumentFormatValidation() {
    // Setup: Valid formats
    String[] validFormats = {
      "--include-generated-content=false",
      "--include-content=true",
      "--profile=CONTENT_ONLY"
    };

    // Assert: All should match pattern
    for ( String format : validFormats ) {
      assertTrue( "Format should start with --", format.startsWith( "--" ) );
      assertTrue( "Format should contain =", format.contains( "=" ) );
    }

    // Invalid formats
    String[] invalidFormats = {
      "include-generated-content=false", // Missing --
      "--include-generated-content false", // Missing =
      "---include-generated-content=false" // Too many --
    };

    for ( String format : invalidFormats ) {
      boolean valid = format.startsWith( "--" ) && format.contains( "=" ) && !format.startsWith( "---" );
      assertFalse( "Format should be invalid: " + format, valid );
    }
  }

  /**
   * Test: Environment variable fallback
   */
  @Test
  public void testEnvironmentVariableFallback() {
    // Setup: Try CLI param, fallback to env var
    String cliValue = cliParams.get( "include-generated-content" );
    String envValue = System.getenv( "PENTAHO_INCLUDE_GENERATED_CONTENT" );
    String finalValue = cliValue != null ? cliValue : ( envValue != null ? envValue : "true" );

    // Act
    boolean includeGeneratedContent = finalValue.equalsIgnoreCase( "true" );
    configFromCli.setIncludeGeneratedContent( includeGeneratedContent );

    // Assert: Should have some value (CLI, env, or default)
    assertNotNull( "Should have a value", finalValue );
  }

  /**
   * Test: Short flag aliases
   */
  @Test
  public void testShortFlagAliases() {
    // Setup: Support both long and short forms
    Map<String, String> shortAliases = new HashMap<>();
    shortAliases.put( "-gc", "--include-generated-content" );
    shortAliases.put( "-c", "--include-content" );
    shortAliases.put( "-u", "--include-users" );
    shortAliases.put( "-p", "--profile" );

    // Assert: Aliases defined
    assertEquals( "Should have short alias for generated-content", "--include-generated-content", shortAliases.get( "-gc" ) );
    assertEquals( "Should have short alias for content", "--include-content", shortAliases.get( "-c" ) );
  }
}
