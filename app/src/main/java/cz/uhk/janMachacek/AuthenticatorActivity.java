package cz.uhk.janMachacek;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by jan on 10.8.2016.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity
{

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.d("Response",  "Auth activity onCreae()");
        setContentView(R.layout.act_login);

        mAccountManager = AccountManager.get(getBaseContext());


        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Response",  "Auth activity CLICK");
                submit();
            }
        });

    }

    public void submit() {

        new AsyncTask<String, Void, Intent>() {
            @Override
            protected Intent doInBackground(String... strings) {
                Log.d("Response", "> Started authenticating");
                return null;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                super.onPostExecute(intent);
                Log.d("Response", "> onPostExecute");
                finishLogin(intent);
            }
        }.execute();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Response",  "Auth activity onStart()");
    }

    private void finishLogin(Intent intent1)
    {
        Log.d("Response",  "Auth activity finishLogin()");
        final Account account = new Account("xxxxxxx", "com.astro.auth_example");

        mAccountManager.addAccountExplicitly(account, "passsworddd", null);
        mAccountManager.setAuthToken(account, "baerer", "token-asldfhsdhfusdfksdfhsdfhsidfoasdfs54df4sad5f");

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, "ssssssssss");
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.astro.auth_example");
        data.putString(AccountManager.KEY_AUTHTOKEN, "token");
        data.putString("USER_PASS", "password");

        Intent intent = new Intent();
        intent.putExtras(data);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("Response",  "Auth activity onActivityResult()");
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == 1 && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
