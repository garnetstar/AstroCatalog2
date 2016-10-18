package cz.uhk.janMachacek.library;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import cz.uhk.janMachacek.AuthenticatorActivity;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.InvalidateRefreshTokenException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;

/**
 * @author Jan Macháček
 *         Created on 10.8.2016.
 */
public class AstroAccountAuthenticator extends AbstractAccountAuthenticator {

    private final Context context;

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
            Log.d("astro", "pass="+password + " " + "login:" + account.name);

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

                Log.d("astro", "SSSSS  ApiErrorException");
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


}
