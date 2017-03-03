package cz.uhk.janMachacek;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jan Macháček
 *         Created on 28.2.2017.
 */
public class UserActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 1234;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private ImageView imageView;

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
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getBaseContext().getString(R.string.accountType));
        if (accounts.length > 0) {
            String login = accountManager.getUserData(accounts[0], AccountManager.KEY_ACCOUNT_NAME);
            String name = accountManager.getUserData(accounts[0], AuthenticatorActivity.NAME);
            String picture = accountManager.getUserData(accounts[0], AuthenticatorActivity.PICTURE);
            TextView loginView = (TextView) findViewById(R.id.login);
            loginView.setText(login);
            TextView nameView = (TextView) findViewById(R.id.user_name);
            nameView.setText(name);

            imageView = (ImageView) findViewById(R.id.user_image);
            String url = picture;
            ShowUserPictureJob showUserPictureJob = new ShowUserPictureJob();
            showUserPictureJob.execute(url);
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
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    private void signOut() {

        Log.d("astro", "SIGN OUT and rewoke");
        showProgressDialog();
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
                                    hideProgressDialog();
                                    startActivity(intent);
                                }
                            }, null);
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("astro", "CONNECTION  FIELD 1234435 " + connectionResult.getErrorMessage().toString());
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

    class ShowUserPictureJob extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... voids) {

            Bitmap bitmap = null;
            try {
                URL url = new URL(voids[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

}
