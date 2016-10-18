package cz.uhk.janMachacek.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cz.uhk.janMachacek.Model.CatalogSyncAdapter;

/**
 * @author Jan Macháček
 *         Created on 3.10.2016.
 */
public class AstroSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static CatalogSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new CatalogSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}