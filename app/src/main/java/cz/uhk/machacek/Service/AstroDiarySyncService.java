package cz.uhk.machacek.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cz.uhk.machacek.library.Synchronization.DiarySyncAdapter;

/**
 * @author Jan Macháček
 *         Created on 3.10.2016.
 */
public class AstroDiarySyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static DiarySyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new DiarySyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}