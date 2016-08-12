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

import junit.framework.Test;

import cz.uhk.janMachacek.AuthenticatorActivity;
import cz.uhk.janMachacek.TestActivity;

/**
 * Created by jan on 10.8.2016.
 */
public class AstroAuthenticator extends AbstractAccountAuthenticator{

    private final Context mContext;

    public AstroAuthenticator(Context context) {
        super(context);
        mContext = context;
    }
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        Log.d("Response",  "> EditProperies");
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
        Log.d("Response",  "> addAccount");

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);

        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, s);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, s1);
        intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);


        final Bundle bund = new Bundle();
        bund.putParcelable(AccountManager.KEY_INTENT, intent);
        Log.d("Response",  "> bound");
        return bund;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
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
