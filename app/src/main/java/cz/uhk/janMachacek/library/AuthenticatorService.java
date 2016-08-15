package cz.uhk.janMachacek.library;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Jan Macháček
 * Created on 10.8.2016.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        AstroAccountAuthenticator authenticator = new AstroAccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
