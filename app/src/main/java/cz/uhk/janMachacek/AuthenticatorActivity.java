package cz.uhk.janMachacek;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Exception.WrongCredentialsException;
import cz.uhk.janMachacek.library.Api.ApiAuthenticator;

/**
 * Created by jan on 10.8.2016.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String AUTH_TYPE = "AUTH_TYPE";
    public final static String LOGIN = "LOGIN";
    public final static String PASSWORD = "PASSWORD";
    public final static String REFRESH_TOKEN = "REFRESH_TOKEN";
    public final static String WRONG_CREDENTIALS = "WRONG_CREDENTIALS";

    public final static String NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d("Response", "Auth activity onCreae()");
        setContentView(R.layout.act_login);

        accountManager = AccountManager.get(getBaseContext());

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String login = ((TextView) findViewById(R.id.accountName)).getText().toString();
                final String password = ((TextView) findViewById(R.id.accountPassword)).getText().toString();

                final String accountType = getIntent().getStringExtra(ACCOUNT_TYPE);


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
                        Log.d("astro", "> onPostExecute");
                        if (intent.hasExtra(WRONG_CREDENTIALS)) {
                            Toast.makeText(getBaseContext(), intent.getStringExtra(WRONG_CREDENTIALS), Toast.LENGTH_LONG).show();
                        } else {
                            finishLogin(intent);
                        }
                    }
                }.execute();
            }
        });

    }

    private void finishLogin(Intent intent) {
        Log.d("astro", "Auth activity finishLogin()");

        String login = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String password = intent.getStringExtra(PASSWORD);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        final Account account = new Account(login, accountType);

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
        data.putString(REFRESH_TOKEN, intent.getStringExtra(REFRESH_TOKEN));
        data.putString(PASSWORD, intent.getStringExtra(PASSWORD));

        accountManager.addAccountExplicitly(account, password, data);
        accountManager.setAuthToken(account, "baerer", authToken);

        /** @todo nevim zda to je tady k necemu **/
//        Intent intent = new Intent();
//        intent.putExtras(data);
//        setAccountAuthenticatorResult(intent.getExtras());
//        setResult(RESULT_OK, intent);

        finish();
    }

}
