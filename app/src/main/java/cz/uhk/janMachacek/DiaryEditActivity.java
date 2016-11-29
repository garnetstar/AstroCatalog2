package cz.uhk.janMachacek;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.uhk.janMachacek.Model.DiaryObject;
import cz.uhk.janMachacek.UI.DatePicker;
import cz.uhk.janMachacek.UI.TimePicker;
import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Utils;

/**
 * @author Jan Macháček
 *         Created on 14.10.2016.
 */
public class DiaryEditActivity extends AbstactBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.diary_edit);
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

        String message = "Lat: " + Utils.getFormatedDegree(latitude) + " Lon: " + Utils.getFormatedDegree(longitude);

        Log.d("astro", "poloha=" + message);
        TextView form_location = (TextView) findViewById(R.id.form_location);
        form_location.setText(message);
        TextView hiddenLatitude = (TextView) findViewById(R.id.actual_latitude);
        hiddenLatitude.setText(Double.toString(latitude.getDecimalDegree()));

        TextView hiddenLongitude = (TextView) findViewById(R.id.actual_longitude);
        hiddenLongitude.setText(Double.toString(longitude.getDecimalDegree()));

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

        TextView dateFrom = (TextView) findViewById(R.id.dateFromDatabase);
        TextView timeFrom = (TextView) findViewById(R.id.timeFrom);

        TextView dateTo = (TextView) findViewById(R.id.dateToDatabase);
        TextView timeTo = (TextView) findViewById(R.id.timeTo);

        String from = String.format("%s %s:00", dateFrom.getText(), timeFrom.getText());
        String to = String.format("%s %s:00", dateTo.getText(), timeTo.getText());

        Log.d("astro", "FROM: " + from);
        Log.d("astro", "TO: " + to);

        // aktualni poloha
        TextView formLatitude = (TextView) findViewById(R.id.actual_latitude);
        String textLatitude = formLatitude.getText().toString();
        Double latitude = Double.parseDouble(textLatitude);

        TextView formLongitude = (TextView) findViewById(R.id.actual_longitude);
        String textLongitude = formLongitude.getText().toString();
        Double longitude = Double.parseDouble(textLongitude);


        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //kontrola správně zadaného času
            if (dfDate.parse(to).before(dfDate.parse(from)) || dfDate.parse(to).equals(dfDate.parse(from))) {
                Toast toast = Toast.makeText(getApplicationContext(), "Čas začátku musí být menší než čas konce", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();

                Log.d("astro", "DATE ERROR start / end");
            } else {

                // formular je OK, ulozit to DB
                DiaryObject val = new DiaryObject();
                val.setFrom(from);
                val.setTo(to);
                val.setLatitude(new Angle(latitude));
                val.setLognitude(new Angle(longitude));
                val.setSyncOk(0);

                // ulozeni pomoci contetnt provideru
                Uri uri = Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
                getContentResolver().insert(uri, val.getContentValues());
            }

        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), "Není zadáno datum", Toast.LENGTH_LONG).show();
            Log.d("astro", "DATE ERROR bad format");
        }

    }
}
