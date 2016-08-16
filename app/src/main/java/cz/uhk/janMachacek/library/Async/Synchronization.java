package cz.uhk.janMachacek.library.Async;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.Set;

import cz.uhk.janMachacek.AbstactBaseActivity;
import cz.uhk.janMachacek.AuthenticatorActivity;
import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.EmptyCredentialsException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.ObjectListActivity;
import cz.uhk.janMachacek.R;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;
import cz.uhk.janMachacek.library.Sync.MessierData;

/**
 * Created by jan on 2.8.2016.
 */
public class Synchronization extends AsyncTask {

    private Context applicationContext;
    private SharedPreferences preferences;
    private ApiAuthenticator apiAuthenticator;

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


            apiAuthenticator = new ApiAuthenticator(preferences);

            final AccountManager am = AccountManager.get(applicationContext);
            final String authTok;
            final Account[] accounts = am.getAccountsByType(applicationContext.getString(R.string.accountType));
            Log.d("Response", "Accounts " + Integer.toString(accounts.length));

            if (accounts.length > 0) {
                Bundle options = new Bundle();
                am.getAuthToken(accounts[0], "baerer", options, true, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                        Bundle bundle;
                        try {

                            bundle = accountManagerFuture.getResult();
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                            return;
                        }
                        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                        if (authToken != null) {
                            SyncMessier sm = new SyncMessier(am);
                            sm.execute(new String[]{authToken});
                        }
                    }
                }, null);
            }
        }

        return null;
    }

    private class SyncMessier extends AsyncTask {
        private AccountManager accountManager;

        public SyncMessier(AccountManager accountManager) {
            this.accountManager = accountManager;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            String accessToken = objects[0].toString();
            Log.d("astro", "SyncMessier");
            MessierData ms = new MessierData();
            try {
                sendStatus("Probíhá synchronizace Messierova katalogu");
                ms.sync(accessToken, applicationContext);
//                Intent intent = new Intent();
//                intent.setAction(ObjectListActivity.REFRESH_OBJECTS_LIST);

                sendStatus("Synchronizace Messierova katalogu dokončena");

           //     Log.d("Response", "SEND BROADCAST");
            //    applicationContext.sendBroadcast(intent);
            } catch (AccessTokenExpiredException e) {

                Log.d("astro", "INVALIDATE: " + accessToken);
                accountManager.invalidateAuthToken(applicationContext.getString(R.string.accountType), accessToken);
                e.printStackTrace();
            }
            return null;
        }
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
}
