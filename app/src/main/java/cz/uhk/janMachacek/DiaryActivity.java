package cz.uhk.janMachacek;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.uhk.janMachacek.UI.DatePicker;
import cz.uhk.janMachacek.UI.TimePicker;
import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Utils;

/**
 * @author Jan Macháček
 *         Created on 14.10.2016.
 */
public class DiaryActivity extends AbstactBaseActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.diary);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        findLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        Angle latitude = new Angle(location.getLatitude());
        Angle longitude = new Angle(location.getLongitude());

        String message = "Lat: " + Utils.getFormatedDegree(latitude) + " Lon: "
                + Utils.getFormatedDegree(longitude);
        Log.d("astro", "poloha=" + message);
        TextView form_location = (TextView) findViewById(R.id.form_location);
        form_location.setText(message);
        hideProgressDialog();
    }

    public void showTimePickerDialog(View view) {
        TimePicker newFragment = new TimePicker();
        newFragment.setView(view);
        newFragment.show(getFragmentManager(), "timePicker");

    }

    public void showDatePickerDialogFrom(View view) {

        View hiddenFrom = (TextView) findViewById(R.id.dateFromDatabase);
        DatePicker newFragment = new DatePicker();
        newFragment.setViews(view, hiddenFrom);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showDatePickerDialogTo(View view) {

        View hiddenTo = (TextView) findViewById(R.id.dateToDatabase);
        DatePicker newFragment = new DatePicker();
        newFragment.setViews(view, hiddenTo);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void save(View view) {

        TextView dateFrom = (TextView)findViewById(R.id.dateFromDatabase);
        TextView timeFrom = (TextView)findViewById(R.id.timeFrom);

        TextView dateTo = (TextView)findViewById(R.id.dateToDatabase);
        TextView timeTo = (TextView)findViewById(R.id.timeTo);

        String from = String.format("%s %s:00", dateFrom.getText(), timeFrom.getText());
        String to = String.format("%s %s:00", dateTo.getText(), timeTo.getText());

        Log.d("astro", "FROM: " + from);
        Log.d("astro", "TO: " + to);

        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //kontrola správně zadaného času
            if (dfDate.parse(to).before(dfDate.parse(from)) || dfDate.parse(to).equals(dfDate.parse(from))) {
                Toast.makeText(getApplicationContext(), "Čas začátku musí být menší než čas konce", Toast.LENGTH_LONG).show();
                Log.d("astro", "DATE ERROR start / end");
            }

        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), "Není zadáno datum", Toast.LENGTH_LONG).show();
            Log.d("astro", "DATE ERROR bad format");
        }







    }
}
