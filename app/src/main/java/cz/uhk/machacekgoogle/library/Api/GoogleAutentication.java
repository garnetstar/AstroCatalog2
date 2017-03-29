package cz.uhk.machacekgoogle.library.Api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import cz.uhk.machacekgoogle.AstroContract;
import cz.uhk.machacekgoogle.AuthenticatorActivity;

/**
 * @author Jan Macháček
 *         Created on 28.2.2017.
 */
public class GoogleAutentication {

    public interface UserCreatedCallback {
        public void onUserCreated(Object o);
    }


    private AccountManager accountManager;

    public GoogleAutentication(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void createUser(GoogleSignInAccount acct, UserCreatedCallback callback, ContentResolver contentResolver) {
        Log.d("astro", "CREATE USER " + acct.getDisplayName() + acct.getEmail());

        CallGoogleApiForTokens job = new CallGoogleApiForTokens(callback, contentResolver, acct, accountManager);
        job.execute();

    }

    private class CallGoogleApiForTokens extends AsyncTask<String, Void, String> {

        private GoogleAutentication.UserCreatedCallback callback;
        private TokenResponse tr;
        private ContentResolver contentResolver;
        private GoogleSignInAccount acct;
        private AccountManager accountManager;

        public CallGoogleApiForTokens(UserCreatedCallback callback, ContentResolver contentResolver, GoogleSignInAccount acct, AccountManager accountManager) {
            this.callback = callback;
            this.contentResolver = contentResolver;
            this.acct = acct;
            this.accountManager = accountManager;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         */
        @Override
        protected String doInBackground(String... params) {
            String serverAuthCode = acct.getServerAuthCode();
            GoogleTokenResponse tokenResponse = null;

           Log.d("astro", "Jackson " + JacksonFactory.getDefaultInstance());

            try {
                tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        "https://www.googleapis.com/oauth2/v4/token",
                        "171814397882-qoafbodpid52h7lh0pc98bruc9vv16vs.apps.googleusercontent.com",
                        "zN_3GYmPfnSAnlz0ChErRI4M",
                        serverAuthCode,
                        "").execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("astro", "sdfsf " + e.toString());
            }

            Log.d("astro", "ACCESS_TOKEN " + tokenResponse.getAccessToken());
            Log.d("astro", "REFRESH_TOKEN " + tokenResponse.getRefreshToken());
            Log.d("astro", "ID_TOKEN " + tokenResponse.getIdToken());

            //vytvoření uživatele
            Account account = new Account(acct.getEmail(), "cz.uhk.machacekgoogle.astroCatalog");
            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, acct.getEmail());
            data.putString(AuthenticatorActivity.REFRESH_TOKEN, tokenResponse.getRefreshToken());
            data.putString(AuthenticatorActivity.ID_TOKEN, tokenResponse.getIdToken());


            accountManager.addAccountExplicitly(account, null, data);
            accountManager.setAuthToken(account, "baerer", tokenResponse.getAccessToken());
            contentResolver.setSyncAutomatically(account, AstroContract.DIARY_AUTHORITY, true);
            contentResolver.addPeriodicSync(account, AstroContract.DIARY_AUTHORITY, new Bundle(), AuthenticatorActivity.PERIOD_OF_SYNC_SEC);
            contentResolver.addPeriodicSync(account, AstroContract.CATALOG_AUTHORITY, new Bundle(), AuthenticatorActivity.PERIOD_OF_SYNC_SEC);
            contentResolver.setSyncAutomatically(account, AstroContract.CATALOG_AUTHORITY, true);

            Log.d("astro", "User has been created");
            Log.d("astro", "revoke https://accounts.google.com/o/oauth2/revoke?token=" + tokenResponse.getAccessToken());


            tr = tokenResponse;
            return null;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param s The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onUserCreated(tr);

        }
    }


}
