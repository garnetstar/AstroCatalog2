package cz.uhk.machacekgoogle;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

/**
 * Created by jan on 10.8.2016.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public final static String ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String AUTH_TYPE = "AUTH_TYPE";
    public final static String LOGIN = "LOGIN";
    public final static String REFRESH_TOKEN = "REFRESH_TOKEN";
    public final static String ID_TOKEN = "ID_TOKEN";
    public final static String NAME = "NAME";
    public final static String PICTURE = "PICTURE";

    public final static int PERIOD_OF_SYNC_SEC = 30;
    public final static int KEY_SIGN_IN = 9001;

    public final static String NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager accountManager;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d("Response", "SPUŠTĚNÍ AUTENTICATION ACTIVITY");
        setContentView(R.layout.act_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // ziskani údajů uživatelova účtu
                .requestScopes(new Scope(Scopes.PLUS_ME))
                //nastavení api_client_id a požadavku na získání autorizačního kódu pro offline přístup
                .requestServerAuthCode(AstroContract.API_CLIENT_ID, true)
                .requestEmail()
                .build();

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
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Log.d("astro", "ZOBRAZENÍ PŘIHLAŠOVACÍHO FORMULÁŘE");
        showProgressDialog();
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
        if (result.isSuccess()) {
            Log.d("astro", "ÚSPĚŠNÉ PŘIHLÁŠENÍ KE GoogleSignInApi");
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();

            Log.d("astro", "SPUŠTĚNÍ ASYNCHRONÍHO VLÁKNA K VYTVOŘENÍ UŽIVATELE "+ googleSignInAccount.getEmail());
            Log.d("astro", "SERVER_AUTH_CODE="+ googleSignInAccount.getServerAuthCode());
            CreateUserTask job = new CreateUserTask(googleSignInAccount, accountManager);
            job.execute(googleSignInAccount.getServerAuthCode());

        } else {
            hideProgressDialog();
           Log.d("astro", "NEPODAŘILO SE PŘIHLÁSIT KE GoogleSignInApi");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        hideProgressDialog();
        Log.d("astro", "CONNECTION FAILED AUTENTICATOR ACTIVITY");
    }

    private class CreateUserTask extends AsyncTask<String, Void, String> {
        private GoogleSignInAccount googleSignInAccount;
        private AccountManager accountManager;
        private GoogleTokenResponse googleTokenResponse;

        public CreateUserTask(GoogleSignInAccount acct, AccountManager accountManager) {
            this.googleSignInAccount = acct;
            this.accountManager = accountManager;
        }

        @Override
        protected String doInBackground(String... params) {

            String serverAuthCode = params[0];
            try {
                googleTokenResponse = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        "https://www.googleapis.com/oauth2/v4/token",
                        "171814397882-qoafbodpid52h7lh0pc98bruc9vv16vs.apps.googleusercontent.com",
                        "zN_3GYmPfnSAnlz0ChErRI4M",
                        serverAuthCode,
                        "").execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("astro", "NEPODAŘILO SE ZÍSKAT AUTORIZAČNÍ TOKENY " + e.toString());
            }

            Log.d("astro", "------------------------------------");
            Log.d("astro", "ÚSPĚŠNÉ ZÍSKÁNÍ AUTORIZAČNÍCH TOKENŮ");
            Log.d("astro", "ACCESS_TOKEN=" + googleTokenResponse.getAccessToken());
            Log.d("astro", "REFRESH_TOKEN=" + googleTokenResponse.getRefreshToken());
            Log.d("astro", "ID_TOKEN=" + googleTokenResponse.getIdToken());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String accountType = getIntent().getStringExtra(ACCOUNT_TYPE);
            String login = googleSignInAccount.getEmail();
            String authToken = googleTokenResponse.getAccessToken();

            // Vytvoření nového účtu
            Account account = new Account(login, accountType);
            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, login);
            data.putString(REFRESH_TOKEN, googleTokenResponse.getRefreshToken());
            data.putString(ID_TOKEN, googleTokenResponse.getIdToken());
//            data.putString(ID_TOKEN, "eyJhbGciOiJSUzI1NiIsImtpZCI6IjgxMDkxNGZiOTk0OGYxZTQzNTdjYzg3MjY4MDg3Mjk4ZTgzNTlkMjAifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJpYXQiOjE0ODgxODg4OTMsImV4cCI6MTQ4ODE5MjQ5MywiYXVkIjoiMTcxODE0Mzk3ODgyLXFvYWZib2RwaWQ1Mmg3bGgwcGM5OGJydWM5dnYxNnZzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTE2ODE2OTM4NjYwODI0MjY3MjQ5IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF6cCI6IjE3MTgxNDM5Nzg4Mi1vZ3JtaDBvZ2Y5NTRsZWhubXFoa2tnZTBwYW44bXIyMy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImVtYWlsIjoibWFjaGFjZWsuakBnbWFpbC5jb20iLCJuYW1lIjoiSmFuIE1hY2jDocSNZWsiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDQuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1ldERGdEowV24zUS9BQUFBQUFBQUFBSS9BQUFBQUFBQURLby9BdTNjWDlFVnZpRS9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiSmFuIiwiZmFtaWx5X25hbWUiOiJNYWNow6HEjWVrIiwibG9jYWxlIjoiY3MifQ.JRRRa3dCmpfddK7DrymCjZ1XC3P7U9q9NQMDMiQAkaEIMWNnd4_rHVspzhWe3UQx-wfWVu11hJVEoWKtMNQMISEmsZACQxnvNe7x1QyUcDSXzXLSMFCWWxUOtyrPp6VJLrMcZlV2cH8NDTSaZlv9loQjIZbBhpp70seYAoxuLMbGp8JL7HNRdyo_0AG421csKRkNOPlGYp3qJLDOYgKlRJ84BSANapqXamWsIU_K7ik6nvWi0ha5pZEG12iPVQ6wSHYtPxPDiZKmHuQjNoArFXOeOuqGKT6E18VjXG3WjVaNJokrAqDTCkmXdAF9fa-85qMpLDhKORC_FveMOZlxwg");
            data.putString(NAME, googleSignInAccount.getDisplayName());
            data.putString(PICTURE, String.valueOf(googleSignInAccount.getPhotoUrl()));

            accountManager.addAccountExplicitly(account, null, data);
            accountManager.setAuthToken(account, "baerer", authToken);

            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.DIARY_AUTHORITY, true);
            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.DIARY_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.CATALOG_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.CATALOG_AUTHORITY, true);

            Log.d("astro", "UKONČENÍ AUTENTICATION_ACTIVITY");
            Log.d("astro", "-------------------------------");

            setAccountAuthenticatorResult(data);
            finish();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }
}
