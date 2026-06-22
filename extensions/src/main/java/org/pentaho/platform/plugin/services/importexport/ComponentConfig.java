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

package org.pentaho.platform.plugin.services.importexport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for selective backup/restore of repository components.
 * Allows users to choose which system components to include in a backup.
 *
 * @author Pentaho Platform Team
 * @since 9.0
 */
public class ComponentConfig implements Serializable {
  private static final long serialVersionUID = 1L;

  // Component flags
  @JsonProperty("includeContent")
  private boolean includeContent = true;
  @JsonProperty("includeUsers")
  private boolean includeUsers = true;
  @JsonProperty("includeDatasources")
  private boolean includeDatasources = true;
  @JsonProperty("includeMetastore")
  private boolean includeMetastore = true;
  @JsonProperty("includeSchedules")
  private boolean includeSchedules = true;
  
  /**
   * Include user settings, preferences, email addresses, and group assignments.
   * When true, the backup will include:
   * - Email addresses and email group settings
   * - Group definitions and memberships
   * - User preferences and personal settings
   * 
   * Controlled by the EmailsGroupsExportUtil in the scheduler plugin.
   */
  @JsonProperty("includeUserSettings")
  private boolean includeUserSettings = true;
  
  @JsonProperty("includeMondrian")
  private boolean includeMondrian = true;

  // Content filtering options
  @JsonProperty("includeGeneratedContent")
  private boolean includeGeneratedContent = true;

  // Configuration metadata
  @JsonProperty("backupName")
  private String backupName;
  @JsonProperty("description")
  private String description;
  @JsonProperty("createdTimestamp")
  private long createdTimestamp;
  @JsonProperty("version")
  private int version = 1;

  /**
   * Default constructor - includes all components
   */
  public ComponentConfig() {
    this.createdTimestamp = System.currentTimeMillis();
  }

  /**
   * Constructor with backup name
   */
  public ComponentConfig( String backupName ) {
    this();
    this.backupName = backupName;
  }

  /**
   * Full system backup - includes all components
   */
  public static ComponentConfig fullSystem() {
    ComponentConfig config = new ComponentConfig( "Full System Backup" );
    config.includeContent = true;
    config.includeUsers = true;
    config.includeDatasources = true;
    config.includeMetastore = true;
    config.includeSchedules = true;
    config.includeUserSettings = true;
    config.includeMondrian = true;
    config.includeGeneratedContent = true; // Include generated content by default
    return config;
  }

  /**
   * Content only backup - repository files/folders only
   */
  public static ComponentConfig contentOnly() {
    ComponentConfig config = new ComponentConfig( "Content Only Backup" );
    config.includeContent = true;
    config.includeUsers = false;
    config.includeDatasources = false;
    config.includeMetastore = false;
    config.includeSchedules = false;
    config.includeUserSettings = false;
    config.includeMondrian = false;
    config.includeGeneratedContent = true; // Include generated content by default
    return config;
  }

  /**
   * Content only backup - without generated content
   */
  public static ComponentConfig contentOnlyWithoutGenerated() {
    ComponentConfig config = contentOnly();
    config.includeGeneratedContent = false; // Exclude generated content
    config.backupName = "Content Only Backup (No Generated)";
    return config;
  }

  /**
   * Security only backup - users and roles
   */
  public static ComponentConfig securityOnly() {
    ComponentConfig config = new ComponentConfig( "Security Only Backup" );
    config.includeContent = false;
    config.includeUsers = true;
    config.includeDatasources = false;
    config.includeMetastore = false;
    config.includeSchedules = false;
    config.includeUserSettings = false;
    config.includeMondrian = false;
    config.includeGeneratedContent = false; // N/A for security
    return config;
  }

  /**
   * Data source backup - datasources and metadata
   */
  public static ComponentConfig dataSource() {
    ComponentConfig config = new ComponentConfig( "Data Source Backup" );
    config.includeContent = false;
    config.includeUsers = false;
    config.includeDatasources = true;
    config.includeMetastore = true;
    config.includeSchedules = false;
    config.includeUserSettings = false;
    config.includeMondrian = true;
    config.includeGeneratedContent = false; // N/A for datasources
    return config;
  }

  /**
   * Schedules backup - job schedules and required dependencies only
   * This profile includes:
   * - Schedules and scheduled jobs
   * - Users (as owners of schedules)
   * 
   * This profile EXCLUDES:
   * - User settings (emails/groups are NOT exported)
   * - Repository content (files/folders are only exported if referenced by schedules)
   * 
   * Use SETTINGS or FULL_SYSTEM profile if you need to export emails and groups.
   */
  public static ComponentConfig schedules() {
    ComponentConfig config = new ComponentConfig( "Schedules Backup" );
    config.includeContent = false;
    config.includeUsers = false;
    config.includeDatasources = false;
    config.includeMetastore = false;
    config.includeSchedules = true;
    config.includeUserSettings = false;
    config.includeMondrian = false;
    config.includeGeneratedContent = false; // N/A for schedules
    return config;
  }

