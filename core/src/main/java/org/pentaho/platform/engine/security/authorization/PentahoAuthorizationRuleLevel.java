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

package org.pentaho.platform.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.authorization.core.rules.AllAuthorizationRule;
import org.pentaho.platform.engine.security.authorization.core.rules.AnyAuthorizationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PentahoAuthorizationRuleLevel implements IAuthorizationRule {
  private static final String RULE_LEVEL_ATTRIBUTE = "ruleLevel";

  public enum RuleLevelType {
    ALL, ANY
  }

  @NonNull
  private final RuleLevelType ruleLevelType;

  @NonNull
  private final Predicate<IPentahoObjectReference<IAuthorizationRule>> levelRulePredicate;

  @NonNull
  private final List<IAuthorizationRule> postRules;

  @NonNull
  private IAuthorizationRule delegateRule;

  public PentahoAuthorizationRuleLevel( @NonNull IPluginManager pluginManager,
                                        @NonNull RuleLevelType ruleLevelType,
                                        @Nullable String ruleLevel,
                                        @NonNull List<IAuthorizationRule> postRules ) {
    this( pluginManager, ruleLevelType, ruleLevel, false, postRules );
  }

  public PentahoAuthorizationRuleLevel( @NonNull IPluginManager pluginManager,
                                        @NonNull RuleLevelType ruleLevelType,
                                        @Nullable String ruleLevel,
                                        boolean isDefaultRuleLevel ) {

    this( pluginManager, ruleLevelType, ruleLevel, isDefaultRuleLevel, List.of() );
  }

  public PentahoAuthorizationRuleLevel( @NonNull IPluginManager pluginManager,
                                        @NonNull RuleLevelType ruleLevelType,
                                        @Nullable String ruleLevel,
                                        boolean isDefaultRuleLevel,
                                        @NonNull List<IAuthorizationRule> postRules ) {

    this( pluginManager, ruleLevelType, buildLevelRulePredicate( ruleLevel, isDefaultRuleLevel ), postRules );
  }

  public PentahoAuthorizationRuleLevel(
    @NonNull IPluginManager pluginManager,
    @NonNull RuleLevelType ruleLevelType,
    @NonNull Predicate<IPentahoObjectReference<IAuthorizationRule>> levelRulePredicate,
    @NonNull List<IAuthorizationRule> postRules ) {

    this.ruleLevelType = Objects.requireNonNull( ruleLevelType );
    this.levelRulePredicate = Objects.requireNonNull( levelRulePredicate );
    this.postRules = Objects.requireNonNull( postRules );

    this.updateDelegateRule();

    pluginManager.addPluginManagerListener( this::updateDelegateRule );
  }

  private void updateDelegateRule() {
    this.delegateRule = buildDelegateRule();
  }

  @NonNull
  private IAuthorizationRule getDelegateRule() {
    return this.delegateRule;
  }

  @NonNull
  private IAuthorizationRule buildDelegateRule() {
    var rules = new ArrayList<>( getLevelAuthorizationRules( levelRulePredicate ) );
    rules.addAll( postRules );

    return ruleLevelType.equals( RuleLevelType.ALL )
      ? new AllAuthorizationRule( rules )
      : new AnyAuthorizationRule( rules );
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {
    return getDelegateRule().authorize( request, context );
  }

  @NonNull
  public static List<IAuthorizationRule> getLevelAuthorizationRules(
    @NonNull Predicate<IPentahoObjectReference<IAuthorizationRule>> rulePredicate ) {

    return PentahoSystem.getObjectReferences( IAuthorizationRule.class, null )
      .stream()
      .filter( rulePredicate )
      .map( IPentahoObjectReference::getObject )
      .collect( Collectors.toList() );
  }

  @NonNull
  public static Predicate<IPentahoObjectReference<IAuthorizationRule>> buildLevelRulePredicate(
    @Nullable String ruleLevel,
    boolean isDefaultRuleLevel ) {
    // normalize null to ""
    final String normalizedRuleLevel = getDefaultValue( ruleLevel, "" );

    return ruleRef -> {
      String value = getDefaultValue(
        ruleRef.getAttributes().get( RULE_LEVEL_ATTRIBUTE ),
        isDefaultRuleLevel ? normalizedRuleLevel : "" );

      return value.equals( normalizedRuleLevel );
    };
  }

  @NonNull
  private static String getDefaultValue( @Nullable Object value, @NonNull String defaultValue ) {
    // normalize null or "" to default value
    return value == null || "".equals( value ) ? defaultValue : value.toString();
  }
}
