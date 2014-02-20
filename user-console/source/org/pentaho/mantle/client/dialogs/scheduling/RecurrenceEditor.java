/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.controls.DateRangeEditor;
import org.pentaho.gwt.widgets.client.controls.ErrorLabel;
import org.pentaho.gwt.widgets.client.controls.TimePicker;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.ui.IChangeHandler;
import org.pentaho.gwt.widgets.client.utils.CronParseException;
import org.pentaho.gwt.widgets.client.utils.CronParser;
import org.pentaho.gwt.widgets.client.utils.CronParser.RecurrenceType;
import org.pentaho.gwt.widgets.client.utils.EnumException;
import org.pentaho.gwt.widgets.client.utils.StringUtils;
import org.pentaho.gwt.widgets.client.utils.TimeUtil;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.DayOfWeek;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.MonthOfYear;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.TimeOfDay;
import org.pentaho.gwt.widgets.client.utils.TimeUtil.WeekOfMonth;
import org.pentaho.mantle.client.messages.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven Barkdull
 * 
 */

@SuppressWarnings( "deprecation" )
public class RecurrenceEditor extends VerticalPanel implements IChangeHandler {

  private static final String SCHEDULE_EDITOR_CAPTION_PANEL = "schedule-editor-caption-panel"; //$NON-NLS-1$
  private static final String DOW_CHECKBOX = "day-of-week-checkbox"; //$NON-NLS-1$

  private TimePicker startTimePicker = null;

  private SecondlyRecurrenceEditor secondlyEditor = null;

  private MinutelyRecurrenceEditor minutelyEditor = null;

  private HourlyRecurrenceEditor hourlyEditor = null;

  private DailyRecurrenceEditor dailyEditor = null;

  private WeeklyRecurrenceEditor weeklyEditor = null;

  private MonthlyRecurrenceEditor monthlyEditor = null;

  private YearlyRecurrenceEditor yearlyEditor = null;

  private DateRangeEditor dateRangeEditor = null;

  private TemporalValue temporalState = null;
  private DeckPanel deckPanel = null;

  private static final String SPACE = " "; //$NON-NLS-1$

  private static int VALUE_OF_SUNDAY = 1;
  private ICallback<IChangeHandler> onChangeHandler;

  private Map<TemporalValue, Panel> temporalPanelMap = new LinkedHashMap<TemporalValue, Panel>();

  public enum TemporalValue {
    SECONDS( 0, Messages.getString( "schedule.seconds" ) ),
      MINUTES( 1, Messages.getString( "schedule.minutes" ) ),
      HOURS( 2, Messages.getString( "schedule.hours" ) ),
      DAILY( 3, Messages.getString( "schedule.daily" ) ),
      WEEKLY( 4, Messages.getString( "schedule.weekly" ) ),
      MONTHLY( 5, Messages.getString( "schedule.monthly" ) ),
      YEARLY( 6, Messages.getString( "schedule.yearly" ) );

    private TemporalValue( int value, String name ) {
      this.value = value;
      this.name = name;
    }

    private final int value;

    private final String name;

    private static TemporalValue[] temporalValues = { SECONDS, MINUTES, HOURS, DAILY, WEEKLY, MONTHLY, YEARLY };

    public int value() {
      return value;
    }

    public String toString() {
      return name;
    }

    public static TemporalValue get( int idx ) {
      return temporalValues[idx];
    }

    public static int length() {
      return temporalValues.length;
    }

    public static TemporalValue stringToTemporalValue( String temporalValue ) throws EnumException {
      for ( TemporalValue v : EnumSet.range( TemporalValue.SECONDS, TemporalValue.YEARLY ) ) {
        if ( v.toString().equals( temporalValue ) ) {
          return v;
        }
      }
      throw new EnumException( Messages.getString( "schedule.invalidTemporalValue", temporalValue ) );
    }
  } /* end enum */

  private static final String DAILY_RB_GROUP = "daily-group"; //$NON-NLS-1$

  private static final String MONTHLY_RB_GROUP = "monthly-group"; //$NON-NLS-1$

  public RecurrenceEditor( final TimePicker startTimePicker ) {
    super();
    this.setWidth( "100%" ); //$NON-NLS-1$

    Widget p = createRecurrencePanel();
    add( p );

    Date now = new Date();
    dateRangeEditor = new DateRangeEditor( now );
    add( dateRangeEditor );

    this.startTimePicker = startTimePicker;

    configureOnChangeHandler();
  }

  public void reset( Date d ) {

    startTimePicker.setHour( "12" ); //$NON-NLS-1$
    startTimePicker.setMinute( "00" ); //$NON-NLS-1$
    startTimePicker.setTimeOfDay( TimeUtil.TimeOfDay.AM );

    dateRangeEditor.reset( d );

    secondlyEditor.reset();
    minutelyEditor.reset();
    hourlyEditor.reset();
    dailyEditor.reset();
    weeklyEditor.reset();
    monthlyEditor.reset();
    yearlyEditor.reset();
  }

  /**
   * 
   * @param recurrenceStr
   * @throws EnumException
   *           thrown if recurrenceTokens[0] is not a valid ScheduleType String.
   */
  public void inititalizeWithRecurrenceString( String recurrenceStr ) throws EnumException {
    String[] recurrenceTokens = recurrenceStr.split( "\\s" ); //$NON-NLS-1$

    setStartTime( recurrenceTokens[1], recurrenceTokens[2], recurrenceTokens[3] );

    RecurrenceType rt = RecurrenceType.stringToScheduleType( recurrenceTokens[0] );

    switch ( rt ) {
      case EveryWeekday:
        setEveryWeekdayRecurrence( recurrenceTokens );
        break;
      case WeeklyOn:
        setWeeklyOnRecurrence( recurrenceTokens );
        break;
      case DayNOfMonth:
        setDayNOfMonthRecurrence( recurrenceTokens );
        break;
      case NthDayNameOfMonth:
        setNthDayNameOfMonthRecurrence( recurrenceTokens );
        break;
      case LastDayNameOfMonth:
        setLastDayNameOfMonthRecurrence( recurrenceTokens );
        break;
      case EveryMonthNameN:
        setEveryMonthNameNRecurrence( recurrenceTokens );
        break;
      case NthDayNameOfMonthName:
        setNthDayNameOfMonthNameRecurrence( recurrenceTokens );
        break;
      case LastDayNameOfMonthName:
        setLastDayNameOfMonthNameRecurrence( recurrenceTokens );
        break;
      default:
    }
  }

