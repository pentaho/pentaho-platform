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


package org.pentaho.platform.web.http.api.resources.utils;


import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.xsl.messages.Messages;

/**
 * This class registers and provides Pentaho Saxon extension functions used by XSL stylesheets (namely Xaction
 * parameter forms).
 */
public class XactionSaxonExtensions {

  private static final String CURRENT_MESSAGE_NAMESPACE = "org.pentaho.platform.web.xsl.messages.Messages";
  private static final String LEGACY_MESSAGE_NAMESPACE = "org.pentaho.platform.plugin.action.messages.Messages";

  /**
   * <b>msg:getInstance()</b><br>
   * Namespaces: org.pentaho.platform.web.xsl.messages.Messages and
   * org.pentaho.platform.plugin.action.messages.Messages<br>
   * XSL usage: <code>&lt;xsl:variable name="messages" select="msg:getInstance()" /&gt;</code>
   */
  public static class MsgGetInstance extends ExtensionFunctionDefinition {

    private final String namespace;

    public MsgGetInstance() {
      this( CURRENT_MESSAGE_NAMESPACE );
    }

    public MsgGetInstance( String namespace ) {
      this.namespace = namespace;
    }

    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", namespace, "getInstance" );
    }

    @Override public SequenceType[] getArgumentTypes() {
      return new SequenceType[ 0 ];
    }

    @Override public SequenceType getResultType( SequenceType[] suppliedArgumentTypes ) {
      return SequenceType.SINGLE_STRING;
    }

    @Override public ExtensionFunctionCall makeCallExpression() {
      return new ExtensionFunctionCall() {
        @Override public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException {
          // Sentinel value — the actual Messages instance is resolved inside getXslString below
          return StringValue.makeStringValue( "__MSG_INSTANCE__" );
        }
      };
    }
  }

  /**
   * <b>msg:getXslString(messages, key)</b><br>
    * Namespaces: org.pentaho.platform.web.xsl.messages.Messages and
    * org.pentaho.platform.plugin.action.messages.Messages<br>
   * XSL usage: <code>msg:getXslString($messages, 'UI.SOME_KEY')</code>
   */
  public static class MsgGetXslString extends ExtensionFunctionDefinition {

    private final String namespace;

    public MsgGetXslString() {
      this( CURRENT_MESSAGE_NAMESPACE );
    }

    public MsgGetXslString( String namespace ) {
      this.namespace = namespace;
    }

    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", namespace, "getXslString" );
    }

    @Override public SequenceType[] getArgumentTypes() {
      return new SequenceType[] { SequenceType.SINGLE_STRING, // messages instance (sentinel)
        SequenceType.SINGLE_STRING  // key
      };
    }

    @Override public SequenceType getResultType( SequenceType[] suppliedArgumentTypes ) {
      return SequenceType.SINGLE_STRING;
    }

    @Override public ExtensionFunctionCall makeCallExpression() {
      return new ExtensionFunctionCall() {
        @Override public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException {
          String key = arguments[ 1 ].head().getStringValue();
          Messages messages = Messages.getInstance();
          return StringValue.makeStringValue( messages.getXslString( key ) );
        }
      };
    }
  }

  /**
   * <b>msg:getString(messages, key)</b><br>
    * Namespaces: org.pentaho.platform.web.xsl.messages.Messages and
    * org.pentaho.platform.plugin.action.messages.Messages<br>
   * XSL usage: <code>msg:getString($messages, 'UI.SOME_KEY')</code>
   */
  public static class MsgGetString extends ExtensionFunctionDefinition {

    private final String namespace;

    public MsgGetString() {
      this( CURRENT_MESSAGE_NAMESPACE );
    }

    public MsgGetString( String namespace ) {
      this.namespace = namespace;
    }

    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", namespace, "getString" );
    }

    @Override public SequenceType[] getArgumentTypes() {
      return new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_STRING };
    }

    @Override public SequenceType getResultType( SequenceType[] suppliedArgumentTypes ) {
      return SequenceType.SINGLE_STRING;
    }

    @Override public ExtensionFunctionCall makeCallExpression() {
      return new ExtensionFunctionCall() {
        @Override public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException {
          String key = arguments[ 1 ].head().getStringValue();
          return StringValue.makeStringValue( Messages.getInstance().getString( key ) );
        }
      };
    }
  }

  /**
   * <b>loc:getTextDirection()</b><br>
   * Namespace: org.pentaho.platform.util.messages.LocaleHelper<br>
   * XSL usage: <code>&lt;xsl:value-of select="loc:getTextDirection()"/&gt;</code>
   */
  public static class LocGetTextDirection extends ExtensionFunctionDefinition {

    private static final String NAMESPACE = "org.pentaho.platform.util.messages.LocaleHelper";

    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "loc", NAMESPACE, "getTextDirection" );
    }

    @Override public SequenceType[] getArgumentTypes() {
      return new SequenceType[ 0 ];
    }

    @Override public SequenceType getResultType( SequenceType[] suppliedArgumentTypes ) {
      return SequenceType.SINGLE_STRING;
    }

    @Override public ExtensionFunctionCall makeCallExpression() {
      return new ExtensionFunctionCall() {
        @Override public Sequence call( XPathContext context, Sequence[] arguments ) throws XPathException {
          return StringValue.makeStringValue( LocaleHelper.getTextDirection() );
        }
      };
    }
  }

  /**
   * Register all extensions into a Saxon Configuration.
   */
  public static void registerAll( Configuration config ) {
    if ( config != null ) {
      config.registerExtensionFunction( new MsgGetInstance() );
      config.registerExtensionFunction( new MsgGetXslString() );
      config.registerExtensionFunction( new MsgGetString() );
      config.registerExtensionFunction( new MsgGetInstance( LEGACY_MESSAGE_NAMESPACE ) );
      config.registerExtensionFunction( new MsgGetXslString( LEGACY_MESSAGE_NAMESPACE ) );
      config.registerExtensionFunction( new MsgGetString( LEGACY_MESSAGE_NAMESPACE ) );
      config.registerExtensionFunction( new LocGetTextDirection() );
    }
  }
}
