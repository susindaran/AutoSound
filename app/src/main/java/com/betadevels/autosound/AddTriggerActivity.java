package com.betadevels.autosound;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.betadevels.autosound.DAOs.Trigger;
import com.betadevels.autosound.layouts.AutoSpaceFlowLayout;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

public class AddTriggerActivity extends AppCompatActivity implements CalendarDatePickerDialogFragment.OnDateSetListener,
        RadialTimePickerDialogFragment.OnTimeSetListener
{
    private static final String DATE_PICKER_TAG = "DATE_PICKER_FRAGMENT";
    private static final String TIME_PICKER_TAG = "TIME_PICKER_FRAGMENT";
    private static final String TAG = "AddTriggerActivity";

    private AutoSpaceFlowLayout autoSpaceFlowLayout;
    private CheckBox[] weekDaysCheckBoxes = new CheckBox[7];
    private Button setDateButton;
    private Button setTimeButton;
    private Button cancelButton;
    private Button createButton;
    private Switch repeatSwitch;
    private Spinner ringerModeSpinner;
    private SeekBar ringerVolumeSeekBar;
    private SeekBar mediaVolumeSeekBar;
    private SeekBar alarmVolumeSeekBar;
    private TextView ringerVolumeText;

    CalendarDatePickerDialogFragment datePicker;
    RadialTimePickerDialogFragment timePicker;
    private boolean isDateSet = false;
    private boolean isTimeSet = false;
    private int year, month, day, hourOfDay, minute;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trigger);

        //TODO: Load saved data if any present

        repeatSwitch = (Switch) findViewById( R.id.repeat_switch );
        boolean isChecked = false;
        if( repeatSwitch != null )
        {
            isChecked = repeatSwitch.isChecked();
            repeatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    for( CheckBox checkBox : weekDaysCheckBoxes )
                    {
                        if( checkBox != null )
                        {
                            checkBox.setEnabled( isChecked );
                        }
                    }
                    setDateButton.setEnabled( !isChecked );

                    if( !isChecked )
                    {
                        autoSpaceFlowLayout.setBackground( null );
                    }
                    else
                    {
                        setDateButton.setError( null );
                    }
                }
            });
        }

        autoSpaceFlowLayout = (AutoSpaceFlowLayout) findViewById( R.id.days_week_group );
        if (autoSpaceFlowLayout != null)
        {
            for( int i = 0; i < autoSpaceFlowLayout.getChildCount(); i++ )
            {
                weekDaysCheckBoxes[i] = ( CheckBox )autoSpaceFlowLayout.getChildAt( i );
                weekDaysCheckBoxes[i].setEnabled( isChecked );
            }
        }

        setDateButton = (Button) findViewById( R.id.set_date_btn );
        if (setDateButton != null)
        {
            setDateButton.setEnabled( !isChecked );
            setDateButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if( datePicker == null )
                    {
                        datePicker = new CalendarDatePickerDialogFragment();
                        datePicker.setOnDateSetListener(AddTriggerActivity.this);
                    }
                    datePicker.show( getSupportFragmentManager(), DATE_PICKER_TAG );
                }
            });
        }

        setTimeButton = (Button) findViewById( R.id.set_time_btn );
        if( setTimeButton != null )
        {
            setTimeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if( timePicker == null )
                    {
                        timePicker = new RadialTimePickerDialogFragment();
                        timePicker.setOnTimeSetListener(AddTriggerActivity.this);
                    }
                    timePicker.show(getSupportFragmentManager(), TIME_PICKER_TAG);
                }
            });
        }

        ringerModeSpinner = (Spinner) findViewById( R.id.ringer_mode_spnr );
        if( ringerModeSpinner != null )
        {
            ringerModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    String selectedRingerMode = parent.getItemAtPosition( position ).toString();
                    Log.i(TAG, "onItemSelected: Selected Ringer mode : " + selectedRingerMode);
                    if( ringerVolumeSeekBar != null && ringerVolumeText != null )
                    {
                        boolean isNormalModeSelected = selectedRingerMode.equalsIgnoreCase("Normal");
                        ringerVolumeSeekBar.setEnabled( isNormalModeSelected );
                        ringerVolumeSeekBar.setVisibility(isNormalModeSelected ? View.VISIBLE : View.GONE);
                        ringerVolumeText.setVisibility( isNormalModeSelected ? View.VISIBLE : View.GONE );
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    //Nothing to do here!
                }
            });
        }

        ringerVolumeText = (TextView) findViewById( R.id.ringer_volume_txt );
        ringerVolumeSeekBar = (SeekBar) findViewById( R.id.ringer_volume_skbar );
        mediaVolumeSeekBar = (SeekBar) findViewById( R.id.media_volume_skbar );
        alarmVolumeSeekBar = (SeekBar) findViewById( R.id.alarm_volume_skbar );
        if( ringerVolumeSeekBar != null && ringerVolumeText != null )
        {
            boolean isNormalModeSelected = ringerModeSpinner.getSelectedItem().toString().equalsIgnoreCase("Normal");
            ringerVolumeSeekBar.setEnabled( isNormalModeSelected );
            ringerVolumeSeekBar.setVisibility(isNormalModeSelected ? View.VISIBLE : View.GONE);
            ringerVolumeText.setVisibility( isNormalModeSelected ? View.VISIBLE : View.GONE );
        }

        createButton = (Button) findViewById( R.id.create_btn );
        if( createButton != null )
        {
            createButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean isFormValid = true;
                    if( repeatSwitch.isChecked() )
                    {
                        int boxesChecked = 0;
                        for( CheckBox checkBox : weekDaysCheckBoxes )
                        {
                            boxesChecked += checkBox.isChecked() ? 1 : 0;
                        }

                        if( boxesChecked == 0 )
                        {
                            if( autoSpaceFlowLayout != null )
                            {
                                autoSpaceFlowLayout.setBackground( ContextCompat.getDrawable( getBaseContext(), R.drawable.rounded_rect_border_red_shape) );
                            }
                            isFormValid = false;
                            Snackbar.make( weekDaysCheckBoxes[6], "Select day(s) for trigger!", Snackbar.LENGTH_INDEFINITE )
                                    .setAction("Dismiss", new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Log.i(TAG, "onClick: SnackBar clicked!");
                                        }
                                    })
                                    .show();
                        }
                        else if( autoSpaceFlowLayout != null )
                        {
                            autoSpaceFlowLayout.setBackground( null );
                        }
                    }
                    else if( !isDateSet )
                    {
                        isFormValid = false;
                        setDateButton.setError( "Date not set" );
                    }

                    if( !isTimeSet )
                    {
                        isFormValid = false;
                        setTimeButton.setError( "Time not set" );
                    }

                    if( isFormValid )
                    {
                        Trigger trigger = new Trigger( repeatSwitch.isChecked(), weekDaysCheckBoxes,
                                year, month, day, hourOfDay, minute, ringerModeSpinner.getSelectedItem().toString(),
                                ringerVolumeSeekBar.getProgress(), mediaVolumeSeekBar.getProgress(),
                                alarmVolumeSeekBar.getProgress());
                        trigger.save();

                        //TODO: Create entry(ies) in TriggerInstance table and schedule alarm(s)

                        setResult( RESULT_OK );
                        finish();
                    }
                }
            });
        }

        cancelButton = (Button) findViewById( R.id.cancel_btn );
        if( cancelButton != null )
        {
            cancelButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    setResult( RESULT_CANCELED );
                    finish();
                }
            });
        }
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth)
    {
        String datePrint = "Year : " + year + " Month : " + monthOfYear + " Day : " + dayOfMonth;
        Log.i(TAG, "onDateSet: "+datePrint);

        //TODO: Properly format text before setting to TextView
        setDateButton.setText(dayOfMonth + "/" + monthOfYear+"/"+year);
        setDateButton.setError( null );
        isDateSet = true;
        this.year = year;
        //Joda DateTime expects month to be between 1 to 12
        this.month = monthOfYear + 1;
        this.day = dayOfMonth;
    }

    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute)
    {
        String timePrint = hourOfDay + ":" + minute;
        Log.i(TAG, "onTimeSet: " + timePrint);

        setTimeButton.setText( timePrint );
        setTimeButton.setError( null );
        isTimeSet = true;
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        CalendarDatePickerDialogFragment datePicker = (CalendarDatePickerDialogFragment) getSupportFragmentManager()
                .findFragmentByTag(DATE_PICKER_TAG);
        if (datePicker != null)
        {
            datePicker.setOnDateSetListener( this );
        }

        RadialTimePickerDialogFragment timePicker = (RadialTimePickerDialogFragment) getSupportFragmentManager()
                .findFragmentByTag(TIME_PICKER_TAG );
        if( timePicker != null )
        {
            timePicker.setOnTimeSetListener( this );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //TODO: Save inputs
    }
}
