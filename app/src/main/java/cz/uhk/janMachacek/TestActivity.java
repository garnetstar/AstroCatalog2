package cz.uhk.janMachacek;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.EmptyCredentialsException;
import cz.uhk.janMachacek.UI.AlertFragment;
import cz.uhk.janMachacek.UI.AlertMessageFragment;
import cz.uhk.janMachacek.library.Sync.MessierData;
import cz.uhk.janMachacek.model.Connector;

public class TestActivity extends AbstactBaseActivity implements AlertFragment.NoticeDialogListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Connector connector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //connecctor
        connector = new Connector(preference);

        TextView v = (TextView) findViewById(R.id.testTextView);
        TextView statusCode = (TextView) findViewById(R.id.statusCode);
        TextView tokenView = (TextView) findViewById(R.id.token);
        String info = new String("info ze aplikace bezi");

        ProgressDialog pDialog = new ProgressDialog(TestActivity.this);
        pDialog.setMessage("pockejj pripojuiji se");
        pDialog.show();

        ConnectivityManager cmr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // spusteni na pozadi, v jinem vlakne
            Sync sync = new Sync();
            sync.execute();
            pDialog.hide();
        } else {
            info = "chyba site";
            Toast.makeText(getBaseContext(),
                    "INFO: " + info,
                    Toast.LENGTH_LONG).show();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // ulozeni credentials predanych fragment dialogem
    @Override
    public void onDialogPositiveClick(AlertFragment dialog) {

        String login = dialog.getLogin();
        String password = dialog.getPassword();

        SharedPreferences.Editor editor = preference.edit();
        editor.putString(Config.API_LOGIN, login);
        editor.putString(Config.API_PASSWORD, password);
        editor.commit();

        Sync sync = new Sync();
        sync.execute();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Toast.makeText(getBaseContext(),
                "negative",
                Toast.LENGTH_LONG).show();
    }

    private class Sync extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {

            MessierData syncMessierData = new MessierData(connector);

            try {
                syncMessierData.sync();
            } catch (EmptyCredentialsException e) {
                AlertFragment alert = AlertFragment.newInstance();
                alert.show(getFragmentManager(), "alert");
            }
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Test Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cz.uhk.janMachacek/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Test Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://cz.uhk.janMachacek/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
