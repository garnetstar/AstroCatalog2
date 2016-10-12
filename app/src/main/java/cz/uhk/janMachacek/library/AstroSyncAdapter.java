package cz.uhk.janMachacek.library;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.R;
import cz.uhk.janMachacek.library.Sync.MessierData;

/**
 * @author Jan Macháček
 *         Created on 3.10.2016.
 */
public class AstroSyncAdapter extends AbstractThreadedSyncAdapter {
    private final AccountManager mAccountManager;

    public AstroSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d("astro", "onPerformSync for account[" + account.name + "]");
        try {
            // Get the auth token for the current account
            String authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);

            Log.d("astro", "ADAPter - " + authToken);

            //    ArrayList<AstroObject> messierData;
            MessierData md = new MessierData();

            try {
                // ziskat data ze serveru
                md.sync(authToken, getContext());
                //    messierData = md.getMessierData(authToken, getContext(), provider);
            } catch (AccessTokenExpiredException e) {
                // doslo k chybe, pokus o získání tokenu pomocí refresh tokenu nebo user credentials
                // AstroAccountAuthenticator->getAuthToken
                Log.d("astro", "EXPIRED Adapter");
                mAccountManager.invalidateAuthToken(getContext().getString(R.string.accountType), authToken);
                authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);
                // druhy pokus o ziskani dat ze serveru
                md.sync(authToken, getContext());
                //    messierData = md.getMessierData(authToken, getContext(), provider);
            }


//            if (messierData != null) {
//                Log.d("astro", "msdata = " + messierData.toString());
//            } else {
//                Log.d("astro", "nothing to update");
//            }


//            ParseComServerAccessor parseComService = new ParseComServerAccessor();
//
//            // Get shows from the remote server
//            List remoteTvShows = parseComService.getShows(authToken);
//
//            // Get shows from the local storage
//            ArrayList localTvShows = new ArrayList();
//            Cursor curTvShows = provider.query(TvShowsContract.CONTENT_URI, null, null, null, null);
//            if (curTvShows != null) {
//                while (curTvShows.moveToNext()) {
//                    localTvShows.add(TvShow.fromCursor(curTvShows));
//                }
//                curTvShows.close();
//            }
            // TODO See what Local shows are missing on Remote

            // TODO See what Remote shows are missing on Local

            // TODO Updating remote tv shows

            // TODO Updating local tv shows


        } catch (Exception e) {
            // pokud pri synchronizaci dojde k chybe, bude v telefonu synchronizace take ukoncena chybou
            syncResult.stats.numAuthExceptions++;

            Log.d("astro", "SyncAdapterError");
            Log.d("astro", e.toString());
            e.printStackTrace();
        }
    }
}
