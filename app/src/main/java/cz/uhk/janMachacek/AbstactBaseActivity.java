package cz.uhk.janMachacek;


import cz.uhk.janMachacek.coordinates.Angle;
import cz.uhk.janMachacek.coordinates.Utils;
import cz.uhk.janMachacek.library.Async.Synchronization;
import cz.uhk.janMachacek.model.DataFacade;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.ExecutionException;





/**
 * Abstraktní activity implementující společné metody
 *
 * @author Jan Mach??ek
 */
abstract public class AbstactBaseActivity extends FragmentActivity implements
        LocationListener {

    public static final String FILTER = "astroIntent.broadcast";
    private Handler mHandler = new Handler();

    protected LocationManager locationManager;
    protected DataFacade facade;
    protected SharedPreferences preference;
    protected String locationProviderName = "none";

    protected ProgressDialog progresDialog;

    private android.accounts.AccountManager aManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nastaven? defaultn?ch settings
        PreferenceManager
                .setDefaultValues(this, R.xml.preference_screen, false);
        // pristup k preferencim
        preference = PreferenceManager.getDefaultSharedPreferences(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        facade = new DataFacade(this, getAssets());

    }

    protected void showProgressDialog(String title, String message) {
        this.progresDialog = ProgressDialog.show(this, title, message, true);
    }

    protected void hideProgressDialog() {
        try {
            this.progresDialog.dismiss();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    "ERROR: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    protected void findLocation() {
        try {
            showProgressDialog("", "Určování aktuální polohy");
            Criteria c = new Criteria();
            c.setAccuracy(Criteria.ACCURACY_COARSE);
            c.setPowerRequirement(Criteria.POWER_LOW);
            locationProviderName = locationManager.getBestProvider(c, true);
            locationManager.requestSingleUpdate(c, this, null);
        } catch (Exception e) {
            hideProgressDialog();
            Toast.makeText(getBaseContext(),
                    "ERROR: " + e.getMessage() + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    abstract public void onLocationChanged(Location location);

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        return;

    }

    @Override
    public void onProviderEnabled(String provider) {
        return;

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getBaseContext(),
                "ERROR: provider " + provider + " is disabled!",
                Toast.LENGTH_LONG).show();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(new MyReciever(), new IntentFilter(FILTER));

        if(!runn)
        startRepeatingTask();
    }

    protected void showLocation(Angle latitude, Angle longitude) {
        TextView location = (TextView) findViewById(R.id.actual_location);
        location.setText("Lat: " + Utils.getFormatedDegree(latitude) + " Lon: "
                + Utils.getFormatedDegree(longitude));
        TextView locatinoProvider = (TextView) findViewById(R.id.actual_location_provider);
        locatinoProvider.setText(locationProviderName);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(this);
    }

    public class MyReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
//				Toast.makeText(context, "Success - " + extras.getString("message"), Toast.LENGTH_LONG).show();
                TextView syncStatus = (TextView) findViewById(R.id.sync_status);
                syncStatus.setText(extras.getString("message"));
            }
        }
    }

    // !!! dulezita promenna, zajisti ze runnable job pojede jen v jedne instanci
    static boolean  runn = false;

    private Runnable mHandlerTask = new Runnable() {

        @Override
        public void run() {
            runn = true;
            try {
                Date date = new Date();
                Log.d("Response", "execution" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
                Synchronization sync = new Synchronization(getApplicationContext(), preference);
                sync.execute();
                mHandler.postDelayed(mHandlerTask, 30000);
            } catch (Exception e) {
                e.printStackTrace();
                runn = false;
            }

        }

    };

    void startRepeatingTask() {
        mHandlerTask.run();

    }

}
