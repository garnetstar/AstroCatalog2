package cz.uhk.machacek;


import cz.uhk.machacek.coordinates.Angle;
import cz.uhk.machacek.coordinates.Utils;
import cz.uhk.machacek.Model.ObjectDataFacade;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Abstraktní activity implementující společné metody
 *
 * @author Jan Mach??ek
 */
abstract public class AbstactBaseActivity extends FragmentActivity implements
        LocationListener {

    public static final String FILTER_SHOW_MESSAGE = "astroIntent.show_message";

    protected LocationManager locationManager;
    protected ObjectDataFacade facade;
    protected SharedPreferences preference;
    protected String locationProviderName = "none";

    protected ProgressDialog progresDialog;

    private android.accounts.AccountManager aManager;
    private AccountManager mAccountManager;
    protected Account account;

    private final SyncReceiver messageReceiver = new SyncReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // nastaven? defaultn?ch settings
        PreferenceManager
                .setDefaultValues(this, R.xml.preference_screen, false);
        // pristup k preferencim
        preference = PreferenceManager.getDefaultSharedPreferences(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        facade = new ObjectDataFacade(this, getAssets());

        mAccountManager = AccountManager.get(this);
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
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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

        if (numberOfAccounts() < 1) {
            final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(getBaseContext().getString(R.string.accountType), "baerer", null, null, this, null, null);
        }
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }

    public class MyReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                TextView syncStatus = (TextView) findViewById(R.id.sync_status);
                syncStatus.setText(extras.getString("message"));
            }
        }
    }

    public class SyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String message = "RESUL SYNCHRONIZACE:\n" + extras.getString("message");
                Log.d("astro",message );

                LayoutInflater inflater = getLayoutInflater();
                View toastRoot = inflater.inflate(R.layout.toast, null);
                Toast toast = new Toast(context);
                toast.setView(toastRoot);
                toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM,
                        0, 0);
                toast.setDuration(Toast.LENGTH_LONG);
                TextView toastMessage = (TextView) toastRoot.findViewById(R.id.toastMessage);
                toastMessage.setText(message);
                toast.show();

//                showNotification(message, getBaseContext());
            }
        }
    }

    private int numberOfAccounts() {
        Account[] accounts = mAccountManager.getAccountsByType(getBaseContext().getString(R.string.accountType));
        if(accounts.length > 0) {
            this.account = accounts[0];
        }
        return accounts.length;
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(messageReceiver, new IntentFilter(FILTER_SHOW_MESSAGE));
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
    }

//    private void showNotification(String eventtext, Context ctx) {
//
//        // Set the icon, scrolling text and timestamp
//        Notification notification = new Notification(R.drawable.common_full_open_on_phone,
//                eventtext, System.currentTimeMillis());
//
//        // The PendingIntent to launch our activity if the user selects this
//        // notification
//        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
//                new Intent(ctx, HomePage.class), 0);
//
//        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(ctx, "AstroCatalog", eventtext,
//                contentIntent);
//
//        // Send the notification.
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify("Title", 0, notification);
//    }

}