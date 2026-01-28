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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CmdParser extends Parser {

  private List<Object> tokens = new ArrayList<>();
  private boolean eatTheRest;
  private Option currentOption;
  private Options options;

  @Override
  protected String[] flatten( Options options, String[] arguments, boolean stopAtNonOption ) {
    init();
    this.options = options;

    // an iterator for the command line tokens
    Iterator iter = Arrays.asList( arguments ).iterator();

    // process each command line token
    while ( iter.hasNext() ) {
      // get the next command line token
      String token = (String) iter.next();

      // handle long option --foo or --foo=bar
      if ( token.startsWith( "--" ) ) {
        handleToken( options, stopAtNonOption, token );
      } else if ( "-".equals( token ) ) { // single hyphen
        tokens.add( token );
      } else if ( token.startsWith( "-" ) ) {
        if ( token.length() == 2 || options.hasOption( token ) ) {
          processOptionToken( token, stopAtNonOption );
        } else {
          handleToken( options, stopAtNonOption, token );
        }
      } else {
        processNonOptionToken( token, stopAtNonOption );
      }

      gobble( iter );
    }
    return (String[]) tokens.toArray( new String[ tokens.size() ] );
  }

  private void handleToken( Options options, boolean stopAtNonOption, String token ) {
    int pos = token.indexOf( '=' );
    String opt = pos == -1 ? token : token.substring( 0, pos ); // --foo

    if ( !options.hasOption( opt ) ) {
      processNonOptionToken( token, stopAtNonOption );
    } else {
      Option option = options.getOption( opt );
      boolean isLongOptAndExists = opt.startsWith( "--" ) && option.getLongOpt().equals( opt.substring( 2, opt.length() ) );
      boolean isShortOptAndExists = opt.startsWith( "-" ) && option.getOpt().equals( opt.substring( 1, opt.length() ) );
      if ( isLongOptAndExists || isShortOptAndExists ) {
        currentOption = option;
        tokens.add( opt );
        if ( pos != -1 ) {
          tokens.add( token.substring( pos + 1 ) );
        }
      } else {
        throw new IllegalArgumentException( Messages.getInstance().getErrorString(
          "CommandLineProcessor.ERROR_0008_INVALID_PARAMETER", opt ) );
      }
    }
  }

  private void init() {
    eatTheRest = false;
    tokens.clear();
  }

  private void processNonOptionToken( String value, boolean stopAtNonOption ) {
    if ( stopAtNonOption && ( currentOption == null || !currentOption.hasArg() ) ) {
      eatTheRest = true;
      tokens.add( "--" );
    }
    tokens.add( value );
  }

  private void processOptionToken( String token, boolean stopAtNonOption ) {
    if ( stopAtNonOption && !options.hasOption( token ) ) {
      eatTheRest = true;
    }
    if ( options.hasOption( token ) ) {
      currentOption = options.getOption( token );
    }
    tokens.add( token );
  }

  private void gobble( Iterator iter ) {
    if ( eatTheRest ) {
      while ( iter.hasNext() ) {
        tokens.add( iter.next() );
      }
    }
  }
}
