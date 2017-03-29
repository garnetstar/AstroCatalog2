package cz.uhk.machacekgoogle.UI;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;

import cz.uhk.machacekgoogle.Model.DateTimeObject;

/**
 * @author Jan Macháček
 *         Created on 14.10.2016.
 */
public class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private View view;
    // obdoba type "hidden" v HTML form
    private View hiddenView;

    private String date;

    public void setViews(View view, View hiddenView) {
        this.view = view;
        this.hiddenView = hiddenView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;

        if (date != null) {
            try {
                DateTimeObject dateTimeObject = new DateTimeObject(date);
                year = dateTimeObject.getYear();
                month = dateTimeObject.getMonth() - 1;
                day = dateTimeObject.getDay();
                return new DatePickerDialog(getActivity(), this, year, month, day);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {

            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            Log.d("astro", month + " " + year + " " + day);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
        TextView view = (TextView) this.view;
        TextView hiddenView = (TextView) this.hiddenView;

        view.setText(String.format("%02d.%02d.%04d", day, month + 1, year));
        hiddenView.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
    }

    public void setDate(String date) {
        this.date = date;
    }
}
