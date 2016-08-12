package cz.uhk.janMachacek.library;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by jan on 10.8.2016.
 */
public class AstroAuthService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AstroAuthenticator authenticator = new AstroAuthenticator(this);
        return authenticator.getIBinder();
    }
}
