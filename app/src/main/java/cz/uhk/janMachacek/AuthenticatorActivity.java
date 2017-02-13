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
 * https://sites.google.com/site/andsamples/concept-of-syncadapter-androidcontentabstractthreadedsyncadapter
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String AUTH_TYPE = "AUTH_TYPE";
    public final static String LOGIN = "LOGIN";
    public final static String PASSWORD = "PASSWORD";
    public final static String REFRESH_TOKEN = "REFRESH_TOKEN";
    public final static String WRONG_CREDENTIALS = "WRONG_CREDENTIALS";
    public final static int PERIOD_OF_SYNC_SEC = 30;

    public final static String NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.d("Response", "Auth activity onCreae()");
        setContentView(R.layout.act_login);

        accountManager = AccountManager.get(getBaseContext());
        String accountName = getIntent().getStringExtra(LOGIN);
        if (accountName != null) {
            ((TextView)findViewById(R.id.accountName)).setText(accountName);
        }

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

        if (getIntent().getBooleanExtra(NEW_ACCOUNT, false)) {


            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            data.putString(REFRESH_TOKEN, intent.getStringExtra(REFRESH_TOKEN));
            data.putString(PASSWORD, intent.getStringExtra(PASSWORD));

            accountManager.addAccountExplicitly(account, password, data);
            accountManager.setAuthToken(account, "baerer", authToken);

            //spustit synchronizaci
//            getApplicationContext().getContentResolver().setMasterSyncAutomatically(true);
//            getApplicationContext().getContentResolver().setIsSyncable(account, AstroContract.DIARY_AUTHORITY, 1);
//            getApplicationContext().getContentResolver().setIsSyncable(account, AstroContract.CATALOG_AUTHORITY, 1);
            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.DIARY_AUTHORITY,true);

            getApplicationContext().getContentResolver().addPeriodicSync(account, AstroContract.DIARY_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().addPeriodicSync(account,  AstroContract.CATALOG_AUTHORITY, new Bundle(), PERIOD_OF_SYNC_SEC);
            getApplicationContext().getContentResolver().setSyncAutomatically(account, AstroContract.CATALOG_AUTHORITY,true);
        }
        else{
            Log.d("astro", "UPDATE ACCOUNT ONLY");
            accountManager.setPassword(account, password);
        }

        /** @TODO nevim zda to je tady k necemu **/
       // Intent intent1 = new Intent();
     //   intent1.putExtras(data);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("astro", "!!!! onActivityResult");
    }
}
