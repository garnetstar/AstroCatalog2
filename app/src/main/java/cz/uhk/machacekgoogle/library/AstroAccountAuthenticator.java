package cz.uhk.machacekgoogle.library;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import cz.uhk.machacekgoogle.AuthenticatorActivity;
import cz.uhk.machacekgoogle.Exception.ApiErrorException;
import cz.uhk.machacekgoogle.Exception.InvalidateRefreshTokenException;
import cz.uhk.machacekgoogle.Exception.WrongCredentialsException;
import cz.uhk.machacekgoogle.library.Api.ApiAuthenticator;

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

        AccountManager am = AccountManager.get(context);

        String signType = am.getUserData(account, AuthenticatorActivity.SIGN_IN_TYPE);
        Log.d("astro", "sign in type === " + signType + " break");

        switch (signType) {
            case AuthenticatorActivity.SIGN_IN_TYPE_GOOGLE:
                return getAuthTokenByGoogle(account, am);
            case AuthenticatorActivity.SIGN_IN_TYPE_ASTRO:
                return getAuthTokenByAstro(account, am, bundle, accountAuthenticatorResponse, authTokenType);
            default:
                Log.d("astro", "ERROR: NO SYNC ACCOUNT TYPE");
                break;
        }
        return null;

    }

    private Bundle getAuthTokenByGoogle(Account account, AccountManager am) {
        Log.d("astro", "ACCOUNT_AUTENTICATOR - OBNOVENI TOKENŮ");
        Log.d("astro", "--------------------------------------" + account.type);

        String refreshToken = am.getUserData(account, AuthenticatorActivity.REFRESH_TOKEN);

        Log.d("astro", "REFRESH_TOKEN=" + refreshToken);

        try {
            GoogleTokenResponse googleTokenResponse = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    refreshToken,
                    "171814397882-qoafbodpid52h7lh0pc98bruc9vv16vs.apps.googleusercontent.com",
                    "zN_3GYmPfnSAnlz0ChErRI4M"
            ).execute();


            Log.d("astro", "NEW ID_TOKEN=" + googleTokenResponse.getIdToken());

            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, googleTokenResponse.getAccessToken());
            result.putString(AuthenticatorActivity.ID_TOKEN, googleTokenResponse.getIdToken());

            if (googleTokenResponse.getRefreshToken() != null) {
                am.setUserData(account, AuthenticatorActivity.REFRESH_TOKEN, googleTokenResponse.getRefreshToken());
            }
            am.setUserData(account, AuthenticatorActivity.ID_TOKEN, googleTokenResponse.getIdToken());

            Log.d("astro", "NEW TOKEN = " + googleTokenResponse.getAccessToken());

            Log.d("astro", am.getUserData(account, AuthenticatorActivity.ID_TOKEN));
            Log.d("astro", "ACCOUNT_AUTENTICATOR - TOKENY BYLY OBNOVENY");
            Log.d("astro", "-------------------------------------------");

            return result;

        } catch (IOException e) {
            Log.d("astro", "ERROR IN GoogleRefreshTokenRequest " + e.toString());
            e.printStackTrace();
        }

        return null;
    }

    private Bundle getAuthTokenByAstro(Account account, AccountManager am, Bundle bundle, AccountAuthenticatorResponse accountAuthenticatorResponse, String authTokenType) {
        String refreshToken = am.getUserData(account, AuthenticatorActivity.REFRESH_TOKEN);
        String[] tokens;

        try {
            tokens = ApiAuthenticator.getAccessTokenByRefreshToken(refreshToken);
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, tokens[0]);
            am.setUserData(account, AuthenticatorActivity.REFRESH_TOKEN, tokens[1]);
            return result;
        } catch (ApiErrorException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidateRefreshTokenException e) {

            Log.d("astro", "INVALID REFRESHTOKEN EXCEPTION");
            String login = account.name;
            String password = am.getUserData(account, AuthenticatorActivity.PASSWORD);
            Log.d("astro", "pass=" + password + " " + "login:" + account.name);

            try {
                tokens = ApiAuthenticator.getTokenByLogin(login, password);
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, tokens[0]);
                am.setUserData(account, AuthenticatorActivity.REFRESH_TOKEN, tokens[1]);
                return result;

            } catch (WrongCredentialsException e1) {
                Log.d("astro", "WRONG CREDENTIALS EXCEPTION  ***");

                final Intent intent = new Intent(context, AuthenticatorActivity.class);

                intent.putExtra(AuthenticatorActivity.ACCOUNT_TYPE, account.type);
                intent.putExtra(AuthenticatorActivity.LOGIN, account.name);
                intent.putExtra(AuthenticatorActivity.AUTH_TYPE, authTokenType);
                intent.putExtra(AuthenticatorActivity.NEW_ACCOUNT, false);
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);

                //      final Bundle bund = new Bundle();
                bundle.putParcelable(AccountManager.KEY_INTENT, intent);
                return bundle;

            } catch (ApiErrorException e1) {

                Log.d("astro", "ApiErrorException");
                e1.printStackTrace();
            }

            e.printStackTrace();
            return null;
        }
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
