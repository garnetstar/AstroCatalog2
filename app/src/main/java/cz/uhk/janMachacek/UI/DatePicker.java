package cz.uhk.janMachacek.UI;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

/**
 * @author Jan Macháček
 *         Created on 14.10.2016.
 */
public class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private View view;
    // obdoba type "hidden" v HTML form
    private View hiddenView;

    public void setViews(View view, View hiddenView) {
        this.view = view;
        this.hiddenView = hiddenView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
        TextView view = (TextView) this.view;
        TextView hiddenView = (TextView) this.hiddenView;

        view.setText(String.format("%02d.%02d.%04d", day, month, year));
        hiddenView.setText(String.format("%04d-%02d-%02d", year, month, day));
    }
}
