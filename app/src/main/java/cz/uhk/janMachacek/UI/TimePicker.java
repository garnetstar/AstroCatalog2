package cz.uhk.janMachacek.UI;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.ParcelFormatException;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.Calendar;

import cz.uhk.janMachacek.Model.DateTimeObject;
import cz.uhk.janMachacek.R;

/**
 * @author Jan Macháček
 *         Created on 13.10.2016.
 */
public class TimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private View v;

    private String date;

    @Override
    public void onTimeSet(android.widget.TimePicker timePicker, int hourOfDay, int minute) {
        TextView button = (TextView) this.v;
        button.setText(String.format("%02d:%02d", hourOfDay, minute));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour, minute;

        if (date != null) {
            try {
                DateTimeObject dateTimeObject = new DateTimeObject(date);

                hour = dateTimeObject.getHour();
                minute = dateTimeObject.getMinute();
                Log.d("astro", "time-" + hour + " " + minute);
                return new TimePickerDialog(getActivity(), this, hour, minute,
                        DateFormat.is24HourFormat(getActivity()));
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            Log.d("astro", "time-" + hour + " " + minute);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
    }

    public void setView(View v) {
        this.v = v;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
