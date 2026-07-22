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

package org.pentaho.platform.util;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

/**
 * Bogus version of XactionSaxonExtensions for unit testing.
 * <p>
 * This class provides the same 3 Saxon extension functions as XactionSaxonExtensions
 * but with no external dependencies. It returns hardcoded/dummy values suitable for
 * testing XSL transformations without requiring actual message bundles or locale helpers.
 * <p>
 * This is useful when testing in modules that don't depend on the full extensions module
 * or when the actual message bundles are not available.
 */
public class BogusXactionSaxonExtensions {
  private static final String MSG_NAMESPACE = "org.pentaho.platform.util.messages.Messages";
  private static final String LOC_NAMESPACE = "org.pentaho.platform.util.messages.LocaleHelper";

  /**
   * BOGUS VERSION for:
   * <b>msg:getInstance()</b><br>
   * Namespace: org.pentaho.platform.util.messages.Messages<br>
   * XSL usage: <code>&lt;xsl:variable name="messages" select="msg:getInstance()" /&gt;</code>
   */
  public static class MsgGetInstance extends ExtensionFunctionDefinition {
    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", MSG_NAMESPACE, "getInstance" );
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
   * BOGUS VERSION for:
   * <b>msg:getXslString(messages, key)</b><br>
   * Namespace: org.pentaho.platform.util.messages.Messages<br>
   * XSL usage: <code>msg:getXslString($messages, 'UI.SOME_KEY')</code>
   */
  public static class MsgGetXslString extends ExtensionFunctionDefinition {
    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", MSG_NAMESPACE, "getXslString" );
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
          return StringValue.makeStringValue( "[msg:getXslString(_, " + key + ")]" );
        }
      };
    }
  }

  /**
   * BOGUS VERSION for:
   * <b>msg:getString(messages, key)</b><br>
   * Namespace: org.pentaho.platform.util.messages.Messages<br>
   * XSL usage: <code>msg:getString($messages, 'UI.USER_UPDATE')</code>
   */
  public static class MsgGetString extends ExtensionFunctionDefinition {
    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", MSG_NAMESPACE, "getString" );
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
          return StringValue.makeStringValue( "[msg:getString(_, " + key + ")]" );
        }
      };
    }
  }

  /**
   * BOGUS VERSION for:
   * <b>loc:getTextDirection()</b><br>
   * Namespace: org.pentaho.platform.util.messages.LocaleHelper<br>
   * XSL usage: <code>&lt;xsl:value-of select="loc:getTextDirection()"/&gt;</code>
   */
  public static class LocGetTextDirection extends ExtensionFunctionDefinition {
    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "loc", LOC_NAMESPACE, "getTextDirection" );
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
          return StringValue.makeStringValue( "[loc:getTextDirection()]" );
        }
      };
    }
  }

  /**
   * Register all 3 bogus extension functions into a Saxon Configuration.
   *
   * @param config the Saxon Configuration to register the functions with
   */
  public static void registerAll( Configuration config ) {
    config.registerExtensionFunction( new MsgGetInstance() );
    config.registerExtensionFunction( new MsgGetXslString() );
    config.registerExtensionFunction( new MsgGetString() );
    config.registerExtensionFunction( new LocGetTextDirection() );
  }
}
