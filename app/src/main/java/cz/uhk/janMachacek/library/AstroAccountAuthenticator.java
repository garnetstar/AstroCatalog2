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
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;

/**
 * @author Jan Macháček
 * Created on 10.8.2016.
 */
public class AstroAccountAuthenticator extends AbstractAccountAuthenticator{

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
        Log.d("astro",  "AcountAuthenticator addAccount()");

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
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        Log.d("astro",  "AcountAuthenticator getAuthToken()");

        Log.d("astro",  accountAuthenticatorResponse.toString());
        AccountManager am = AccountManager.get(context);

        Log.d("Response", "heslo>>> " + am.getUserData(account, AuthenticatorActivity.PASSWORD));


        Log.d("astro", am.getUserData(account,AuthenticatorActivity.PASSWORD));

        String refreshToken = am.getUserData(account, AuthenticatorActivity.REFRESH_TOKEN);

        try {
            String[] tokens = ApiAuthenticator.getAccessTokenByRefreshToken(refreshToken);
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, tokens[0]);
            am.setUserData(account, AuthenticatorActivity.REFRESH_TOKEN, tokens[1]);
            return result;
        } catch (ApiErrorException e) {
            e.printStackTrace();
            return null;
        } catch (WrongCredentialsException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {

        Log.d("Response",  "> updatecredetntials");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }
}