  /**
   * Settings backup - user settings, email configuration, and group assignments
   * This profile includes:
   * - Email addresses and email group settings
   * - Group definitions and memberships
   * - User preferences and settings
   * 
   * This is separate from USER_SETTINGS flag and is used to control export of
   * emails/groups by EmailsGroupsExportUtil in the scheduler plugin.
   */
  public static ComponentConfig settings() {
    ComponentConfig config = new ComponentConfig( "Settings Backup" );
    config.includeContent = false;
    config.includeUsers = false;
    config.includeDatasources = false;
    config.includeMetastore = false;
    config.includeSchedules = false;
    config.includeUserSettings = true;
    config.includeMondrian = false;
    config.includeGeneratedContent = false; // N/A for settings
    return config;
  }

  /**
   * Infrastructure backup - schedules and settings (deprecated, use schedules() or settings() instead)
   */
  @Deprecated
  public static ComponentConfig infrastructure() {
    ComponentConfig config = new ComponentConfig( "Infrastructure Backup" );
    config.includeContent = false;
    config.includeUsers = false;
    config.includeDatasources = false;
    config.includeMetastore = false;
    config.includeSchedules = true;
    config.includeUserSettings = true;
    config.includeMondrian = false;
    config.includeGeneratedContent = false; // N/A for infrastructure
    return config;
  }

  /**
   * Validate backup configuration
   */
  @JsonIgnore
  public boolean isValid() {
    // At least one component must be selected
    return includeContent || includeUsers || includeDatasources || includeMetastore
        || includeSchedules || includeUserSettings || includeMondrian;
  }

  /**
   * Get list of enabled components
   */
  @JsonIgnore
  public List<String> getEnabledComponents() {
    List<String> components = new ArrayList<>();

    if ( includeContent ) {
      components.add( "CONTENT" );
    }
    if ( includeUsers ) {
      components.add( "USERS_AND_ROLES" );
    }
    if ( includeDatasources ) {
      components.add( "DATASOURCES" );
    }
    if ( includeMetastore ) {
      components.add( "METASTORE" );
    }
    if ( includeSchedules ) {
      components.add( "SCHEDULES" );
    }
    if ( includeUserSettings ) {
      components.add( "USER_SETTINGS" );
    }
    if ( includeMondrian ) {
      components.add( "MONDRIAN" );
    }

    return components;
  }

  /**
   * Convert to map for easy passing to exporter
   */
  public Map<String, Boolean> toMap() {
    Map<String, Boolean> map = new HashMap<>();
    map.put( "content", includeContent );
    map.put( "users", includeUsers );
    map.put( "datasources", includeDatasources );
    map.put( "metastore", includeMetastore );
    map.put( "schedules", includeSchedules );
    map.put( "userSettings", includeUserSettings );
    map.put( "mondrian", includeMondrian );
    map.put( "generatedContent", includeGeneratedContent );
    return map;
  }

  /**
   * Get total number of selected components
   */
  @JsonIgnore
  public int getComponentCount() {
    int count = 0;
    if ( includeContent ) count++;
    if ( includeUsers ) count++;
    if ( includeDatasources ) count++;
    if ( includeMetastore ) count++;
    if ( includeSchedules ) count++;
    if ( includeUserSettings ) count++;
    if ( includeMondrian ) count++;
    return count;
  }

  /**
   * Human-readable summary
   */
  @Override
  public String toString() {
    return String.format(
        "ComponentConfig{name='%s', components=%s, enabled=%d}",
        backupName,
        getEnabledComponents(),
        getComponentCount()
    );
  }

  // Getters and Setters

  public boolean isIncludeContent() {
    return includeContent;
  }

  public void setIncludeContent( boolean includeContent ) {
    this.includeContent = includeContent;
  }

  public boolean isIncludeUsers() {
    return includeUsers;
  }

  public void setIncludeUsers( boolean includeUsers ) {
    this.includeUsers = includeUsers;
  }

  public boolean isIncludeDatasources() {
    return includeDatasources;
  }

  public void setIncludeDatasources( boolean includeDatasources ) {
    this.includeDatasources = includeDatasources;
  }

  public boolean isIncludeMetastore() {
    return includeMetastore;
  }

  public void setIncludeMetastore( boolean includeMetastore ) {
    this.includeMetastore = includeMetastore;
  }

  public boolean isIncludeSchedules() {
    return includeSchedules;
  }

  public void setIncludeSchedules( boolean includeSchedules ) {
    this.includeSchedules = includeSchedules;
  }

  public boolean isIncludeUserSettings() {
    return includeUserSettings;
  }

  public void setIncludeUserSettings( boolean includeUserSettings ) {
    this.includeUserSettings = includeUserSettings;
  }

  public boolean isIncludeMondrian() {
    return includeMondrian;
  }

  public void setIncludeMondrian( boolean includeMondrian ) {
    this.includeMondrian = includeMondrian;
  }

  public boolean isIncludeGeneratedContent() {
    return includeGeneratedContent;
  }

  public void setIncludeGeneratedContent( boolean includeGeneratedContent ) {
    this.includeGeneratedContent = includeGeneratedContent;
  }

  public String getBackupName() {
    return backupName;
  }

  public void setBackupName( String backupName ) {
    this.backupName = backupName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public long getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp( long createdTimestamp ) {
    this.createdTimestamp = createdTimestamp;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion( int version ) {
    this.version = version;
  }
}
