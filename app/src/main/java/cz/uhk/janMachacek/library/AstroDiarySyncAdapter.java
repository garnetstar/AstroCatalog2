package cz.uhk.janMachacek.library;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Jan Macháček
 *         Created on 12.10.2016.
 */
public class AstroDiarySyncAdapter extends AbstractThreadedSyncAdapter {

    public AstroDiarySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d("astro", "Synchronizace diary");
    }
}
