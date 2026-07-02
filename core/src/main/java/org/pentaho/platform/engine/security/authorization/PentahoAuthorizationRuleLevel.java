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

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.authorization.core.rules.AbstractAuthorizationRule;
import org.pentaho.platform.engine.security.authorization.core.rules.AbstractCompositeAuthorizationRule;
import org.pentaho.platform.engine.security.authorization.core.rules.AllAuthorizationRule;
import org.pentaho.platform.engine.security.authorization.core.rules.AnyAuthorizationRule;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PentahoAuthorizationRuleLevel extends AbstractAuthorizationRule<IAuthorizationRequest> {
  private static final String RULE_LEVEL_ATTRIBUTE = "ruleLevel";

  public enum RuleLevelType {
    ALL, ANY
  }

  @NonNull
  private final RuleLevelType ruleLevelType;

  @NonNull
  private final Predicate<IPentahoObjectReference<?>> levelRulePredicate;

  @NonNull
  private final List<IAuthorizationRule<IAuthorizationRequest>> postRules;

  // There's no way to solve the raw type issue, given these are triple nested generics...
  // This property/parameter allows independence from PentahoSystem.getObjectReferences,
  // and passing a supplier which is mocked in tests. Unfortunately, this reveals the raw type
  // that could otherwise be hidden by use of `var` for example.
  @SuppressWarnings( "rawtypes" )
  @NonNull
  private final Supplier<List<IPentahoObjectReference<IAuthorizationRule>>> authorizationRuleReferencesSupplier;

  @NonNull
  private AbstractCompositeAuthorizationRule delegateRule;

  public PentahoAuthorizationRuleLevel( @NonNull IPluginManager pluginManager,
                                        @NonNull RuleLevelType ruleLevelType,
                                        @Nullable String ruleLevel,
                                        @NonNull List<IAuthorizationRule<IAuthorizationRequest>> postRules ) {
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
                                        @NonNull List<IAuthorizationRule<IAuthorizationRequest>> postRules ) {

    this(
      pluginManager,
      ruleLevelType,
      buildLevelRulePredicate( ruleLevel, isDefaultRuleLevel ),
      postRules,
      getPentahoSystemAuthorizationRuleReferencesSupplier() );
  }

  protected PentahoAuthorizationRuleLevel(
    @NonNull IPluginManager pluginManager,
    @NonNull RuleLevelType ruleLevelType,
    @NonNull Predicate<IPentahoObjectReference<?>> levelRulePredicate,
    @NonNull List<IAuthorizationRule<IAuthorizationRequest>> postRules,

    @SuppressWarnings( "rawtypes" )
    @NonNull
    Supplier<List<IPentahoObjectReference<IAuthorizationRule>>> authorizationRuleReferencesSupplier ) {

    Assert.notNull( ruleLevelType, "Argument 'ruleLevelType' is required" );
    Assert.notNull( levelRulePredicate, "Argument 'levelRulePredicate' is required" );
    Assert.notNull( authorizationRuleReferencesSupplier, "Argument 'authorizationRuleReferencesSupplier' is required" );

    this.ruleLevelType = ruleLevelType;
    this.levelRulePredicate = levelRulePredicate;
    this.postRules = List.copyOf( postRules );
    this.authorizationRuleReferencesSupplier = authorizationRuleReferencesSupplier;

    this.updateDelegateRule();

    pluginManager.addPluginManagerListener( this::updateDelegateRule );
  }

  private void updateDelegateRule() {
    this.delegateRule = buildDelegateRule();
  }

  @VisibleForTesting
  @NonNull
  protected AbstractCompositeAuthorizationRule getDelegateRule() {
    return this.delegateRule;
  }

  @NonNull
  private AbstractCompositeAuthorizationRule buildDelegateRule() {
    var rules = getLevelAuthorizationRules( levelRulePredicate );
    rules.addAll( postRules );

    return ruleLevelType.equals( RuleLevelType.ALL )
      ? new AllAuthorizationRule( rules )
      : new AnyAuthorizationRule( rules );
  }

  @NonNull
  @Override
  public Class<IAuthorizationRequest> getRequestType() {
    return IAuthorizationRequest.class;
  }

  @NonNull
  @Override
  public Optional<IAuthorizationDecision> authorize( @NonNull IAuthorizationRequest request,
                                                     @NonNull IAuthorizationContext context ) {
    return getDelegateRule().authorize( request, context );
  }

  @NonNull
  private ArrayList<IAuthorizationRule<IAuthorizationRequest>> getLevelAuthorizationRules(
    @NonNull Predicate<IPentahoObjectReference<?>> rulePredicate ) {

    var objectReferences = authorizationRuleReferencesSupplier.get();

    return objectReferences
      .stream()
      .filter( rulePredicate )
      .map( ref -> {
        // Cast the raw generic rule type from IPentahoObjectReference<IAuthorizationRule>.
        @SuppressWarnings( "unchecked" )
        IAuthorizationRule<IAuthorizationRequest> rule =
          (IAuthorizationRule<IAuthorizationRequest>) ref.getObject();
        return rule;
      } )
      .collect( Collectors.toCollection( ArrayList::new ) );
  }

  @VisibleForTesting
  @NonNull
  protected static Predicate<IPentahoObjectReference<?>> buildLevelRulePredicate(
    @Nullable String ruleLevel,
    boolean isDefaultRuleLevel ) {

    // normalize null to ""
    final String normalizedRuleLevel = getDefaultValue( ruleLevel, "" );

    return new Predicate<>() {
      @Override
      public boolean test( IPentahoObjectReference<?> ruleRef ) {
        String value = getDefaultValue(
          ruleRef.getAttributes().get( RULE_LEVEL_ATTRIBUTE ),
          isDefaultRuleLevel ? normalizedRuleLevel : "" );

        return value.equals( normalizedRuleLevel );
      }

      @Override
      public String toString() {
        return "Rules[level=" + normalizedRuleLevel + " default=" + isDefaultRuleLevel + "]";
      }
    };
  }

  @NonNull
  private static String getDefaultValue( @Nullable Object value, @NonNull String defaultValue ) {
    // normalize null or "" to default value
    return value == null || "".equals( value ) ? defaultValue : value.toString();
  }

  @SuppressWarnings( "rawtypes" )
  @NonNull
  private static Supplier<List<IPentahoObjectReference<IAuthorizationRule>>> getPentahoSystemAuthorizationRuleReferencesSupplier() {
    return () -> PentahoSystem.getObjectReferences( IAuthorizationRule.class, null );
  }

  @Override
  public String toString() {
    return String.format(
      "PentahoAuthorizationRuleLevel[%s, %s, count=%s]",
      ruleLevelType,
      levelRulePredicate,
      getDelegateRule().getRules().size() );
  }
}
