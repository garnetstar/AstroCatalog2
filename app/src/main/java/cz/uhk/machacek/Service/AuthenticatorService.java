package cz.uhk.machacek.Service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import cz.uhk.machacek.AstroContract;
import cz.uhk.machacek.library.AstroAccountAuthenticator;

/**
 * @author Jan Macháček
 *         Created on 10.8.2016.
 */
public class AuthenticatorService extends Service implements OnAccountsUpdateListener {

    private AccountManager am;
    private Account[] _currentAccounts;

    @Override
    public IBinder onBind(Intent intent) {
        AstroAccountAuthenticator authenticator = new AstroAccountAuthenticator(this);
        return authenticator.getIBinder();
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        am = AccountManager.get(this);

        // set to true so we get the current list of accounts right away.
        am.addOnAccountsUpdatedListener(this, null, true);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        am.removeOnAccountsUpdatedListener(this);
    }

    /**
     * This invoked when the AccountManager starts up and whenever the account
     * set changes.
     *
     * @param accounts the current accounts
     */
    @Override
    public void onAccountsUpdated(Account[] accounts) {
        Log.d("astro", "UPDATE ACCOUNT FREOM SERVICE");

        if (accounts.length > 0) {
            Log.d("astro", "ACCOUNT> " + accounts[0].toString());
        } else {
            Log.d("astro", "NO ACCOUNT");
        }

        // NOTE: this is every account on the device (you may want to filter by type)
        if (_currentAccounts == null) {
            _currentAccounts = accounts;
            return;
        }

        for (Account currentAccount : _currentAccounts) {
            boolean accountExists = false;
            for (Account account : accounts) {
                if (account.equals(currentAccount)) {
                    accountExists = true;
                    break;
                }
            }

            if (!accountExists) {

                Log.d("astro", "DELETE ACCOUNT DATE");
                Uri uri = Uri.parse(AstroContract.DIARY_URI + "/delete_all");

                //smazat vse
                getContentResolver().delete(uri, null, null);
                // Take actions to clean up.  Maybe send intent on Local Broadcast reciever
                Log.d("astro", "DELETE ACCOUNT DATE OK");

                //revoke account
            }
        }

//        getContentResolver().
    }
}
