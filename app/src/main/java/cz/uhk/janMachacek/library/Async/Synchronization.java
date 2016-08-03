package cz.uhk.janMachacek.library.Async;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cz.uhk.janMachacek.AbstactBaseActivity;
import cz.uhk.janMachacek.Config;
import cz.uhk.janMachacek.R;

/**
 * Created by jan on 2.8.2016.
 */
public class Synchronization extends AsyncTask {

    private Context applicationContext;

    public Synchronization(Context context) {
        this.applicationContext = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        publishProgress("Start +++");
        try {
            Thread.sleep(1000);
            publishProgress("Second");
            Thread.sleep(1000);
            publishProgress("Third");
            Thread.sleep(1000);
            publishProgress("Fourth");
            Thread.sleep(1000);
            publishProgress("Fifth");
            Thread.sleep(1000);
            publishProgress("Sixth");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        sendStatus("OK finished");
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        String message = String.valueOf(values[0]);
        sendStatus(message);

    }

    private void sendStatus(String status) {
        Intent intent = new Intent();
        intent.setAction(AbstactBaseActivity.FILTER);
        Bundle bundle = new Bundle();
        bundle.putString("message", status);
        intent.putExtras(bundle);

        applicationContext.sendBroadcast(intent);

    }


}
