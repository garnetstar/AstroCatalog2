package cz.uhk.machacekgoogle.library.Synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import cz.uhk.machacekgoogle.AuthenticatorActivity;
import cz.uhk.machacekgoogle.Exception.AccessTokenExpiredException;
//import cz.uhk.machacekgoogle.R;
import cz.uhk.machacekgoogle.Model.AstroObject;
import cz.uhk.machacekgoogle.ObjectListActivity;
import cz.uhk.machacekgoogle.R;
import cz.uhk.machacekgoogle.AstroContract;

/**
 * @author Jan Macháček
 *         Created on 3.10.2016.
 */
public class CatalogSyncAdapter extends AbstractThreadedSyncAdapter {
    private final AccountManager mAccountManager;

    public CatalogSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("astro", "onPerformSync for account[" + account.name + "]");
        try {
            // Get the auth token for the current account
//            String idToken = mAccountManager.getUserData(account, AuthenticatorActivity.ID_TOKEN);
//            String authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);
//
//            ArrayList<AstroObject> messierData = null;
//            MessierData md = new MessierData();
//
//            try {
//                // ziskat data ze serveru
//                // md.sync(authToken, getContext());
//                messierData = md.getMessierData(idToken, getContext(), provider);
//            } catch (AccessTokenExpiredException e) {
//                // doslo k chybe, pokus o získání tokenu pomocí refresh tokenu nebo user credentials
//                // AstroAccountAuthenticator->getAuthToken
//                Log.d("astro", "EXPIRED Adapter");
//                mAccountManager.invalidateAuthToken(getContext().getString(R.string.accountType), authToken);
//                syncResult.fullSyncRequested=true;
//            }

            MessierData md = new MessierData();
            String authToken;
            ArrayList<AstroObject> messierData = null;
            String signType = mAccountManager.getUserData(account, AuthenticatorActivity.SIGN_IN_TYPE);
            switch (signType) {
                case AuthenticatorActivity.SIGN_IN_TYPE_GOOGLE:
                    String idToken = mAccountManager.getUserData(account, AuthenticatorActivity.ID_TOKEN);
                    authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);


                    try {
                        // ziskat data ze serveru
                        // md.sync(authToken, getContext());
                        messierData = md.getMessierData(idToken, getContext(), provider);
                    } catch (AccessTokenExpiredException e) {
                        // doslo k chybe, pokus o získání tokenu pomocí refresh tokenu nebo user credentials
                        // AstroAccountAuthenticator->getAuthToken
                        Log.d("astro", "EXPIRED Adapter");
                        mAccountManager.invalidateAuthToken(getContext().getString(R.string.accountType), authToken);
                        syncResult.fullSyncRequested = true;
                    }
                    break;
                case AuthenticatorActivity.SIGN_IN_TYPE_ASTRO:
                    authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);

                    Log.d("astro", "ADAPter - " + authToken);

                    try {
                        // ziskat data ze serveru
                        // md.sync(authToken, getContext());
                        messierData = md.getMessierData(authToken, getContext(), provider);
                    } catch (AccessTokenExpiredException e) {
                        // doslo k chybe, pokus o získání tokenu pomocí refresh tokenu nebo user credentials
                        // AstroAccountAuthenticator->getAuthToken
                        Log.d("astro", "EXPIRED Adapter");
                        mAccountManager.invalidateAuthToken(getContext().getString(R.string.accountType), authToken);
                        authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);
                        // druhy pokus o ziskani dat ze serveru
                        // md.sync(authToken, getContext());
                        messierData = md.getMessierData(authToken, getContext(), provider);
                    }
                    break;
                default:
                    Log.d("astro", "ERROR: NO SYNC ACCOUNT TYPE in MESSIER");
                    break;
            }


            // pokud prisla nejaka data
            if (messierData != null) {
                //smazat vse
                provider.delete(Uri.parse(AstroContract.CATALOG_URI + "/messier"), null, null);

                // Vlozit data ziskana ze serveru
                int i = 0;
                ContentValues newValues[] = new ContentValues[messierData.size()];
                for (AstroObject oneValue : messierData) {
                    newValues[i++] = oneValue.getContentValues();
                }

                provider.bulkInsert(Uri.parse(AstroContract.CATALOG_URI + "/messier"), newValues);
                Log.d("astro", "UPDATED msdata = " + messierData.toString());

                //aktualizovat verzi katalogu
                // default preference
                SharedPreferences preferences = getContext().getSharedPreferences(getContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                editor.commit();
                Log.d("astro", "set new version " + md.getActualVersion());

                //zobrazit aktuální data
                Intent intent = new Intent();
                intent.setAction(ObjectListActivity.REFRESH_OBJECTS_LIST);
                Log.d("Response", "SEND BROADCAST *");
                getContext().sendBroadcast(intent);

            } else {
                Log.d("astro", "nothing to update");
            }
        } catch (Exception e) {
            // pokud pri synchronizaci dojde k chybe, bude v telefonu synchronizace take ukoncena chybou
            syncResult.stats.numAuthExceptions++;

            Log.d("astro", "SyncAdapterError");
            Log.d("astro", e.toString());
            e.printStackTrace();
        }
    }
}
