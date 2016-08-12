package cz.uhk.janMachacek.library.Async;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

import cz.uhk.janMachacek.AbstactBaseActivity;
import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.EmptyCredentialsException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.ObjectListActivity;
import cz.uhk.janMachacek.library.Api.Facade;
import cz.uhk.janMachacek.library.Sync.MessierData;

/**
 * Created by jan on 2.8.2016.
 */
public class Synchronization extends AsyncTask {

    private Context applicationContext;
    private SharedPreferences preferences;
    private Facade apiFacade;

    public Synchronization(Context context, SharedPreferences preferences) {
        this.applicationContext = context;
        this.preferences = preferences;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        ConnectivityManager cmr = (ConnectivityManager) applicationContext.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmr.getActiveNetworkInfo();

        if (!(networkInfo != null && networkInfo.isConnected())) {
            sendStatus("Internetoré spojení není k dispozici, synchronizace nebude zahájena");
        } else {


            apiFacade = new Facade(preferences);

            sendStatus("Zahájena synchronizace");

            try {
                if (checkAccessToken()) {
                    sendStatus("AT is " + preferences.getString(Config.API_ACCESS_TOKEN, null));

                    Set<String> syncIssues = preferences.getStringSet("sync_issues", null);

                    if (syncIssues.contains("1")) {

                        sendStatus("Synchronizují se messier data");
                        MessierData messierData = new MessierData(apiFacade, applicationContext);
                        messierData.sync();
                        sendStatus("Messier data byla synchronizována");

                        Intent intent = new Intent();
                        intent.setAction(ObjectListActivity.REFRESH_OBJECTS_LIST);

                        Log.d("Response", "SEND BROADCAST");
                        applicationContext.sendBroadcast(intent);
                    }
                }
            } catch (ApiErrorException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        // sendStatus("OK finished");
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        String message = String.valueOf(values[0]);
        sendStatus(message);

    }

    private void sendStatus(String status) {
        Intent intent = new Intent();
        intent.setAction(AbstactBaseActivity.FILTER);
        Bundle bundle = new Bundle();
        bundle.putString("message", status);
        intent.putExtras(bundle);

        applicationContext.sendBroadcast(intent);

    }

    private boolean checkAccessToken() throws ApiErrorException {

        String accessToken = preferences.getString(Config.API_ACCESS_TOKEN, null);

        // pokud ještě nebyl použit access token
        if (null == accessToken) {

            try {
                apiFacade.getTokenByLogin();
            } catch (EmptyCredentialsException e) {
                sendStatus("Vyplňte přihlašovací jméno a heslo");
                return false;
            } catch (WrongCredentialsException e) {
                sendStatus("Přihlašovací údaje nejsou platné" + e.getMessage());
                return false;
            }
            return true;
        } else return true;
    }
}
