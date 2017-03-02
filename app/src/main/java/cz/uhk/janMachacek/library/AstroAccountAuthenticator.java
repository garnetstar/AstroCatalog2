package cz.uhk.janMachacek.library;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.crash.FirebaseCrash;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cz.uhk.janMachacek.AstroContract;
import cz.uhk.janMachacek.AuthenticatorActivity;

/**
 * @author Jan Macháček
 *         Created on 10.8.2016.
 */
public class AstroAccountAuthenticator extends AbstractAccountAuthenticator implements ResultCallback {

    private final Context context;

    private GoogleApiClient mGoogleApiClient;

    public AstroAccountAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String accountType, String authTokenType, String[] strings, Bundle bundle) throws NetworkErrorException {
        Log.d("astro", "AcountAuthenticator addAccount()");

        final Intent intent = new Intent(context, AuthenticatorActivity.class);

        intent.putExtra(AuthenticatorActivity.ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.AUTH_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);

        final Bundle bund = new Bundle();
        bund.putParcelable(AccountManager.KEY_INTENT, intent);
        return bund;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
        Log.d("astro", "AcountAuthenticator getAuthToken()");


        GoogleTokenResponse tokenResponse =
                null;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://www.googleapis.com/oauth2/v4/token",
                    "171814397882-qoafbodpid52h7lh0pc98bruc9vv16vs.apps.googleusercontent.com",
                    "zN_3GYmPfnSAnlz0ChErRI4M",
                    "4/-6HSoWFIA2mnlYhvoExQaHVDVN8jLn_B1lukkT6TWYg",
                    "")  // Specify the same redirect URI that you use with your web
                    // app. If you don't have a web version of your app, you can
                    // specify an empty string.
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("astro", "sdfsf " + e.toString());
        }

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        Log.d("astro", "new access " + accessToken);
        Log.d("astro", "new refresh " + refreshToken);
        return null;

    }

    @Override
    public String getAuthTokenLabel(String s) {
        Log.d("Response", "> getAuthTokenLabel");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {

        Log.d("Response", "> updatecredetntials");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        Log.d("Response", "> hasFeatures");
        return null;
    }


    @Override
    public void onResult(@NonNull Result googleSignInResult) {
        Log.d("astro", "CALLBACK FIRST");
        GoogleSignInResult r = (GoogleSignInResult) googleSignInResult;

        Log.d("astro", "CALLBACK INTERFACE = " + ((GoogleSignInResult) googleSignInResult).isSuccess() + r.getSignInAccount().getIdToken());
    }
}
