package cz.uhk.janMachacek;

import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.uhk.janMachacek.Exception.InvalidateRefreshTokenException;
import cz.uhk.janMachacek.Model.AstroDbHelper;
import cz.uhk.janMachacek.Model.DiaryFacade;
import cz.uhk.janMachacek.Model.DiaryObject;
import cz.uhk.janMachacek.UI.DatePicker;
import cz.uhk.janMachacek.UI.TimePicker;
import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Utils;

/**
 * @author Jan Macháček
 *         Created on 14.10.2016.
 */
public class DiaryEditActivity extends AbstactBaseActivity implements View.OnClickListener {

    /**
     * nastaveni modu true=novy zaznam, false=editace
     */
    private boolean isNew = true;

    private int id;

    private DiaryObject object;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.diary_edit);

        Intent intent = getIntent();

        if (null != intent) {

            this.id = intent.getIntExtra(AstroDbHelper.KEY_DIARY_ID, 0);

            if (id != 0) {
                isNew = false;
                DiaryFacade facade = new DiaryFacade(getContentResolver().acquireContentProviderClient(AstroContract
                        .DIARY_URI));

                try {
                    object = facade.getOneById(id);

                    //FROM
                    TextView dateFrom = (TextView) findViewById(R.id.dateFrom);
                    TextView dateFromDatabase = (TextView) findViewById(R.id.dateFromDatabase);
                    TextView timeFrom = (TextView) findViewById(R.id.timeFrom);
                    dateFrom.setText(object.getFromObject().getDateStringFormat());
                    timeFrom.setText(object.getFromObject().getTimeStringFormat());
                    dateFromDatabase.setText(object.getFromObject().getDateToString());

                    //TO
                    TextView dateTo = (TextView) findViewById(R.id.dateTo);
                    TextView timeTo = (TextView) findViewById(R.id.timeTo);
                    TextView dateToDatabase = (TextView) findViewById(R.id.dateToDatabase);
                    dateTo.setText(object.getToObject().getDateStringFormat());
                    timeTo.setText(object.getToObject().getTimeStringFormat());
                    dateToDatabase.setText(object.getToObject().getDateToString());

                    // POSSITON
                    TextView actualLatitude = (TextView) findViewById(R.id.actual_latitude);
                    actualLatitude.setText(Double.toString(object.getLatitude().getDecimalDegree()));
                    TextView actualLongitude = (TextView) findViewById(R.id.actual_longitude);
                    actualLongitude.setText(Double.toString(object.getLognitude().getDecimalDegree()));

                    String possition = "Lat: " + Utils.getFormatedDegree(object.getLatitude()) + " Lon: " + Utils.getFormatedDegree(object.getLognitude());
                    TextView form_location = (TextView) findViewById(R.id.form_location);
                    form_location.setText(possition);

                    if (!isNew) {
                        Button delete = new Button(this);
                        delete.setText("Smazat");
                        delete.setOnClickListener(this);
                        LinearLayout ll = (LinearLayout) findViewById(R.id.edit_diary_form);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        ll.addView(delete, lp);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }

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
        if (isNew) {
            Angle latitude = new Angle(location.getLatitude());
            Angle longitude = new Angle(location.getLongitude());

            String message = "Lat: " + Utils.getFormatedDegree(latitude) + " Lon: " + Utils.getFormatedDegree(longitude);

            TextView form_location = (TextView) findViewById(R.id.form_location);
            form_location.setText(message);
            TextView hiddenLatitude = (TextView) findViewById(R.id.actual_latitude);
            hiddenLatitude.setText(Double.toString(latitude.getDecimalDegree()));

            TextView hiddenLongitude = (TextView) findViewById(R.id.actual_longitude);
            hiddenLongitude.setText(Double.toString(longitude.getDecimalDegree()));

            hideProgressDialog();
        } else {
            //nastavit z databaze
            hideProgressDialog();
        }
    }

    public void showTimePickerDialogFrom(View view) {
        TimePicker newFragment = new TimePicker();
        newFragment.setView(view);
        if (isNew == false) {
            newFragment.setDate(object.getFrom());
        }
        newFragment.show(getFragmentManager(), "timePicker");

    }

    public void showTimePickerDialogTo(View view) {
        TimePicker newFragment = new TimePicker();
        newFragment.setView(view);
        if (isNew == false) {
            newFragment.setDate(object.getTo());
        }
        newFragment.show(getFragmentManager(), "timePicker");

    }

    public void showDatePickerDialogFrom(View view) {

        View hiddenFrom = (TextView) findViewById(R.id.dateFromDatabase);
        DatePicker newFragment = new DatePicker();
        newFragment.setViews(view, hiddenFrom);
        if (isNew == false) {
            newFragment.setDate(object.getFrom());
        }
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showDatePickerDialogTo(View view) {

        View hiddenTo = (TextView) findViewById(R.id.dateToDatabase);
        DatePicker newFragment = new DatePicker();
        newFragment.setViews(view, hiddenTo);
        if (isNew == false) {
            newFragment.setDate(object.getFrom());
        }
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void save(View view) {

        TextView dateFrom = (TextView) findViewById(R.id.dateFromDatabase);
        TextView timeFrom = (TextView) findViewById(R.id.timeFrom);

        TextView dateTo = (TextView) findViewById(R.id.dateToDatabase);
        TextView timeTo = (TextView) findViewById(R.id.timeTo);

        String from = String.format("%s %s:00", dateFrom.getText(), timeFrom.getText());
        String to = String.format("%s %s:00", dateTo.getText(), timeTo.getText());

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

                // ulozeni nebo update pomoci contetnt provideru
                Uri uri = Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
                if (isNew) {
                    getContentResolver().insert(uri, val.getContentValues());
                } else {
                    val.setId(this.object.getId());
                    val.setGuid(this.object.getGuid());

                    String where = AstroDbHelper.KEY_DIARY_ID + "=?";
                    String[] whereArgs = {Integer.toString(val.getId())};
                    getContentResolver().update(uri, val.getContentValues(), where, whereArgs);
                }
                //navrat na vypis objektu
                Intent intent = new Intent(this, DiaryActivity.class);
                startActivity(intent);
            }

        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), "Není zadáno datum", Toast.LENGTH_LONG).show();
            Log.d("astro", "DATE ERROR bad format");
        }
    }

    @Override
    public void onClick(View view) {
        Log.d("astro", "DELETE bude " + this.id);

        String selected = AstroDbHelper.KEY_DIARY_ID + "=?";
        String[] selectedArgs = {Integer.toString(this.id)};
        ContentValues cv = new ContentValues();
        cv.put(AstroDbHelper.KEY_DIARY_DELETED, 1);
        cv.put(AstroDbHelper.KEY_DIARY_SYNC_OK, 0);

        getContentResolver().update(getUri(), cv, selected, selectedArgs);
        //navrat na vypis objektu
        Intent intent = new Intent(this, DiaryActivity.class);
        startActivity(intent);
    }

    private Uri getUri() {
        return Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
    }
}