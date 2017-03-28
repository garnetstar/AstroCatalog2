package cz.uhk.machacek;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.uhk.machacek.Model.AstroDbHelper;
import cz.uhk.machacek.Model.DiaryFacade;
import cz.uhk.machacek.Model.DiaryObject;
import cz.uhk.machacek.UI.DatePicker;
import cz.uhk.machacek.UI.TimePicker;
import cz.uhk.machacek.coordinates.Angle;
import cz.uhk.machacek.coordinates.Timer;
import cz.uhk.machacek.library.Api.Http.Response;


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

    protected TextView weather;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.diary_edit);
        Intent intent = getIntent();

        if (null != intent) {
            this.id = intent.getIntExtra(AstroDbHelper.KEY_DIARY_ID, 0);
            if (id != 0) {
                isNew = false;
                DiaryFacade facade = new DiaryFacade(getContentResolver().acquireContentProviderClient(AstroContract.DIARY_URI));
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
                    TextView actualLatitude = (TextView) findViewById(R.id.lat);
                    actualLatitude.setText(Double.toString(object.getLatitude().getDecimalDegree()));
                    TextView actualLongitude = (TextView) findViewById(R.id.lon);
                    actualLongitude.setText(Double.toString(object.getLongitude().getDecimalDegree()));

                    //weather
                    TextView weather = (TextView) findViewById(R.id.weather);
                    weather.setText(object.getWeather());

                    //log
                    TextView log = (TextView) findViewById(R.id.log);
                    log.setText(object.getLog());

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

    @Override
    protected void onResume() {
        super.onResume();
        findLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isNew) {
            Log.d("astro", "LOCATION - zjištěna poloha");

            Angle latitude = new Angle(location.getLatitude());
            Angle longitude = new Angle(location.getLongitude());

            TextView hiddenLatitude = (TextView) findViewById(R.id.lat);
            hiddenLatitude.setText(Double.toString(latitude.getDecimalDegree()));

            TextView hiddenLongitude = (TextView) findViewById(R.id.lon);
            hiddenLongitude.setText(Double.toString(longitude.getDecimalDegree()));

            //pokud je k dispozici internetové spojení, načíst data počasí z API
            ConnectivityManager cmr =
                    (ConnectivityManager) getBaseContext().getSystemService( Activity.CONNECTIVITY_SERVICE );
            NetworkInfo networkInfo = cmr.getActiveNetworkInfo();

            if (!(networkInfo != null && networkInfo.isConnected())) {
                Log.d("astro", "Internetoré spojení není k dispozici");
            } else {
                // získat data počasí
                WeatherDataJob job = new WeatherDataJob(latitude.getDecimalDegree(), longitude.getDecimalDegree());
                job.execute();
            }

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

    public void save(View view) throws RemoteException {

        TextView dateFrom = (TextView) findViewById(R.id.dateFromDatabase);
        TextView timeFrom = (TextView) findViewById(R.id.timeFrom);

        TextView dateTo = (TextView) findViewById(R.id.dateToDatabase);
        TextView timeTo = (TextView) findViewById(R.id.timeTo);

        String from = String.format("%s %s:00", dateFrom.getText(), timeFrom.getText());
        String to = String.format("%s %s:00", dateTo.getText(), timeTo.getText());

        // aktualni poloha
        TextView formLatitude = (TextView) findViewById(R.id.lat);
        String textLatitude = formLatitude.getText().toString();
        Double latitude = Double.parseDouble(textLatitude);

        TextView formLongitude = (TextView) findViewById(R.id.lon);
        String textLongitude = formLongitude.getText().toString();
        Double longitude = Double.parseDouble(textLongitude);

        //weather
        TextView formWeather = (TextView) findViewById(R.id.weather);
        String weather = formWeather.getText().toString();

        // log
        TextView formLog = (TextView) findViewById(R.id.log);
        String log = formLog.getText().toString();

        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //kontrola správně zadaného času
            if (dfDate.parse(to).before(dfDate.parse(from)) || dfDate.parse(to).equals(dfDate.parse(from))) {

                showAlert("Čas začátku musí být menší než čas konce");
                Log.d("astro", "DATE ERROR start / end");
            } else {

                // formular je OK, ulozit to DB
                DiaryObject val = new DiaryObject();
                val.setFrom(from);
                val.setTo(to);
                val.setLatitude(new Angle(latitude));
                val.setLongitude(new Angle(longitude));
                val.setWeather(weather);
                val.setLog(log);
                val.setSyncOk(0);
                // pri vložení nového záznamu i při updatu je třeba nastavit aktuální timestamp
                val.setTimestamp(Timer.getTimestamp());


                // ulozeni nebo update pomoci contetnt provideru
                Uri uri = Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
                if (isNew) {
                    getContentResolver().insert(uri, val.getContentValues());
                    //navrat na vypis objektu
                    Intent intent = new Intent(this, DiaryActivity.class);
                    startActivity(intent);
                } else {

                    int actualServerCounter = getActualServerCounter(this.object.getId());
                    Log.d("astro", "PREVENT BEFORE CONFLICT: object>" + this.object.getRowCounter() + " actual>" + actualServerCounter);
                    if (this.object.getRowCounter() < actualServerCounter) {
                        showAlert("Záznam nelze editovat, byl aktualizován serverem");

                    } else {
                        val.setId(this.object.getId());
                        val.setGuid(this.object.getGuid());
                        val.setRowCounter(this.object.getRowCounter());
                        Log.d("astro", "Save Row Counter:" + this.object.getRowCounter());
                        String where = AstroDbHelper.KEY_DIARY_ID + "=?";
                        String[] whereArgs = {Integer.toString(val.getId())};
                        getContentResolver().update(uri, val.getContentValues(), where, whereArgs);
                        //navrat na vypis objektu
                        Intent intent = new Intent(this, DiaryActivity.class);
                        startActivity(intent);
                    }
                }



            }

        } catch (ParseException e) {
            showAlert( "Není zadáno datum");
            Log.d("astro", "DATE ERROR bad format");
        }
    }


    @Override
    public void onClick(View view) {

        String selected = AstroDbHelper.KEY_DIARY_ID + "=?";
        String[] selectedArgs = {Integer.toString(this.id)};
        ContentValues cv = new ContentValues();
        cv.put(AstroDbHelper.KEY_DIARY_DELETED, 1);
        cv.put(AstroDbHelper.KEY_DIARY_SYNC_OK, 0);

        getContentResolver().update(getUri(), cv, selected, selectedArgs);
        //navrat na vypis objektu
        back();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        back();

    }

    private void back() {
        //navrat na vypis objektu
        Intent intent = new Intent(this, DiaryActivity.class);
        startActivity(intent);
    }

    private Uri getUri() {
        return Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
    }

    public int getActualServerCounter(int id) throws RemoteException {
        DiaryFacade facade = new DiaryFacade(getContentResolver().acquireContentProviderClient(AstroContract
                .DIARY_URI));
        DiaryObject actualObject = facade.getOneById(id);
        return actualObject.getRowCounter();
    }


    /**
     * POcasi
     */
    private class WeatherDataJob extends AsyncTask<String, Void, String> {

        private double lat, lon;
        private String message;

        public WeatherDataJob(double lat, double lon) {
            super();
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected String doInBackground(String[] params) {


            try {
                JSONObject json = Response.getWeatherData(lat, lon);
                JSONObject main = json.getJSONObject("main");
                String temp = main.getString("temp");
                String humidity = main.getString("humidity");
                String pressure = main.getString("pressure");

                JSONArray weather = json.getJSONArray("weather");
                String weatherMain = weather.getJSONObject(0).getString("main");
                String description = weather.getJSONObject(0).getString("description");

                this.message = weatherMain + ", " + description + ", " + temp + "°C"
                        + ", " + "humidity: " + humidity + "%, "
                        + "pressure: " + pressure + "hPa";

                Log.d("astro", message);


//                URL url = null;
//                try {
//                    url = new URL("http://openweathermap.org/img/w/10d.png");
//                    try {
//                        bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }


            } catch (IOException e) {
                Log.d("astro", "ERROR 123" + e.toString());
            } catch (JSONException e) {
                Log.d("astro", "ERROR 124" + e.toString());
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String message) {
            TextView weather = (TextView) findViewById(R.id.weather);
            weather.setText(this.message);

        }
    }

    private void showAlert(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View toastRoot = inflater.inflate(R.layout.toast, null);
        Toast toast = new Toast(getBaseContext());
        toast.setView(toastRoot);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.CENTER,
                0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        TextView toastMessage = (TextView) toastRoot.findViewById(R.id.toastMessage);
        toastMessage.setText(message);
        toast.show();
    }

}
