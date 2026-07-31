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

  /**
   * <b>msg:getInstance()</b><br>
   * Namespace: org.pentaho.platform.web.xsl.messages.Messages<br>
   * XSL usage: <code>&lt;xsl:variable name="messages" select="msg:getInstance()" /&gt;</code>
   */
  public static class MsgGetInstance extends ExtensionFunctionDefinition {

    private static final String NAMESPACE = "org.pentaho.platform.web.xsl.messages.Messages";

    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", NAMESPACE, "getInstance" );
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
   * Namespace: org.pentaho.platform.web.xsl.messages.Messages<br>
   * XSL usage: <code>msg:getXslString($messages, 'UI.SOME_KEY')</code>
   */
  public static class MsgGetXslString extends ExtensionFunctionDefinition {

    private static final String NAMESPACE = "org.pentaho.platform.web.xsl.messages.Messages";

    @Override public StructuredQName getFunctionQName() {
      return new StructuredQName( "msg", NAMESPACE, "getXslString" );
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
          String key = arguments[ 1 ].head().getStringValue(); Messages messages = Messages.getInstance();
          return StringValue.makeStringValue( messages.getXslString( key ) );
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
   * Register all 3 extensions into a Saxon Configuration.
   */
  public static void registerAll( Configuration config ) {
    if ( config != null ) {
      config.registerExtensionFunction( new MsgGetInstance() );
      config.registerExtensionFunction( new MsgGetXslString() );
      config.registerExtensionFunction( new LocGetTextDirection() );
    }
  }
}
