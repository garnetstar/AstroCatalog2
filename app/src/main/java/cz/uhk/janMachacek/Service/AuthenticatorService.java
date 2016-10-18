package cz.uhk.janMachacek.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import cz.uhk.janMachacek.library.AstroAccountAuthenticator;

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
