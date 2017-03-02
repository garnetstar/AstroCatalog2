package cz.uhk.janMachacek;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.api.client.auth.oauth2.TokenResponse;

import cz.uhk.janMachacek.library.Api.GoogleAutentication;

/**
 * @author Jan Macháček
 *         Created on 28.2.2017.
 */
public class UserActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleAutentication.UserCreatedCallback {

    private static final int RC_SIGN_IN = 1234;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    /**
     * Perform initialization of all fragments and loaders.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // ziskani údajů uživatelova účtu
                .requestScopes(new Scope(Scopes.PROFILE))
                //nastavení api_client_id a požadavku na získání autorizačního kódu pro offline přístup
                .requestServerAuthCode(AstroContract.API_CLIENT_ID, false)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d("astro", "SIGN > Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
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
//                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    private void signOut() {
        Log.d("astro", "SIGN OUT and rewoke");
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d("astro", "Account is revoked");
                        // [START_EXCLUDE]
                        Log.d("astro", "REMOVE ACCOUNT");

                        AccountManager am = AccountManager.get(getBaseContext());
                        Account[] accounts = am.getAccountsByType(getBaseContext().getString(R.string.accountType));
                        if (accounts.length > 0) {
                            Account accountToRemove = accounts[0];
                            am.removeAccount(accountToRemove, new AccountManagerCallback<Boolean>() {
                                @Override
                                public void run(AccountManagerFuture<Boolean> future) {
                                    Log.d("astro", "účet byl úspěšně odstraněn");
                                    Intent intent = new Intent(getBaseContext(), HomePage.class);
                                    startActivity(intent);
                                }
                            }, null);
                        }
//                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
//
//    private void signIn() {
//        showProgressDialog();
//        Log.d("astro", "přihlášení k účtu googlu, zobrazení dialogu");
//        // Zobrazení přihlašovacího dialogu
//
//
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }

//    /**
//     * Dispatch incoming result to the correct fragment.
//     *
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleSignInResult(result);
//        }
//    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.d("astro", "Přihlášení proběhlo v pořádku " + acct.getDisplayName() + " " + acct.getServerAuthCode() + " " + acct.getPhotoUrl());


            Account[] accounts = AccountManager.get(this).getAccountsByType(getBaseContext().getString(R.string.accountType));
            Log.d("astro", "pocet uctu " + accounts.length + " " + acct.getDisplayName());
            // pokud ucet neexistuje
            if (accounts.length == 0) {
                createAccount(acct);
            } else {
//                updateUI(true);
            }
            TextView v = (TextView) findViewById(R.id.user_name);
            v.setText(acct.getDisplayName());

        } else {
            // Signed out, show unauthenticated UI.
//            updateUI(false);
        }
    }
//
//    private void updateUI(boolean logIn) {
//        if (logIn) {
//            findViewById(R.id.sign_in_layout).setVisibility(View.GONE);
//            findViewById(R.id.signo_out_layout).setVisibility(View.VISIBLE);
//            hideProgressDialog();
//        } else {
//            findViewById(R.id.sign_in_layout).setVisibility(View.VISIBLE);
//            findViewById(R.id.signo_out_layout).setVisibility(View.GONE);
//        }
//    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createAccount(GoogleSignInAccount acct) {
        Log.d("astro", "Autorizační kód serveru: " + acct.getServerAuthCode());
        GoogleAutentication ga = new GoogleAutentication(AccountManager.get(this));
        ga.createUser(acct, this, getContentResolver());

    }

    @Override
    public void onUserCreated(Object o) {

        TokenResponse t = (TokenResponse) o;
        Log.d("astro", "c accessToken=" + t.getAccessToken());
        Log.d("astro", "c refreshToken=" + t.getRefreshToken());

        Log.d("astro", "User successfuly created show in user thread");
//        updateUI(true);
        Log.d("astro", "button should be gone");
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