  private void setStartTime( String seconds, String minutes, String hours ) {
    TimeOfDay td = TimeUtil.getTimeOfDayBy0To23Hour( hours );
    int intHours = Integer.parseInt( hours );
    int intTwelveHour = TimeUtil.to12HourClock( intHours ); // returns 0..11
    startTimePicker.setHour( Integer.toString( TimeUtil.map0Through11To12Through11( intTwelveHour ) ) );
    startTimePicker.setMinute( minutes );
    startTimePicker.setTimeOfDay( td );
  }

  private void setEveryWeekdayRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.DAILY );
    dailyEditor.setEveryWeekday();
  }

  private void setWeeklyOnRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.WEEKLY );
    String days = recurrenceTokens[4];
    weeklyEditor.setCheckedDaysAsString( days, VALUE_OF_SUNDAY );
  }

  private void setDayNOfMonthRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.MONTHLY );
    monthlyEditor.setDayNOfMonth();
    String dayNOfMonth = recurrenceTokens[4];
    monthlyEditor.setDayOfMonth( dayNOfMonth );
  }

  private void setNthDayNameOfMonthRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.MONTHLY );
    monthlyEditor.setNthDayNameOfMonth();
    monthlyEditor.setWeekOfMonth( WeekOfMonth.get( Integer.parseInt( recurrenceTokens[5] ) - 1 ) );
    monthlyEditor.setDayOfWeek( DayOfWeek.get( Integer.parseInt( recurrenceTokens[4] ) - 1 ) );
  }

  private void setLastDayNameOfMonthRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.MONTHLY );
    monthlyEditor.setNthDayNameOfMonth();
    monthlyEditor.setWeekOfMonth( WeekOfMonth.LAST );
    monthlyEditor.setDayOfWeek( DayOfWeek.get( Integer.parseInt( recurrenceTokens[4] ) - 1 ) );
  }

  private void setEveryMonthNameNRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.YEARLY );
    yearlyEditor.setEveryMonthOnNthDay();
    yearlyEditor.setDayOfMonth( recurrenceTokens[4] );
    yearlyEditor.setMonthOfYear0( MonthOfYear.get( Integer.parseInt( recurrenceTokens[5] ) - 1 ) );
  }

  private void setNthDayNameOfMonthNameRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.YEARLY );
    yearlyEditor.setNthDayNameOfMonthName();
    yearlyEditor.setMonthOfYear1( MonthOfYear.get( Integer.parseInt( recurrenceTokens[6] ) - 1 ) );
    yearlyEditor.setWeekOfMonth( WeekOfMonth.get( Integer.parseInt( recurrenceTokens[5] ) - 1 ) );
    yearlyEditor.setDayOfWeek( DayOfWeek.get( Integer.parseInt( recurrenceTokens[4] ) - 1 ) );
  }

  private void setLastDayNameOfMonthNameRecurrence( String[] recurrenceTokens ) {
    setTemporalState( TemporalValue.YEARLY );
    yearlyEditor.setNthDayNameOfMonthName();
    yearlyEditor.setMonthOfYear1( MonthOfYear.get( Integer.parseInt( recurrenceTokens[5] ) - 1 ) );
    yearlyEditor.setWeekOfMonth( WeekOfMonth.LAST );
    yearlyEditor.setDayOfWeek( DayOfWeek.get( Integer.parseInt( recurrenceTokens[4] ) - 1 ) );
  }

  /**
   * 
   * @param repeatInSecs
   */
  public void inititalizeWithRepeatInSecs( int repeatInSecs ) {

    TemporalValue currentVal;
    long repeatTime;
    if ( TimeUtil.isSecondsWholeDay( repeatInSecs ) ) {
      repeatTime = TimeUtil.secsToDays( repeatInSecs );
      currentVal = TemporalValue.DAILY;
      dailyEditor.setRepeatValue( Long.toString( repeatTime ) );
    } else {
      SimpleRecurrencePanel p = null;
      if ( TimeUtil.isSecondsWholeHour( repeatInSecs ) ) {
        repeatTime = TimeUtil.secsToHours( repeatInSecs );
        currentVal = TemporalValue.HOURS;
      } else if ( TimeUtil.isSecondsWholeMinute( repeatInSecs ) ) {
        repeatTime = TimeUtil.secsToMinutes( repeatInSecs );
        currentVal = TemporalValue.MINUTES;
      } else {
        // the repeat time is seconds
        repeatTime = repeatInSecs;
        currentVal = TemporalValue.SECONDS;
      }
      p = (SimpleRecurrencePanel) temporalPanelMap.get( currentVal );
      p.setValue( Long.toString( repeatTime ) );
    }
    setTemporalState( currentVal );
  }

  private Widget createRecurrencePanel() {

    CaptionPanel recurrenceGB = new CaptionPanel( Messages.getString( "schedule.recurrencePattern" ) );
    recurrenceGB.setStyleName( SCHEDULE_EDITOR_CAPTION_PANEL );

    deckPanel = new DeckPanel();
    recurrenceGB.add( deckPanel );

    secondlyEditor = new SecondlyRecurrenceEditor();
    minutelyEditor = new MinutelyRecurrenceEditor();
    hourlyEditor = new HourlyRecurrenceEditor();
    dailyEditor = new DailyRecurrenceEditor();
    weeklyEditor = new WeeklyRecurrenceEditor();
    monthlyEditor = new MonthlyRecurrenceEditor();
    yearlyEditor = new YearlyRecurrenceEditor();

    createTemporalMap();

    deckPanel.add( secondlyEditor );
    deckPanel.add( minutelyEditor );
    deckPanel.add( hourlyEditor );

    deckPanel.add( dailyEditor );
    deckPanel.add( weeklyEditor );
    deckPanel.add( monthlyEditor );
    deckPanel.add( yearlyEditor );

    deckPanel.showWidget( 0 );

    return recurrenceGB;
  }

  private void createTemporalMap() {
    // must come after creation of temporal panels
    assert dailyEditor != null : "Temporal panels must be initialized before calling createTemporalCombo."; //$NON-NLS-1$

    temporalPanelMap.put( TemporalValue.SECONDS, secondlyEditor );
    temporalPanelMap.put( TemporalValue.MINUTES, minutelyEditor );
    temporalPanelMap.put( TemporalValue.HOURS, hourlyEditor );
    temporalPanelMap.put( TemporalValue.DAILY, dailyEditor );
    temporalPanelMap.put( TemporalValue.WEEKLY, weeklyEditor );
    temporalPanelMap.put( TemporalValue.MONTHLY, monthlyEditor );
    temporalPanelMap.put( TemporalValue.YEARLY, yearlyEditor );
  }

  private class SimpleRecurrencePanel extends VerticalPanel implements IChangeHandler {
    private TextBox valueTb = new TextBox();
    private ErrorLabel valueLabel = null;
    private ICallback<IChangeHandler> onChangeHandler;

    public SimpleRecurrencePanel( String strLabel ) {

      HorizontalPanel hp = new HorizontalPanel();
      Label l = new Label( Messages.getString( "schedule.every" ) );
      l.setStyleName( "startLabel" ); //$NON-NLS-1$
      hp.add( l );

      valueTb.setWidth( "3em" ); //$NON-NLS-1$
      valueTb.setTitle( Messages.getString( "schedule.numberOfXToRepeat", strLabel ) );
      hp.add( valueTb );

      l = new Label( strLabel );
      l.setStyleName( "endLabel" ); //$NON-NLS-1$
      hp.add( l );

      valueLabel = new ErrorLabel( hp );
      add( valueLabel );

      configureOnChangeHandler();
    }

    public String getValue() {
      return valueTb.getText();
    }

    public void setValue( String val ) {
      valueTb.setText( val );
    }

    public void reset() {
      setValue( "" ); //$NON-NLS-1$
    }

    public void setValueError( String errorMsg ) {
      valueLabel.setErrorMsg( errorMsg );
    }

    public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
      this.onChangeHandler = handler;
    }

    private void changeHandler() {
      if ( null != onChangeHandler ) {
        onChangeHandler.onHandle( this );
      }
    }

    private void configureOnChangeHandler() {
      final SimpleRecurrencePanel localThis = this;

      KeyboardListener keyboardListener = new KeyboardListener() {
        public void onKeyDown( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyPress( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyUp( Widget sender, char keyCode, int modifiers ) {
          localThis.changeHandler();
        }
      };

      valueTb.addKeyboardListener( keyboardListener );
    }
  }

  public class SecondlyRecurrenceEditor extends SimpleRecurrencePanel {
    public SecondlyRecurrenceEditor() {
      super( Messages.getString( "schedule.seconds" ) );
    }
  }

  public class MinutelyRecurrenceEditor extends SimpleRecurrencePanel {
    public MinutelyRecurrenceEditor() {
      super( Messages.getString( "schedule.minuteOrMinutes" ) );
    }
  }

  public class HourlyRecurrenceEditor extends SimpleRecurrencePanel {
    public HourlyRecurrenceEditor() {
      super( Messages.getString( "schedule.hourOrHours" ) );
    }
  }

  public class DailyRecurrenceEditor extends VerticalPanel implements IChangeHandler {

    private TextBox repeatValueTb = new TextBox();
    private RadioButton everyNDaysRb = new RadioButton( DAILY_RB_GROUP, Messages.getString( "schedule.every" ) );
    private RadioButton everyWeekdayRb = new RadioButton( DAILY_RB_GROUP,
      Messages.getString( "schedule.everyWeekDay" ) );
    private ErrorLabel repeatLabel = null;
    private ICallback<IChangeHandler> onChangeHandler;

    public DailyRecurrenceEditor() {
      HorizontalPanel hp = new HorizontalPanel();
      everyNDaysRb.setStyleName( "recurrenceRadioButton" ); //$NON-NLS-1$
      everyNDaysRb.setChecked( true );
      hp.add( everyNDaysRb );

      repeatValueTb.setWidth( "3em" ); //$NON-NLS-1$
      repeatValueTb.setTitle( Messages.getString( "schedule.numDaysToRepeat" ) );
      hp.add( repeatValueTb );

      Label l = new Label( Messages.getString( "schedule.dayOrDays" ) );
      l.setStyleName( "endLabel" ); //$NON-NLS-1$
      hp.add( l );
      repeatLabel = new ErrorLabel( hp );
      add( repeatLabel );

      everyWeekdayRb.setStyleName( "recurrenceRadioButton" ); //$NON-NLS-1$
      add( everyWeekdayRb );
      configureOnChangeHandler();
    }

    public void reset() {
      setRepeatValue( "" ); //$NON-NLS-1$
      setEveryNDays();
    }

    public String getRepeatValue() {
      return repeatValueTb.getText();
    }

    public void setRepeatValue( String repeatValue ) {
      repeatValueTb.setText( repeatValue );
    }

    public void setEveryNDays() {
      everyNDaysRb.setChecked( true );
      everyWeekdayRb.setChecked( false );
    }

    public boolean isEveryNDays() {
      return everyNDaysRb.isChecked();
    }

    public void setEveryWeekday() {
      everyWeekdayRb.setChecked( true );
      everyNDaysRb.setChecked( false );
    }

    public boolean isEveryWeekday() {
      return everyWeekdayRb.isChecked();
    }

    public void setRepeatError( String errorMsg ) {
      repeatLabel.setErrorMsg( errorMsg );
    }

    public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
      this.onChangeHandler = handler;
    }

    private void changeHandler() {
      if ( null != onChangeHandler ) {
        onChangeHandler.onHandle( this );
      }
    }

    private void configureOnChangeHandler() {
      final DailyRecurrenceEditor localThis = this;

      KeyboardListener keyboardListener = new KeyboardListener() {
        public void onKeyDown( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyPress( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyUp( Widget sender, char keyCode, int modifiers ) {
          localThis.changeHandler();
        }
      };

      ClickListener clickListener = new ClickListener() {
        public void onClick( Widget sender ) {
          localThis.changeHandler();
        }
      };

      repeatValueTb.addKeyboardListener( keyboardListener );
      everyNDaysRb.addClickListener( clickListener );
      everyNDaysRb.addKeyboardListener( keyboardListener );
      everyWeekdayRb.addClickListener( clickListener );
      everyWeekdayRb.addKeyboardListener( keyboardListener );
    }
  }

  public class WeeklyRecurrenceEditor extends VerticalPanel implements IChangeHandler {

    private Map<DayOfWeek, CheckBox> dayToCheckBox = new HashMap<DayOfWeek, CheckBox>();
    private ErrorLabel everyWeekOnLabel = null;
    private ICallback<IChangeHandler> onChangeHandler;

    public WeeklyRecurrenceEditor() {
      setStyleName( "weeklyRecurrencePanel" ); //$NON-NLS-1$

      Label l = new Label( Messages.getString( "schedule.recurEveryWeek" ) );
      everyWeekOnLabel = new ErrorLabel( l );
      l.setStyleName( "startLabel" ); //$NON-NLS-1$
      add( everyWeekOnLabel );

      FlexTable gp = new FlexTable();
      gp.setCellPadding( 0 );
      gp.setCellSpacing( 0 );
      // add Sun - Wed
      final int ITEMS_IN_ROW = 4;
      for ( int ii = 0; ii < ITEMS_IN_ROW; ++ii ) {
        DayOfWeek day = DayOfWeek.get( ii );
        CheckBox cb = new CheckBox( day.toString() );
        cb.setStylePrimaryName( DOW_CHECKBOX );
        gp.setWidget( 0, ii, cb );
        dayToCheckBox.put( day, cb );
      }
      // Add Thur - Sat
      for ( int ii = ITEMS_IN_ROW; ii < DayOfWeek.length(); ++ii ) {
        DayOfWeek day = DayOfWeek.get( ii );
        CheckBox cb = new CheckBox( day.toString() );
        cb.setStylePrimaryName( DOW_CHECKBOX );
        gp.setWidget( 1, ii - 4, cb );
        dayToCheckBox.put( day, cb );
      }
      add( gp );
      configureOnChangeHandler();
    }

    public void reset() {
      for ( DayOfWeek d : dayToCheckBox.keySet() ) {
        CheckBox cb = dayToCheckBox.get( d );
        cb.setChecked( false );
      }
    }

    public List<DayOfWeek> getCheckedDays() {
      ArrayList<DayOfWeek> checkedDays = new ArrayList<DayOfWeek>();
      for ( DayOfWeek d : EnumSet.range( DayOfWeek.SUN, DayOfWeek.SAT ) ) {
        CheckBox cb = dayToCheckBox.get( d );
        if ( cb.isChecked() ) {
          checkedDays.add( d );
        }
      }
      return checkedDays;
    }

    /**
     * 
     * @param valueOfSunday
     *          int used to adjust the starting point of the weekday sequence. If this value is 0, Sun-Sat maps to
     *          0-6, if this value is 1, Sun-Sat maps to 1-7, etc.
     * @return String comma separated list of numeric days of the week.
     */
    public String getCheckedDaysAsString( int valueOfSunday ) {
      StringBuilder sb = new StringBuilder();
      for ( DayOfWeek d : getCheckedDays() ) {
        sb.append( Integer.toString( d.value() + valueOfSunday ) ).append( "," ); //$NON-NLS-1$
      }
      sb.deleteCharAt( sb.length() - 1 );
      return sb.toString();
    }

    /**
     * 
     * @param valueOfSunday
     *          int used to adjust the starting point of the weekday sequence. If this value is 0, Sun-Sat maps to
     *          0-6, if this value is 1, Sun-Sat maps to 1-7, etc.
     * @return String comma separated list of numeric days of the week.
     */
    public void setCheckedDaysAsString( String strDays, int valueOfSunday ) {
      String[] days = strDays.split( "," ); //$NON-NLS-1$
      for ( String day : days ) {
        int intDay = Integer.parseInt( day ) - valueOfSunday;
        DayOfWeek dayOfWeek = DayOfWeek.get( intDay );
        CheckBox cb = dayToCheckBox.get( dayOfWeek );
        cb.setChecked( true );
      }
    }

    public int getNumCheckedDays() {
      int numCheckedDays = 0;
      // for ( DayOfWeek d : EnumSet.range( DayOfWeek.SUN, DayOfWeek.SAT) ) {
      for ( Map.Entry<DayOfWeek, CheckBox> cbEntry : dayToCheckBox.entrySet() ) {
        if ( cbEntry.getValue().isChecked() ) {
          numCheckedDays++;
        }
      }
      return numCheckedDays;
    }

    public void setEveryDayOnError( String errorMsg ) {
      everyWeekOnLabel.setErrorMsg( errorMsg );
    }

    public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
      this.onChangeHandler = handler;
    }

    private void changeHandler() {
      if ( null != onChangeHandler ) {
        onChangeHandler.onHandle( this );
      }
    }

    private void configureOnChangeHandler() {

      final WeeklyRecurrenceEditor localThis = this;

      KeyboardListener keyboardListener = new KeyboardListener() {
        public void onKeyDown( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyPress( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyUp( Widget sender, char keyCode, int modifiers ) {
          localThis.changeHandler();
        }
      };

      ClickListener clickListener = new ClickListener() {
        public void onClick( Widget sender ) {
          localThis.changeHandler();
        }
      };
      for ( DayOfWeek d : dayToCheckBox.keySet() ) {
        CheckBox cb = dayToCheckBox.get( d );
        cb.addClickListener( clickListener );
        cb.addKeyboardListener( keyboardListener );
      }
    }
  }

  public class MonthlyRecurrenceEditor extends VerticalPanel implements IChangeHandler {

    private RadioButton dayNOfMonthRb = new RadioButton( MONTHLY_RB_GROUP, Messages.getString( "schedule.day" ) );
    private RadioButton nthDayNameOfMonthRb = new RadioButton( MONTHLY_RB_GROUP, Messages.getString( "schedule.the" ) );
    private TextBox dayOfMonthTb = new TextBox();
    private ListBox whichWeekLb = createWhichWeekListBox();
    private ListBox dayOfWeekLb = createDayOfWeekListBox();
    private ErrorLabel dayNOfMonthLabel = null;
    private ICallback<IChangeHandler> onChangeHandler;

    public MonthlyRecurrenceEditor() {
      setSpacing( 6 );

      HorizontalPanel hp = new HorizontalPanel();
      dayNOfMonthRb.setStyleName( "recurrenceRadioButton" ); //$NON-NLS-1$
      dayNOfMonthRb.setChecked( true );
      hp.add( dayNOfMonthRb );
      dayOfMonthTb.setWidth( "3em" ); //$NON-NLS-1$
      hp.add( dayOfMonthTb );
      Label l = new Label( Messages.getString( "schedule.ofEveryMonth" ) );
      l.setStyleName( "endLabel" ); //$NON-NLS-1$
      hp.add( l );

      dayNOfMonthLabel = new ErrorLabel( hp );
      add( dayNOfMonthLabel );

      hp = new HorizontalPanel();
      nthDayNameOfMonthRb.setStyleName( "recurrenceRadioButton" ); //$NON-NLS-1$
      hp.add( nthDayNameOfMonthRb );
      hp.add( whichWeekLb );

      hp.add( dayOfWeekLb );
      l = new Label( Messages.getString( "schedule.ofEveryMonth" ) );
      l.setStyleName( "endLabel" ); //$NON-NLS-1$
      hp.add( l );
      add( hp );
      configureOnChangeHandler();
    }

    public void reset() {
      setDayNOfMonth();
      setDayOfMonth( "" ); //$NON-NLS-1$
      setWeekOfMonth( WeekOfMonth.FIRST );
      setDayOfWeek( DayOfWeek.SUN );
    }

    public void setDayNOfMonth() {
      dayNOfMonthRb.setChecked( true );
      nthDayNameOfMonthRb.setChecked( false );
    }

    public boolean isDayNOfMonth() {
      return dayNOfMonthRb.isChecked();
    }

    public void setNthDayNameOfMonth() {
      nthDayNameOfMonthRb.setChecked( true );
      dayNOfMonthRb.setChecked( false );
    }

    public boolean isNthDayNameOfMonth() {
      return nthDayNameOfMonthRb.isChecked();
    }

    public String getDayOfMonth() {
      return dayOfMonthTb.getText();
    }

    public void setDayOfMonth( String dayOfMonth ) {
      dayOfMonthTb.setText( dayOfMonth );
    }

    public WeekOfMonth getWeekOfMonth() {
      return WeekOfMonth.get( whichWeekLb.getSelectedIndex() );
    }

    public void setWeekOfMonth( WeekOfMonth week ) {
      whichWeekLb.setSelectedIndex( week.value() );
    }

    public DayOfWeek getDayOfWeek() {
      return DayOfWeek.get( dayOfWeekLb.getSelectedIndex() );
    }

    public void setDayOfWeek( DayOfWeek day ) {
      dayOfWeekLb.setSelectedIndex( day.value() );
    }

    public void setDayNOfMonthError( String errorMsg ) {
      dayNOfMonthLabel.setErrorMsg( errorMsg );
    }

    public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
      this.onChangeHandler = handler;
    }

    private void changeHandler() {
      if ( null != onChangeHandler ) {
        onChangeHandler.onHandle( this );
      }
    }

    private void configureOnChangeHandler() {
      final MonthlyRecurrenceEditor localThis = this;

      KeyboardListener keyboardListener = new KeyboardListener() {
        public void onKeyDown( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyPress( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyUp( Widget sender, char keyCode, int modifiers ) {
          localThis.changeHandler();
        }
      };

      ClickListener clickListener = new ClickListener() {
        public void onClick( Widget sender ) {
          localThis.changeHandler();
        }
      };

      ChangeListener changeListener = new ChangeListener() {
        public void onChange( Widget sender ) {
          localThis.changeHandler();
        }
      };
      dayNOfMonthRb.addClickListener( clickListener );
      dayNOfMonthRb.addKeyboardListener( keyboardListener );
      nthDayNameOfMonthRb.addClickListener( clickListener );
      nthDayNameOfMonthRb.addKeyboardListener( keyboardListener );
      dayOfMonthTb.addKeyboardListener( keyboardListener );
      whichWeekLb.addChangeListener( changeListener );
      dayOfWeekLb.addChangeListener( changeListener );
    }
  }

  public class YearlyRecurrenceEditor extends VerticalPanel implements IChangeHandler {

    private RadioButton everyMonthOnNthDayRb = new RadioButton( YEARLY_RB_GROUP,
      Messages.getString( "schedule.every" ) );
    private RadioButton nthDayNameOfMonthNameRb = new RadioButton( YEARLY_RB_GROUP, Messages.getString( "schedule.the" ) );
    private TextBox dayOfMonthTb = new TextBox();
    private ListBox monthOfYearLb0 = createMonthOfYearListBox();
    private ListBox monthOfYearLb1 = createMonthOfYearListBox();
    private ListBox whichWeekLb = createWhichWeekListBox();
    private ListBox dayOfWeekLb = createDayOfWeekListBox();
    private ErrorLabel dayOfMonthLabel = null;
    private ICallback<IChangeHandler> onChangeHandler;

    private static final String YEARLY_RB_GROUP = "yearly-group"; //$NON-NLS-1$

    public YearlyRecurrenceEditor() {
      setSpacing( 6 );

      HorizontalPanel p = new HorizontalPanel();
      everyMonthOnNthDayRb.setStyleName( "recurrenceRadioButton" ); //$NON-NLS-1$
      everyMonthOnNthDayRb.setChecked( true );
      p.add( everyMonthOnNthDayRb );
      p.add( monthOfYearLb0 );
      dayOfMonthTb.setStylePrimaryName( "DAY_OF_MONTH_TB" ); //$NON-NLS-1$
      dayOfMonthTb.setWidth( "3em" ); //$NON-NLS-1$
      p.add( dayOfMonthTb );
      dayOfMonthLabel = new ErrorLabel( p );
      add( dayOfMonthLabel );

      p = new HorizontalPanel();
      nthDayNameOfMonthNameRb.setStyleName( "recurrenceRadioButton" ); //$NON-NLS-1$
      p.add( nthDayNameOfMonthNameRb );
      p.add( whichWeekLb );
      p.add( dayOfWeekLb );
      Label l = new Label( Messages.getString( "schedule.of" ) );
      l.setStyleName( "middleLabel" ); //$NON-NLS-1$
      p.add( l );
      p.add( monthOfYearLb1 );
      add( p );
      configureOnChangeHandler();
    }

    public void reset() {
      setEveryMonthOnNthDay();
      setMonthOfYear0( MonthOfYear.JAN );
      setDayOfMonth( "" ); //$NON-NLS-1$
      setWeekOfMonth( WeekOfMonth.FIRST );
      setDayOfWeek( DayOfWeek.SUN );
      setMonthOfYear1( MonthOfYear.JAN );
    }

    public boolean isEveryMonthOnNthDay() {
      return everyMonthOnNthDayRb.isChecked();
    }

    public void setEveryMonthOnNthDay() {
      everyMonthOnNthDayRb.setChecked( true );
      nthDayNameOfMonthNameRb.setChecked( false );
    }

    public boolean isNthDayNameOfMonthName() {
      return nthDayNameOfMonthNameRb.isChecked();
    }

    public void setNthDayNameOfMonthName() {
      nthDayNameOfMonthNameRb.setChecked( true );
      everyMonthOnNthDayRb.setChecked( false );
    }

    public String getDayOfMonth() {
      return dayOfMonthTb.getText();
    }

    public void setDayOfMonth( String dayOfMonth ) {
      dayOfMonthTb.setText( dayOfMonth );
    }

    public WeekOfMonth getWeekOfMonth() {
      return WeekOfMonth.get( whichWeekLb.getSelectedIndex() );
    }

    public void setWeekOfMonth( WeekOfMonth week ) {
      whichWeekLb.setSelectedIndex( week.value() );
    }

    public DayOfWeek getDayOfWeek() {
      return DayOfWeek.get( dayOfWeekLb.getSelectedIndex() );
    }

    public void setDayOfWeek( DayOfWeek day ) {
      dayOfWeekLb.setSelectedIndex( day.value() );
    }

    public MonthOfYear getMonthOfYear0() {
      return MonthOfYear.get( monthOfYearLb0.getSelectedIndex() );
    }

    public void setMonthOfYear0( MonthOfYear month ) {
      monthOfYearLb0.setSelectedIndex( month.value() );
    }

    public MonthOfYear getMonthOfYear1() {
      return MonthOfYear.get( monthOfYearLb1.getSelectedIndex() );
    }

    public void setMonthOfYear1( MonthOfYear month ) {
      monthOfYearLb1.setSelectedIndex( month.value() );
    }

    public void setDayOfMonthError( String errorMsg ) {
      dayOfMonthLabel.setErrorMsg( errorMsg );
    }

    public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
      this.onChangeHandler = handler;
    }

    private void changeHandler() {
      if ( null != onChangeHandler ) {
        onChangeHandler.onHandle( this );
      }
    }

    private void configureOnChangeHandler() {

      final YearlyRecurrenceEditor localThis = this;

      KeyboardListener keyboardListener = new KeyboardListener() {
        public void onKeyDown( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyPress( Widget sender, char keyCode, int modifiers ) {
        }

        public void onKeyUp( Widget sender, char keyCode, int modifiers ) {
          localThis.changeHandler();
        }
      };

      ClickListener clickListener = new ClickListener() {
        public void onClick( Widget sender ) {
          localThis.changeHandler();
        }
      };

      ChangeListener changeListener = new ChangeListener() {
        public void onChange( Widget sender ) {
          localThis.changeHandler();
        }
      };

      everyMonthOnNthDayRb.addClickListener( clickListener );
      everyMonthOnNthDayRb.addKeyboardListener( keyboardListener );

      nthDayNameOfMonthNameRb.addClickListener( clickListener );
      nthDayNameOfMonthNameRb.addKeyboardListener( keyboardListener );

      dayOfMonthTb.addKeyboardListener( keyboardListener );

      monthOfYearLb0.addChangeListener( changeListener );
      monthOfYearLb1.addChangeListener( changeListener );
      whichWeekLb.addChangeListener( changeListener );
      dayOfWeekLb.addChangeListener( changeListener );
    }
  }

  private ListBox createDayOfWeekListBox() {
    ListBox l = new ListBox();
    for ( int ii = 0; ii < DayOfWeek.length(); ++ii ) {
      DayOfWeek day = DayOfWeek.get( ii );
      l.addItem( day.toString() );
    }
    return l;
  }

  private ListBox createMonthOfYearListBox() {

    ListBox l = new ListBox();
    for ( int ii = 0; ii < MonthOfYear.length(); ++ii ) {
      MonthOfYear month = MonthOfYear.get( ii );
      l.addItem( month.toString() );
    }

    return l;
  }

  private ListBox createWhichWeekListBox() {

    ListBox l = new ListBox();
    for ( WeekOfMonth week : EnumSet.range( WeekOfMonth.FIRST, WeekOfMonth.LAST ) ) {
      l.addItem( week.toString() );
    }

    return l;
  }

  private void selectTemporalPanel( TemporalValue selectedTemporalValue ) {
    int i = 0;
    for ( Map.Entry<TemporalValue, Panel> me : temporalPanelMap.entrySet() ) {
      if ( me.getKey().equals( selectedTemporalValue ) ) {
        deckPanel.showWidget( i );
        break;
      }
      i++;
    }
  }

  /**
   * 
   * @return null if the selected schedule does not support repeat-in-seconds, otherwise return the number of
   *         seconds between schedule execution.
   * @throws RuntimeException
   *           if the temporal value (tv) is invalid. This condition occurs as a result of programmer error.
   */
  public Long getRepeatInSecs() throws RuntimeException {
    switch ( temporalState ) {
      case WEEKLY:
        // fall through
      case MONTHLY:
        // fall through
      case YEARLY:
        return null;
      case SECONDS:
        return Long.parseLong( secondlyEditor.getValue() );
      case MINUTES:
        return TimeUtil.minutesToSecs( Long.parseLong( minutelyEditor.getValue() ) );
      case HOURS:
        return TimeUtil.hoursToSecs( Long.parseLong( hourlyEditor.getValue() ) );
      case DAILY:
        return TimeUtil.daysToSecs( Long.parseLong( dailyEditor.getRepeatValue() ) );
      default:
        throw new RuntimeException(
          Messages.getString( "schedule.invalidTemporalValueInGetRepeatInSecs", temporalState.toString() ) );
    }
  }

  /**
   * 
   * @return null if the selected schedule does not support CRON, otherwise return the CRON string.
   * @throws RuntimeException
   *           if the temporal value (tv) is invalid. This condition occurs as a result of programmer error.
   */
  public String getCronString() throws RuntimeException {
    switch ( temporalState ) {
      case SECONDS:
        // fall through
      case MINUTES:
        // fall through
      case HOURS:
        return null;
      case DAILY:
        return getDailyCronString();
      case WEEKLY:
        return getWeeklyCronString();
      case MONTHLY:
        return getMonthlyCronString();
      case YEARLY:
        return getYearlyCronString();
      default:
        throw new RuntimeException(
          Messages.getString( "schedule.invalidTemporalValueInGetCronString", temporalState.toString() ) );
    }
  }

  public boolean isEveryNDays() {
    return ( temporalState == TemporalValue.DAILY ) && dailyEditor.isEveryNDays();
  }

  public MonthOfYear getSelectedMonth() {
    MonthOfYear selectedMonth = null;
    if ( ( temporalState == TemporalValue.YEARLY ) && yearlyEditor.isNthDayNameOfMonthName() ) {
      selectedMonth = yearlyEditor.getMonthOfYear1();
    } else if ( ( temporalState == TemporalValue.YEARLY ) && yearlyEditor.isEveryMonthOnNthDay() ) {
      selectedMonth = yearlyEditor.getMonthOfYear0();
    }
    return selectedMonth;
  }

  public List<DayOfWeek> getSelectedDaysOfWeek() {
    ArrayList<DayOfWeek> selectedDaysOfWeek = new ArrayList<DayOfWeek>();
    if ( ( temporalState == TemporalValue.DAILY ) && !dailyEditor.isEveryNDays() ) {
      selectedDaysOfWeek.add( DayOfWeek.MON );
      selectedDaysOfWeek.add( DayOfWeek.TUE );
      selectedDaysOfWeek.add( DayOfWeek.WED );
      selectedDaysOfWeek.add( DayOfWeek.THU );
      selectedDaysOfWeek.add( DayOfWeek.FRI );
    } else if ( temporalState == TemporalValue.WEEKLY ) {
      selectedDaysOfWeek.addAll( weeklyEditor.getCheckedDays() );
    } else if ( ( temporalState == TemporalValue.MONTHLY ) && monthlyEditor.isNthDayNameOfMonth() ) {
      selectedDaysOfWeek.add( monthlyEditor.getDayOfWeek() );
    } else if ( ( temporalState == TemporalValue.YEARLY ) && yearlyEditor.isNthDayNameOfMonthName() ) {
      selectedDaysOfWeek.add( yearlyEditor.getDayOfWeek() );
    }
    return selectedDaysOfWeek;
  }

  public WeekOfMonth getSelectedWeekOfMonth() {
    WeekOfMonth selectedWeekOfMonth = null;
    if ( ( temporalState == TemporalValue.MONTHLY ) && monthlyEditor.isNthDayNameOfMonth() ) {
      selectedWeekOfMonth = monthlyEditor.getWeekOfMonth();
    } else if ( ( temporalState == TemporalValue.YEARLY ) && yearlyEditor.isNthDayNameOfMonthName() ) {
      selectedWeekOfMonth = yearlyEditor.getWeekOfMonth();
    }
    return selectedWeekOfMonth;
  }

  public Integer getSelectedDayOfMonth() {
    Integer selectedDayOfMonth = null;
    if ( ( temporalState == TemporalValue.MONTHLY ) && monthlyEditor.isDayNOfMonth() ) {
      try {
        selectedDayOfMonth = Integer.parseInt( monthlyEditor.getDayOfMonth() );
      } catch ( Exception ex ) {
        //ignored
      }
    } else if ( ( temporalState == TemporalValue.YEARLY ) && yearlyEditor.isEveryMonthOnNthDay() ) {
      try {
        selectedDayOfMonth = Integer.parseInt( yearlyEditor.getDayOfMonth() );
      } catch ( Exception ex ) {
        //ignored
      }
    }
    return selectedDayOfMonth;
  }

  /**
   * 
   * @return
   * @throws RuntimeException
   */
  private String getDailyCronString() throws RuntimeException {
    String cronStr;
    StringBuilder recurrenceSb = new StringBuilder();
    if ( dailyEditor.isEveryNDays() ) {
      return null;
    } else {
      // must be every weekday
      recurrenceSb.append( RecurrenceType.EveryWeekday ).append( SPACE ).append( getTimeOfRecurrence() );
      try {
        cronStr = CronParser.recurrenceStringToCronString( recurrenceSb.toString() );
      } catch ( CronParseException e ) {
        throw new RuntimeException( Messages.getString( "schedule.invalidRecurrenceString", recurrenceSb.toString() ) );
      }
      return cronStr;
    }
  }

  private String getWeeklyCronString() throws RuntimeException {
    String cronStr;
    StringBuilder recurrenceSb = new StringBuilder();
    // WeeklyOn 0 33 6 1,3,5
    recurrenceSb.append( RecurrenceType.WeeklyOn ).append( SPACE ).append( getTimeOfRecurrence() ).append( SPACE )
        .append( weeklyEditor.getCheckedDaysAsString( VALUE_OF_SUNDAY ) );
    try {
      cronStr = CronParser.recurrenceStringToCronString( recurrenceSb.toString() );
    } catch ( CronParseException e ) {
      throw new RuntimeException( Messages.getString( "schedule.invalidRecurrenceString", recurrenceSb.toString() ) );
    }
    return cronStr;

  }

  private String getMonthlyCronString() throws RuntimeException {
    String cronStr;
    StringBuilder recurrenceSb = new StringBuilder();
    if ( monthlyEditor.isDayNOfMonth() ) {
      recurrenceSb.append( RecurrenceType.DayNOfMonth ).append( SPACE ).append( getTimeOfRecurrence() ).append( SPACE )
          .append( monthlyEditor.getDayOfMonth() );
    } else if ( monthlyEditor.isNthDayNameOfMonth() ) {
      if ( monthlyEditor.getWeekOfMonth() != WeekOfMonth.LAST ) {
        String weekOfMonth = Integer.toString( monthlyEditor.getWeekOfMonth().value() + 1 );
        String dayOfWeek = Integer.toString( monthlyEditor.getDayOfWeek().value() + 1 );
        recurrenceSb.append( RecurrenceType.NthDayNameOfMonth ).append( SPACE ).append( getTimeOfRecurrence() ).append(
            SPACE ).append( dayOfWeek ).append( SPACE ).append( weekOfMonth );
      } else {
        String dayOfWeek = Integer.toString( monthlyEditor.getDayOfWeek().value() + 1 );
        recurrenceSb.append( RecurrenceType.LastDayNameOfMonth ).append( SPACE ).append( getTimeOfRecurrence() )
            .append( SPACE ).append( dayOfWeek );
      }
    } else {
      throw new RuntimeException( Messages.getString( "schedule.noRadioBtnsSelected" ) );
    }
    try {
      cronStr = CronParser.recurrenceStringToCronString( recurrenceSb.toString() );
    } catch ( CronParseException e ) {
      throw new RuntimeException( Messages.getString( "schedule.invalidRecurrenceString", recurrenceSb.toString() ) );
    }
    return cronStr;
  }

  private String getYearlyCronString() throws RuntimeException {
    String cronStr;
    StringBuilder recurrenceSb = new StringBuilder();
    if ( yearlyEditor.isEveryMonthOnNthDay() ) {
      String monthOfYear = Integer.toString( yearlyEditor.getMonthOfYear0().value() + 1 );
      recurrenceSb.append( RecurrenceType.EveryMonthNameN ).append( SPACE ).append( getTimeOfRecurrence() ).append(
          SPACE ).append( yearlyEditor.getDayOfMonth() ).append( SPACE ).append( monthOfYear );
    } else if ( yearlyEditor.isNthDayNameOfMonthName() ) {
      if ( yearlyEditor.getWeekOfMonth() != WeekOfMonth.LAST ) {
        String monthOfYear = Integer.toString( yearlyEditor.getMonthOfYear1().value() + 1 );
        String dayOfWeek = Integer.toString( yearlyEditor.getDayOfWeek().value() + 1 );
        String weekOfMonth = Integer.toString( yearlyEditor.getWeekOfMonth().value() + 1 );
        recurrenceSb.append( RecurrenceType.NthDayNameOfMonthName ).append( SPACE ).append( getTimeOfRecurrence() )
            .append( SPACE ).append( dayOfWeek ).append( SPACE ).append( weekOfMonth ).append( SPACE ).append(
          monthOfYear );
      } else {
        String monthOfYear = Integer.toString( yearlyEditor.getMonthOfYear1().value() + 1 );
        String dayOfWeek = Integer.toString( yearlyEditor.getDayOfWeek().value() + 1 );
        recurrenceSb.append( RecurrenceType.LastDayNameOfMonthName ).append( SPACE ).append( getTimeOfRecurrence() )
            .append( SPACE ).append( dayOfWeek ).append( SPACE ).append( monthOfYear );
      }
    } else {
      throw new RuntimeException( Messages.getString( "schedule.noRadioBtnsSelected" ) );
    }
    try {
      cronStr = CronParser.recurrenceStringToCronString( recurrenceSb.toString() );
    } catch ( CronParseException e ) {
      throw new RuntimeException( Messages.getString( "schedule.invalidRecurrenceString", recurrenceSb.toString() ) );
    }
    return cronStr;
  }

  private StringBuilder getTimeOfRecurrence() {
    int timeOfDayAdjust = ( startTimePicker.getTimeOfDay().equals( TimeUtil.TimeOfDay.AM ) ) ? TimeUtil.MIN_HOUR // 0
        : TimeUtil.MAX_HOUR; // 12
    String strHour = StringUtils.addStringToInt( startTimePicker.getHour(), timeOfDayAdjust );
    return new StringBuilder().append( "00" ).append( SPACE ) //$NON-NLS-1$
        .append( startTimePicker.getMinute() ).append( SPACE ).append( strHour );
  }

  // TODO sbarkdull
  // private static DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

  public void setStartTime( String startTime ) {
    startTimePicker.setTime( startTime );
  }

  public String getStartTime() {
    return startTimePicker.getTime();
  }

  public void setStartDate( Date startDate ) {
    dateRangeEditor.setStartDate( startDate );
  }

  public Date getStartDate() {
    return dateRangeEditor.getStartDate();
  }

  public void setEndDate( Date endDate ) {
    dateRangeEditor.setEndDate( endDate );
  }

  public Date getEndDate() {
    return dateRangeEditor.getEndDate();
  }

  public void setNoEndDate() {
    dateRangeEditor.setNoEndDate();
  }

  public void setEndBy() {
    dateRangeEditor.setEndBy();
  }

  public TemporalValue getTemporalState() {
    return temporalState;
  }

  public void setTemporalState( TemporalValue temporalState ) {
    this.temporalState = temporalState;
    selectTemporalPanel( temporalState );
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return DateRangeEditor
   */
  public DateRangeEditor getDateRangeEditor() {
    return dateRangeEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return SecondlyRecurrencePanel
   */
  public SecondlyRecurrenceEditor getSecondlyEditor() {
    return secondlyEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return MinutelyRecurrencePanel
   */
  public MinutelyRecurrenceEditor getMinutelyEditor() {
    return minutelyEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return HourlyRecurrencePanel
   */
  public HourlyRecurrenceEditor getHourlyEditor() {
    return hourlyEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return DailyRecurrencePanel
   */
  public DailyRecurrenceEditor getDailyEditor() {
    return dailyEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return WeeklyRecurrencePanel
   */
  public WeeklyRecurrenceEditor getWeeklyEditor() {
    return weeklyEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return MonthlyRecurrencePanel
   */
  public MonthlyRecurrenceEditor getMonthlyEditor() {
    return monthlyEditor;
  }

  /**
   * NOTE: should only ever be used by validators. This is a backdoor into this class that shouldn't be here, do
   * not use this method unless you are validating.
   * 
   * @return YearlyRecurrencePanel
   */
  public YearlyRecurrenceEditor getYearlyEditor() {
    return yearlyEditor;
  }

  public void setOnChangeHandler( ICallback<IChangeHandler> handler ) {
    this.onChangeHandler = handler;
  }

  private void changeHandler() {
    if ( null != onChangeHandler ) {
      onChangeHandler.onHandle( this );
    }
  }

  private void configureOnChangeHandler() {
    final RecurrenceEditor localThis = this;

    ICallback<IChangeHandler> handler = new ICallback<IChangeHandler>() {
      public void onHandle( IChangeHandler o ) {
        localThis.changeHandler();
      }
    };

    startTimePicker.setOnChangeHandler( handler );
    dateRangeEditor.setOnChangeHandler( handler );

    secondlyEditor.setOnChangeHandler( handler );
    minutelyEditor.setOnChangeHandler( handler );
    hourlyEditor.setOnChangeHandler( handler );
    dailyEditor.setOnChangeHandler( handler );
    weeklyEditor.setOnChangeHandler( handler );
    monthlyEditor.setOnChangeHandler( handler );
    yearlyEditor.setOnChangeHandler( handler );
  }
}
