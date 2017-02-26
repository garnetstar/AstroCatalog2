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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import cz.uhk.janMachacek.AstroContract;
import cz.uhk.janMachacek.AuthenticatorActivity;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.InvalidateRefreshTokenException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.R;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;

/**
 * @author Jan Macháček
 *         Created on 10.8.2016.
 */
public class AstroAccountAuthenticator extends AbstractAccountAuthenticator {

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

        AccountManager am = AccountManager.get(context);
        String refreshToken = am.getUserData(account, AuthenticatorActivity.REFRESH_TOKEN);
        String[] tokens;

        try {

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(AstroContract.API_CLIENT_ID)
                    .build();
            // [END configure_signin]


            // [START build_client]
            // Build a GoogleApiClient with access to the Google Sign-In API and the
            // options specified by gso.
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    //      .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            Log.d("astro", "AUTENTICATOR> " + opr.isDone());
            if (opr.isDone()) {
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
//                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
//                        hideProgressDialog();
                        if (googleSignInResult.isSuccess()) {
                            // Signed in successfully, show authenticated UI.
                            GoogleSignInAccount acct = googleSignInResult.getSignInAccount();
                            Log.d("astro", "SIGN > " + "create accoutn");
                            Log.d("astro", acct.getEmail());
                            Log.d("astro", acct.getId());
                            Log.d("astro", acct.getIdToken());
                        }
                    }
                });
            }

            Log.d("astro", "AUTENTICATOR> GET TOKEN:");


            tokens = ApiAuthenticator.getAccessTokenByRefreshToken(refreshToken);
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, tokens[0]);
//            am.setUserData(account, AuthenticatorActivity.REFRESH_TOKEN, tokens[1]);
            return result;
        } catch (ApiErrorException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidateRefreshTokenException e) {

            Log.d("astro", "INVALID REFRESHTOKEN EXCEPTION");
            String login = account.name;
            String password = am.getUserData(account, AuthenticatorActivity.PASSWORD);
            Log.d("astro", "pass=" + password + " " + "login:" + account.name);


//
//            try {
//                tokens = ApiAuthenticator.getTokenByLogin(login, password);
//                Bundle result = new Bundle();
//                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
//                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
//                result.putString(AccountManager.KEY_AUTHTOKEN, tokens[0]);
//                am.setUserData(account, AuthenticatorActivity.REFRESH_TOKEN, tokens[1]);
//                return result;
//
//            } catch (WrongCredentialsException e1) {
//                Log.d("astro", "WRONG CREDENTIALS EXCEPTION  ***");
//
//                final Intent intent = new Intent(context, AuthenticatorActivity.class);
//
//                intent.putExtra(AuthenticatorActivity.ACCOUNT_TYPE, account.type);
//                intent.putExtra(AuthenticatorActivity.LOGIN, account.name);
//                intent.putExtra(AuthenticatorActivity.AUTH_TYPE, authTokenType);
//                intent.putExtra(AuthenticatorActivity.NEW_ACCOUNT, false);
//                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
//
//          //      final Bundle bund = new Bundle();
//                bundle.putParcelable(AccountManager.KEY_INTENT, intent);
//                return bundle;
//
//            } catch (ApiErrorException e1) {
//
//                Log.d("astro", "SSSSS  ApiErrorException");
//                e1.printStackTrace();
//            }

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


}
