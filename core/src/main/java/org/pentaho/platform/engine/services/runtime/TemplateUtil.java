/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IParameterManager;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.DateMath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtil {

  private static final String PARAMETER_PATTERN = "\\{([^\\}\\{$^]*)\\}"; //$NON-NLS-1$

  private static final String DATE_EXPR_PATTERN = "([\\+\\-\\s]?(\\d)+:[YMWDhms][ES]?[\\s]?)+((\\s)?;.*)?"; //$NON-NLS-1$

  //  private final static String DATE_EXPR_PATTERN2 = "(DATEMATH\\(['\"])?([\\+\\-]?\\d:[YMWDhms][MS]?[\\s]?)+((\\s)?;.*)?.*"; //$NON-NLS-1$

  private static final String DATEMATH_EXPR_PATTERN = "DATEMATH\\((\\s*)['\"].*['\"](\\s)*\\)"; //$NON-NLS-1$
  private static final String DATEMATH_VAR_PATTERN = "DATEMATH:.*"; //$NON-NLS-1$

  private static final String DATE_PATTERN = "\\d\\d\\d\\d-\\d\\d-\\d\\d"; //$NON-NLS-1$

  private static final Pattern parameterExpressionPattern = Pattern.compile( TemplateUtil.PARAMETER_PATTERN );

  private static final Pattern dateExpressionPattern = Pattern.compile( TemplateUtil.DATE_EXPR_PATTERN );

  private static final Pattern dateMathExpressionPattern = Pattern.compile( TemplateUtil.DATEMATH_EXPR_PATTERN );

  private static final Pattern dateMathVarPattern = Pattern.compile( TemplateUtil.DATEMATH_VAR_PATTERN );

  private static final Pattern datePattern = Pattern.compile( TemplateUtil.DATE_PATTERN );

  private static final List<String> SystemInputs = new ArrayList<String>();

  private static final Log logger = LogFactory.getLog( TemplateUtil.class );

  static {
    TemplateUtil.SystemInputs.add( "$user" ); //$NON-NLS-1$
    TemplateUtil.SystemInputs.add( "$url" ); //$NON-NLS-1$
    TemplateUtil.SystemInputs.add( "$solution" ); //$NON-NLS-1$
  }

  public static String getSystemInput( final String inputName, final IRuntimeContext context ) {
    int i = TemplateUtil.SystemInputs.indexOf( inputName );
    switch ( i ) {
      case 0: { // User
        return context.getSession().getName();
      }
      case 1: { // Relative URL
        return PentahoRequestContextHolder.getRequestContext().getContextPath();
      }
      case 2: { // Solution
        return PentahoSystem.getApplicationContext().getSolutionPath( "" ); //$NON-NLS-1$
      }
      case 3: { // Fully Qualified Server URL
        return PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
      }
    }
    return null;
  }

  public static String applyTemplate( final String template, final IRuntimeContext context,
      final IParameterResolver resolver ) {
    return TemplateUtil.applyTemplate( template, new InputProperties( context ), resolver );
  }

  public static String applyTemplate( final String template, final IRuntimeContext context ) {
    return TemplateUtil.applyTemplate( template, new InputProperties( context ), null );
  }

  public static String applyTemplate( final String template, final IRuntimeContext context,
      final String parameterPatternStr ) {
    Pattern pattern = Pattern.compile( parameterPatternStr );
    return TemplateUtil.applyTemplate( template, new InputProperties( context ), pattern, null );
  }

  public static String
  applyTemplate( final String template, final Properties inputs, final IParameterResolver resolver ) {
    return TemplateUtil.applyTemplate( template, inputs, TemplateUtil.parameterExpressionPattern, resolver );
  }

  /**
   * Processes a template by processing the parameters declared in the template. The parameters to be replaced are
   * enclosed in curly brackets. Parameters can be the input values (as specified by the name of the input value)
   * or date expressions. Parameters that can not be processed are left in the template.
   * 
   * @param template
   *          the template specification.
   * @param input
   *          the input values communicated as a {@link java.util.Properties}.
   * @param locale
   *          the locale to use for the formatting of date expression. If <tt>null</tt>, the locale for the thread
   *          is used. If no locale for the thread, then the default locale is used.
   * @throws IllegalArgumentException
   *           if a date expression is illegal
   * @see DateMath#calculateDateString(Calendar, String)
   * @see PentahoSystem#getLocale()
   * @see PentahoSystem#getDefaultLocale()
   */
  public static String applyTemplate( final String template, final Properties inputs, final Pattern parameterPattern,
      final IParameterResolver resolver ) {
    StringBuffer results = new StringBuffer();
    Matcher parameterMatcher = parameterPattern.matcher( template );
    int copyStart = 0;

    while ( parameterMatcher.find() ) {
      int start = parameterMatcher.start();
      String parameter = parameterMatcher.group( 1 );
      String value = null;
      int colonPosition = parameter.indexOf( ':' );
      boolean hasSpaces = parameter.indexOf( ' ' ) != -1;
      boolean isTableTemplate = !hasSpaces && parameter.indexOf( ":col:" ) != -1; //$NON-NLS-1$
      boolean isComponentResoved = !hasSpaces && colonPosition != -1;
      boolean isNamedParameter = !hasSpaces;
      boolean isDateParamter = hasSpaces;
      if ( isTableTemplate ) {
        TemplateUtil.applyTableTemplate( template, inputs, parameterPattern, results );
        return results.toString();
      }
      if ( isComponentResoved ) {
        // Allow alternate parameter resolution to be provided by the
        // component.
        if ( resolver != null ) {
          int newCopyStart = resolver.resolveParameter( template, parameter, parameterMatcher, copyStart, results );
          if ( newCopyStart >= 0 ) {
            copyStart = newCopyStart;
            continue;
          }
        }
        StringTokenizer tokenizer = new StringTokenizer( parameter, ":" ); //$NON-NLS-1$
        if ( tokenizer.countTokens() >= 5 ) {
          // this looks like a data table key
          parameter = tokenizer.nextToken();
          String keyColumn = tokenizer.nextToken();
          String keyValue = tokenizer.nextToken();
          String valueColumn = tokenizer.nextToken();
          StringBuffer defaultValue = new StringBuffer();
          defaultValue.append( tokenizer.nextToken() );
          while ( tokenizer.hasMoreTokens() ) {
            defaultValue.append( ':' ).append( tokenizer.nextToken() );
          }
          // see if we can find this in the data
          if ( inputs instanceof InputProperties ) {
            value =
                ( (InputProperties) inputs ).getProperty( parameter, keyColumn, keyValue, valueColumn, defaultValue
                    .toString() );
          }
        }
      } else if ( isNamedParameter ) {
        // TODO support type conversion
        value = inputs.getProperty( parameter );
        if ( value == null ) {
          if ( TemplateUtil.logger.isDebugEnabled() ) {
            TemplateUtil.logger.debug( Messages.getInstance().getString( "TemplateUtil.NOT_FOUND", parameter ) ); //$NON-NLS-1$
          }
        }
      }

      results.append( template.substring( copyStart, start ) );
      copyStart = parameterMatcher.end();
      if ( isDateParamter || value == null ) {
        value = TemplateUtil.matchDateRegex( parameter, inputs );
      }

      if ( value == null ) {
        results.append( parameterMatcher.group() );
      } else {
        results.append( value );
      }
    }

    if ( copyStart < template.length() ) {
      results.append( template.substring( copyStart ) );
    }

    return results.toString();
  }

  public static void applyTableTemplate( final String template, final Properties inputs,
      final Pattern parameterPattern, final StringBuffer results ) {
    Matcher parameterMatcher = parameterPattern.matcher( template );
    ArrayList<String> partsList = new ArrayList<String>();
    ArrayList<Integer> columnsList = new ArrayList<Integer>();
    int idx = 0;
    int lastEnd = 0;
    IPentahoResultSet data = null;
    while ( parameterMatcher.find() ) {
      int start = parameterMatcher.start();
      String parameter = parameterMatcher.group( 1 );
      // pull out the repeating part
      int pos1 = parameter.indexOf( ":col:" ); //$NON-NLS-1$ 
      if ( pos1 > -1 ) {
        String part = template.substring( lastEnd, start );
        if ( PentahoSystem.debug ) {
          TemplateUtil.logger.debug( "parameter=" + parameter ); //$NON-NLS-1$ 
          TemplateUtil.logger.debug( "part=" + part ); //$NON-NLS-1$ 
        }
        String inputName = parameter.substring( 0, pos1 );
        String columnNoStr = parameter.substring( pos1 + 5 );
        int columnNo = Integer.parseInt( columnNoStr );
        if ( PentahoSystem.debug ) {
          TemplateUtil.logger.debug( "inputName=" + inputName ); //$NON-NLS-1$ 
          TemplateUtil.logger.debug( "columnNoStr=" + columnNoStr ); //$NON-NLS-1$ 
          TemplateUtil.logger.debug( "columnNo=" + columnNo ); //$NON-NLS-1$ 
        }
        Object obj = null;
        if ( inputs instanceof InputProperties ) {
          obj = ( (InputProperties) inputs ).getInput( inputName );
        }
        if ( obj == null ) {
          if ( TemplateUtil.logger.isDebugEnabled() ) {
            TemplateUtil.logger.debug( Messages.getInstance().getString( "TemplateUtil.NOT_FOUND", inputName ) ); //$NON-NLS-1$
          }
        } else {
          if ( obj instanceof IPentahoResultSet ) {
            data = (IPentahoResultSet) obj;
            if ( columnNo < data.getColumnCount() ) {
              columnsList.add( new Integer( columnNo ) );
            } else {
              TemplateUtil.logger.warn( Messages.getInstance().getString(
                  "TemplateUtil.INVALID_COLUMN", String.valueOf( columnNo ) ) ); //$NON-NLS-1$
            }
          }
        }
        partsList.add( part );
        lastEnd = parameterMatcher.end();
      }
    }
    if ( PentahoSystem.debug ) {
      TemplateUtil.logger.debug( "partsList.size()=" + partsList.size() ); //$NON-NLS-1$ 
    }
    if ( PentahoSystem.debug ) {
      TemplateUtil.logger.debug( "columnsList.size()=" + columnsList.size() ); //$NON-NLS-1$ 
    }
    if ( PentahoSystem.debug ) {
      TemplateUtil.logger.debug( "data=" + data ); //$NON-NLS-1$ 
    }
    if ( partsList.size() > 0 ) {
      partsList.add( template.substring( lastEnd ) );
    } else {
      TemplateUtil.logger.warn( Messages.getInstance().getString( "TemplateUtil.NO_TOKEN" ) ); //$NON-NLS-1$
    }

    if ( ( data != null ) && ( partsList.size() == columnsList.size() + 1 ) ) {
      // here we go
      String[] parts = new String[partsList.size()];
      partsList.toArray( parts );
      Integer[] cols = new Integer[columnsList.size()];
      columnsList.toArray( cols );
      int rowNo = 0;
      Object[] row = data.getDataRow( rowNo );
      while ( row != null ) {
        for ( idx = 0; idx < cols.length; idx++ ) {
          results.append( parts[idx] );
          results.append( row[cols[idx].intValue()] );
        }
        results.append( parts[parts.length - 1] );
        rowNo++;
        row = data.getDataRow( rowNo );
      }
    }
    if ( PentahoSystem.debug ) {
      TemplateUtil.logger.debug( "results=" + results.toString() ); //$NON-NLS-1$ 
    }

  }

  public static String applyTemplate( final String template, final String name, final String value ) {

    String result = template;
    result = result.replaceAll( "\\{" + name + "\\}", value ); //$NON-NLS-1$//$NON-NLS-2$
    return result;

  }

  public static String applyTemplate( final String template, final String name, final String[] value ) {
    if ( value == null ) {
      return ( template );
    }

    if ( value.length == 1 ) {
      return ( TemplateUtil.applyTemplate( template, name, value[0] ) );
    }

    int pos = template.indexOf( "{" + name + "}" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( pos == -1 ) {
      return ( template );
    }

    int startPos = template.substring( 0, pos ).lastIndexOf( '&' );
    if ( startPos < 0 ) {
      startPos = template.substring( 0, pos ).lastIndexOf( '?' );
    }
    if ( startPos < 0 ) {
      startPos = 0;
    } else {
      startPos += 1;
    }

    int endPos = template.substring( pos + name.length() + 1 ).indexOf( '&' );
    if ( endPos < 0 ) {
      endPos = template.substring( pos + name.length() + 1 ).indexOf( '#' );
    }
    if ( endPos < 0 ) {
      endPos = template.length();
    } else {
      endPos += pos + name.length() + 1;
    }

    String result = template.substring( 0, startPos );
    String replacePart = template.substring( startPos, endPos );

    result += replacePart.replaceAll( "\\{" + name + "\\}", value[0] ); //$NON-NLS-1$ //$NON-NLS-2$
    for ( int i = 1; i < value.length; ++i ) {
      result += "&" + replacePart.replaceAll( "\\{" + name + "\\}", value[i] ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

    result += template.substring( endPos );
    return result;

  }

  /**
   * Uses regex matching to see if the input parameter appears to be a date expression
   * 
   * @param parameter
   * @return the value of the calculated date
   */
  public static String matchDateRegex( String parameter ) {
    return matchDateRegex( parameter, null );
  }

  /**
   * Uses regex matching to see if the input parameter appears to be a date expression
   * 
   * @param parameter
   * @return the value of the calculated date
   */
  public static String matchDateRegex( String parameter, final Properties inputs ) {

    // try a 'expression' pattern
    Matcher dateMatcher = TemplateUtil.dateExpressionPattern.matcher( parameter );
    String value = null;
    if ( dateMatcher.matches() ) {
      if ( parameter.indexOf( ';' ) != -1 ) {
        value = DateMath.calculateDateString( null, parameter );
      } else {
        // default to yyyy-MM-dd format for date strings
        value = DateMath.calculateDateString( null, parameter + ";yyyy-MM-dd" ); //$NON-NLS-1$
      }
    }
    if ( value == null ) {
      // try a 'DATEMATH("expression")' pattern
      dateMatcher = TemplateUtil.dateMathExpressionPattern.matcher( parameter );

      if ( dateMatcher.matches() ) {
        // remove the 'DATEMATH' part, look for the first single or double quote
        int pos = parameter.indexOf( '\'' );
        if ( pos == -1 ) {
          pos = parameter.indexOf( '"' );
        }
        if ( pos != -1 ) {
          parameter = parameter.substring( pos + 1 );
          // now look for the last one
          pos = parameter.lastIndexOf( '\'' );
          if ( pos == -1 ) {
            pos = parameter.lastIndexOf( '"' );
          }
          if ( pos != -1 ) {
            parameter = parameter.substring( 0, pos );
            value = TemplateUtil.matchDateRegex( parameter, inputs );
          }
        }
      }
    }
    if ( value == null ) {
      // try a DATEMATH:varname
      dateMatcher = TemplateUtil.dateMathVarPattern.matcher( parameter );
      if ( dateMatcher.matches() ) {
        int pos = parameter.indexOf( ':' );
        parameter = parameter.substring( pos + 1 );
        parameter = inputs.getProperty( parameter );
        if ( parameter != null ) {
          value = TemplateUtil.matchDateRegex( parameter, inputs );
        }
      }

    }
    if ( value == null ) {
      // try a date in yyyy-MM-dd format
      dateMatcher = TemplateUtil.datePattern.matcher( parameter );
      if ( dateMatcher.matches() ) {
        value = parameter;
      }
    }
    return value;

  }

  /**
   * Acts as a facade for a {@link IRuntimeContext} to access the input values as from a
   * {@link java.util.Properties Properties}. The class only overrides the {@link #getProperty(String)} method, as
   * its is the only method used in {@link TemplateComponent#applyTemplate(String, IRuntimeContext)
   * TemplateComponent.applyTemplate(String, IRuntimeContext)}.
   */
  private static class InputProperties extends Properties {
    private static final long serialVersionUID = 1L;

    private IRuntimeContext context;

    private Set<String> inputs;

    private static final Log inputPropertiesLogger = LogFactory.getLog( InputProperties.class );

    @SuppressWarnings( { "unchecked" } )
    InputProperties( final IRuntimeContext context ) {
      this.context = context;
      inputs = new HashSet<String>();
      inputs.addAll( context.getInputNames() );
      inputs.addAll( context.getParameterManager().getCurrentInputNames() );
      inputs.add( "$user" ); //$NON-NLS-1$
      inputs.add( "$url" ); //$NON-NLS-1$
      inputs.add( "$solution" ); //$NON-NLS-1$
    }

    @Override
    public int size() {
      if ( inputs == null ) {
        return 0;
      } else {
        return inputs.size();
      }
    }

    public String getProperty( final String parameter, final String keyColumn, String keyValue,
        final String valueColumn, final String defaultValue ) {
      if ( !context.getInputNames().contains( parameter ) ) {
        // leave the text alone
        return "{" + parameter + ":" + keyColumn + ":" + keyValue + ":" + valueColumn + ":" + defaultValue + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
      }
      Object valueObj = context.getInputParameterValue( parameter );
      if ( valueObj instanceof IPentahoResultSet ) {
        IPentahoResultSet data = (IPentahoResultSet) valueObj;
        // this is slow
        // TODO implement mapping or sorting here to improve performance
        int keyColumnNo = data.getMetaData().getColumnIndex( keyColumn );
        if ( keyValue.indexOf( '_' ) > 0 ) {
          keyValue = keyValue.replace( '_', ' ' );
        }
        int valueColumnNo = data.getMetaData().getColumnIndex( valueColumn );
        if ( ( keyColumnNo != -1 ) && ( valueColumnNo != -1 ) ) {
          for ( int row = 0; row < data.getRowCount(); row++ ) {
            Object thisKey = data.getValueAt( row, keyColumnNo );
            if ( thisKey != null ) {
              if ( keyValue.equals( thisKey.toString() ) ) {
                // we found the value
                // TODO support typing here
                return data.getValueAt( row, valueColumnNo ).toString();
              }
            }
          }
        }
      }
      return defaultValue;
    }

    public Object getInput( final String name ) {
      Object value = null;
      if ( inputs == null ) {
        return null;
      }
      if ( inputs.contains( name ) ) {
        value = TemplateUtil.getSystemInput( name, context );
        if ( value != null ) {
          return value;
        }
        value = context.getInputParameterValue( name );
      }
      return value;
    }

    @Override
    public String getProperty( final String name ) {
      String value = null;
      if ( inputs == null ) {
        return null;
      }
      if ( inputs.contains( name ) ) {
        value = TemplateUtil.getSystemInput( name, context );
        if ( value != null ) {
          return value;
        }
        IParameterManager paramMgr = context.getParameterManager();
        Map allParams = paramMgr.getAllParameters();
        Object valueObj;
        if ( allParams.containsKey( name ) ) {
          IActionParameter param = (IActionParameter) allParams.get( name );
          valueObj = param.getValue();
        } else {
          valueObj = context.getInputParameterValue( name );
        }
        if ( valueObj instanceof String ) {
          value = (String) valueObj;
        } else if ( valueObj instanceof Object[] ) {
          Object[] values = (Object[]) valueObj;
          StringBuffer valuesBuffer = new StringBuffer();
          // TODO support non-string items
          // TODO this is assuming that the surrounding 's exist
          for ( int i = 0; i < values.length; i++ ) {
            if ( i == 0 ) {
              valuesBuffer.append( "'" ).append( values[i].toString() ).append( "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              valuesBuffer.append( ",'" ).append( values[i].toString() ).append( "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
          String valueStr = valuesBuffer.toString();
          value = valueStr.substring( 1, valueStr.length() - 1 );
        } else if ( valueObj instanceof IPentahoResultSet ) {
          IPentahoResultSet rs = (IPentahoResultSet) valueObj;
          // See if we can find a column in the metadata with the same
          // name as the input
          IPentahoMetaData md = rs.getMetaData();
          int columnIdx = -1;
          if ( md.getColumnCount() == 1 ) {
            columnIdx = 0;
          } else {
            columnIdx = md.getColumnIndex( new String[] { name } );
          }
          if ( columnIdx < 0 ) {
            InputProperties.inputPropertiesLogger.error( Messages.getInstance().getErrorString(
                "Template.ERROR_0005_COULD_NOT_DETERMINE_COLUMN" ) ); //$NON-NLS-1$
            return null;
          }
          int rowCount = rs.getRowCount();
          Object valueCell = null;
          StringBuffer valuesBuffer = new StringBuffer();
          // TODO support non-string columns
          for ( int i = 0; i < rowCount; i++ ) {
            valueCell = rs.getValueAt( i, columnIdx );
            if ( i == 0 ) {
              valuesBuffer.append( "'" ).append( valueCell.toString() ).append( "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              valuesBuffer.append( ",'" ).append( valueCell.toString() ).append( "'" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
          String valueStr = valuesBuffer.toString();
          // TODO Assumes that parameter is already surrounded by
          // quotes.
          value = valueStr.substring( 1, valueStr.length() - 1 );
        } else if ( valueObj != null ) {
          value = valueObj.toString();
        }
        // TODO add support for numeric classes
      } else {
        value = super.getProperty( name );
      }
      if ( value == null ) {
        value = TemplateUtil.matchDateRegex( name, null );
      } else {
        String tempValue = TemplateUtil.matchDateRegex( value, null );
        if ( tempValue != null ) {
          value = tempValue;
        }
      }

      return value;
    }
  }

  public static Properties parametersToProperties( final IParameterProvider parameterProvider ) {
    Properties properties = new Properties();
    Iterator names = parameterProvider.getParameterNames();
    while ( names.hasNext() ) {
      String name = (String) names.next();
      String value = parameterProvider.getStringParameter( name, null );
      if ( value != null ) {
        properties.put( name, value );
      }
    }
    return properties;
  }

}
