package cz.uhk.janMachacek;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;

/**
 * @author Jan Macháček
 *         Created on 13.10.2016.
 */
public class HomePage extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);



//        new AsyncTask<String, Void, Intent>() {
//            @Override
//            protected Intent doInBackground(String... strings) {
//                AccountManager mgr = AccountManager.get(getBaseContext());
//                Account[] accts = mgr.getAccountsByType("com.google");
//                Account acct = accts[0];
//                AccountManagerFuture<Bundle> accountManagerFuture = mgr.getAuthToken(acct, "ah", null, true, null, null);
//                Bundle authTokenBundle = null;
//                try {
//                    authTokenBundle = accountManagerFuture.getResult();
//                } catch (OperationCanceledException e) {
//                    e.printStackTrace();
//                    Log.d("astro", "GOOGLE ERROR > " + e.toString());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.d("astro", "GOOGLE ERROR > " + e.toString());
//                } catch (AuthenticatorException e) {
//                    e.printStackTrace();
//                    Log.d("astro", "GOOGLE ERROR > " + e.toString());
//                }
//                String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
//                Log.d("astro", "GOOGLE > " + authToken);
//                return null;
//            }
//        }.execute();

    }

    public void startCatalog(View view) {
        Intent intent = new Intent(this, ObjectListActivity.class);
        startActivity(intent);
    }

    public void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startDiary(View view) {
        Intent intent = new Intent(this, DiaryActivity.class);
        startActivity(intent);
    }
}
