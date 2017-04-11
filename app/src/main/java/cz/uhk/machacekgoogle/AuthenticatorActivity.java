package cz.uhk.machacekgoogle;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import cz.uhk.machacekgoogle.Exception.ApiErrorException;
import cz.uhk.machacekgoogle.Exception.WrongCredentialsException;
import cz.uhk.machacekgoogle.library.Api.ApiAuthenticator;

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
    public final static String PASSWORD = "PASSWORD";
    public final static String WRONG_CREDENTIALS = "WRONG_CREDENTIALS";
    public final static String SIGN_IN_TYPE = "SIGN_IN_TYPE";
    public final static String SIGN_IN_TYPE_GOOGLE = "google";
    public final static String SIGN_IN_TYPE_ASTRO = "astro";


    public final static int PERIOD_OF_SYNC_SEC = (60 * 60);
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

        TextView tv = (TextView) findViewById(R.id.htmllink);
        tv.setText(Html.fromHtml("<a href=http://astro.8u.cz>astro.8u.cz"));
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // ziskani údajů uživatelova účtu
                .requestScopes(new Scope(Scopes.PLUS_ME))
                //nastavení api_client_id a požadavku na získání autorizačního kódu pro offline přístup
                .requestServerAuthCode(AstroContract.API_CLIENT_ID, true)
                .requestEmail()
                .build();

// Vytvoření GoogleApiClient s přístupem do Google Sign-In API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        accountManager = AccountManager.get(getBaseContext());

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String login = ((TextView) findViewById(R.id.accountName)).getText().toString();
                final String password = ((TextView) findViewById(R.id.accountPassword)).getText().toString();

                final String accountType = getIntent().getStringExtra(ACCOUNT_TYPE);

                showProgressDialog();

                new AsyncTask<String, Void, Intent>() {
                    @Override
                    protected Intent doInBackground(String... strings) {

                        Log.d("Response", "> Started authenticating");

                        String[] tokens;
                        Bundle data = new Bundle();

                        try {
                            tokens = ApiAuthenticator.getTokenByLogin(login, password);
                            String authToken = tokens[0];
                            String refreshToken = tokens[1];

                            data.putString(SIGN_IN_TYPE, SIGN_IN_TYPE_ASTRO);
                            data.putString(AccountManager.KEY_ACCOUNT_NAME, login);
                            data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                            data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                            data.putString(PASSWORD, password);
                            data.putString(REFRESH_TOKEN, refreshToken);

                        } catch (WrongCredentialsException e) {
                            data.putString(WRONG_CREDENTIALS, e.getMessage());
                        } catch (ApiErrorException e) {
                            Log.d("astro", e.toString());
                            e.printStackTrace();
                        }

                        final Intent result = new Intent();
                        result.putExtras(data);
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Intent intent) {
                        super.onPostExecute(intent);

                        hideProgressDialog();
                        Log.d("astro", "> onPostExecute");
                        if (intent.hasExtra(WRONG_CREDENTIALS)) {
                            Toast.makeText(AuthenticatorActivity.this, "ERROR: baly zadány neplatné přihlašovací údaje", Toast.LENGTH_SHORT).show();
                        } else {
                            finishLogin(intent);
                        }
                    }
                }.execute();
            }
        });
    }

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

            Log.d("astro", "SPUŠTĚNÍ ASYNCHRONÍHO VLÁKNA K VYTVOŘENÍ UŽIVATELE " + googleSignInAccount.getEmail());
            Log.d("astro", "SERVER_AUTH_CODE=" + googleSignInAccount.getServerAuthCode());
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
            data.putString(SIGN_IN_TYPE, SIGN_IN_TYPE_GOOGLE);
            data.putString(AccountManager.KEY_ACCOUNT_NAME, login);
            data.putString(REFRESH_TOKEN, googleTokenResponse.getRefreshToken());
            data.putString(ID_TOKEN, googleTokenResponse.getIdToken());
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
            sync();
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

    private void finishLogin(Intent intent) {
        Log.d("astro", "Auth activity finishLogin()");

        String login = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String password = intent.getStringExtra(PASSWORD);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        final Account account = new Account(login, accountType);

        if (getIntent().getBooleanExtra(NEW_ACCOUNT, false)) {


            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            data.putString(REFRESH_TOKEN, intent.getStringExtra(REFRESH_TOKEN));
            data.putString(PASSWORD, intent.getStringExtra(PASSWORD));
            data.putString(SIGN_IN_TYPE, intent.getStringExtra(SIGN_IN_TYPE));

            accountManager.addAccountExplicitly(account, password, data);
            accountManager.setAuthToken(account, "baerer", authToken);

            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.DIARY_AUTHORITY, true);

            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.DIARY_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.CATALOG_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.CATALOG_AUTHORITY, true);
        } else {
            Log.d("astro", "UPDATE ACCOUNT ONLY");
            accountManager.setPassword(account, password);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        sync();
        finish();
    }

    public void sync() {
        Log.d("astro", "SPUŠTĚNA SYNCHRONIZACE...");
        Account account;
        Account[] accounts = AccountManager.get(this).getAccountsByType(getBaseContext().getString(R.string.accountType));
        if(accounts.length > 0) {
            account = accounts[0];
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(account, AstroContract.CATALOG_AUTHORITY, settingsBundle);
            ContentResolver.requestSync(account, AstroContract.DIARY_AUTHORITY, settingsBundle);
        }
    }

}
