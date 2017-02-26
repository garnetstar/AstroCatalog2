package cz.uhk.janMachacek;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;

/**
 * Created by jan on 10.8.2016.
 * https://sites.google.com/site/andsamples/concept-of-syncadapter-androidcontentabstractthreadedsyncadapter
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    public final static String ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String AUTH_TYPE = "AUTH_TYPE";
    public final static String LOGIN = "LOGIN";
    public final static String PASSWORD = "PASSWORD";
    public final static String REFRESH_TOKEN = "REFRESH_TOKEN";
    public final static String WRONG_CREDENTIALS = "WRONG_CREDENTIALS";
    public final static int PERIOD_OF_SYNC_SEC = 30;
    public final static int KEY_SIGN_IN = 9001;

    public final static String NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager accountManager;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d("Response", "Auth activity onCreae()");
        setContentView(R.layout.act_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(AstroContract.API_CLIENT_ID)
                .build();
        // [END configure_signin]


        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
          //      .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        accountManager = AccountManager.get(getBaseContext());
        String accountName = getIntent().getStringExtra(LOGIN);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Log.d("astro", "LOGIN viewId = " + v.getId());
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
              //  signOut();
                break;
//            case R.id.disconnect_button:
//                revokeAccess();
//                break;
        }
    }

    private void signIn() {
        Log.d("astro", "SIGN > " + "signIn()");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, KEY_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == KEY_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("astro", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            TextView v = (TextView) findViewById(R.id.user_name);
            v.setText(acct.getDisplayName());


            Log.d("astro", "SIGN > " + "create accoutn");
            Log.d("astro", acct.getEmail());
            Log.d("astro", acct.getId());
            Log.d("astro", acct.getIdToken());





            String accountType = getIntent().getStringExtra(ACCOUNT_TYPE);
            String login = acct.getEmail();
            String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQwZjI2OGQxNTVmNDdlYWEyOTI1MTNmMzRkMTRlYjU0ZjdiYzIzYTQifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJpYXQiOjE0ODc3OTE4OTYsImV4cCI6MTQ4Nzc5NTQ5NiwiYXVkIjoiMTcxODE0Mzk3ODgyLXFvYWZib2RwaWQ1Mmg3bGgwcGM5OGJydWM5dnYxNnZzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTE2ODE2OTM4NjYwODI0MjY3MjQ5IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjE3MTgxNDM5Nzg4Mi1vZ3JtaDBvZ2Y5NTRsZWhubXFoa2tnZTBwYW44bXIyMy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImVtYWlsIjoibWFjaGFjZWsuakBnbWFpbC5jb20iLCJuYW1lIjoiSmFuIE1hY2jDocSNZWsiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDQuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1ldERGdEowV24zUS9BQUFBQUFBQUFBSS9BQUFBQUFBQURLby9BdTNjWDlFVnZpRS9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiSmFuIiwiZmFtaWx5X25hbWUiOiJNYWNow6HEjWVrIiwibG9jYWxlIjoiY3MifQ.arwxHDSevipkQeTA1ueQ4-jum4luh6ZKmHMXsku4rpMBlauA9DpFZS5P3Jk-8VgkyAywF2rzKOou6uwY_iVO3voiUxBDubY1j2VnmhhAnf-LYig05lggTQgoFOMVdRoiiFw0DocS_LxElf2CnYhVs_bnjxIwVE0yq6ANz_3SHwK86TMj9Fh2B99agkmkNm0qRpvEoGptL7dhYs-o_yBFSgri8o3y5rx2DzsguchZsCsmaBmsGQ8TFkOP693Xv2d4wB-5Iw6NwlfIDpDJTxnhrZujhXyx9rP4Z5xRBf4dRuucTiDI-ihrqNc_pIggCXWv1UEUiair35WSTWMqzggJBw";
                    //acct.getIdToken();
            Log.d("astro", "AccountType = " + accountType);


            Account account = new Account(login, accountType);
            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, login);

            accountManager.addAccountExplicitly(account, null, data);
            accountManager.setAuthToken(account, "baerer", token);
            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.DIARY_AUTHORITY, true);
            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.DIARY_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.CATALOG_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.CATALOG_AUTHORITY, true);



            Log.d("astro", "Call finish()");
            finish();
//            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
//            updateUI(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d("astro", "CONNECTION FAILED AUTENTICATOR ACTIVITY");
    }
}
