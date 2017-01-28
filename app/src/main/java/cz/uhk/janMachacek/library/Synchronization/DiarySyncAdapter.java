package cz.uhk.janMachacek.library.Synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import cz.uhk.janMachacek.AstroContract;
import cz.uhk.janMachacek.DiaryActivity;
import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Model.AstroDbHelper;
import cz.uhk.janMachacek.Model.DiaryFacade;
import cz.uhk.janMachacek.Model.DiaryObject;
import cz.uhk.janMachacek.ObjectListActivity;

/**
 * @author Jan Macháček
 *         Created on 12.10.2016.
 */
public class DiarySyncAdapter extends AbstractThreadedSyncAdapter {

    private final AccountManager mAccountManager;

    private DiaryData diaryData;

    private DiaryFacade facade;

    public DiarySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult)  {

        Log.d("astro", "Synchronizace diary_edit");
        Log.d("astro", contentProviderClient.getLocalContentProvider().getClass().toString());

        facade = new DiaryFacade(contentProviderClient);


        try {
            String authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);
            Log.d("astro", "A");
            diaryData = new DiaryData(contentProviderClient, authToken);
            Log.d("astro", "B");
            syncFromServer(contentProviderClient);
            Log.d("astro", "C");
            syncToServer(contentProviderClient, diaryData.getNextId(), diaryData.getUserId());
            Log.d("astro", "D");
        } catch (AccessTokenExpiredException e) {
            //chyba http://stackoverflow.com/questions/14828998/how-to-show-sync-failed-message
            syncResult.stats.numAuthExceptions++;
            syncResult.delayUntil = 180;
        } catch (Exception e) {
            Log.d("astro", "onPerformSync diary " + e.toString());
            e.printStackTrace();
        }


//            //test
//            ContentValues val = new ContentValues();
//            val.put(AstroDbHelper.KEY_SETTINGS_VALUE, ++client_counter);
//            contentProviderClient.update(uri,val,AstroDbHelper.KEY_SETTINGS_KEY + "=?", new String[]{"client_counter"});
    }

    private void syncToServer(ContentProviderClient contentProviderClient, int nextId, int userId) throws RemoteException {

        // ziskat všechny objekty se sync_OK = 0
        ArrayList<DiaryObject> objects = facade.getObjectForSync();

        // doplnit guid a rowCounter tam, kde chybí
        for (int i = 0; i < objects.size(); i++) {

            if(objects.get(i).getGuid() == null) {
                nextId++;
                String newGuid = Integer.toString(nextId) + "-" + Integer.toString(userId);

                objects.get(i).setGuid(newGuid);
                objects.get(i).setIsNew(true);
                objects.get(i).setRowCounter(diaryData.getServerCounter());
                Log.d("astro", "NEWGUID " + objects.get(i).toString());
            }
        }

        // odeslat je na server
        diaryData.sendDataToServer(objects,diaryData.getServerCounter());

        String selection = AstroDbHelper.KEY_DIARY_ID + "=?";
        // ulozit nove guid a sync_ok do databaze pristroje
        for(int i = 0; i < objects.size(); i++) {
            String[] selectionArgs = {Integer.toString(objects.get(i).getId())};
            objects.get(i).setSyncOk(1);
            Log.d("astro", "SSSDDDFFF "  + objects.get(i).toString());
            contentProviderClient.update(getUri(), objects.get(i).getContentValues(), selection, selectionArgs);
        }

        // ulozit serverCounter do clientCounter


//            //test
            ContentValues val = new ContentValues();
            val.put(AstroDbHelper.KEY_SETTINGS_VALUE, diaryData.getServerCounter());
            contentProviderClient.update(Uri.parse(AstroContract.DIARY_URI + "/settings"),val,AstroDbHelper.KEY_SETTINGS_KEY + "=?", new String[]{"client_counter"});
            Log.d("astro", "client counter is now = " + diaryData.getServerCounter());

    }

    /**
     * Synchronizace objektů v zařízení se serverem
     *
     * @param contentProviderClient
     * @throws ApiErrorException
     * @throws AccessTokenExpiredException
     * @throws RemoteException
     */
    private void syncFromServer(ContentProviderClient contentProviderClient) throws ApiErrorException, AccessTokenExpiredException, RemoteException {


        ArrayList<DiaryObject> diaryObjects = diaryData.getDataFromServer();


        // Vlozit data ziskana ze serveru
        int i = 0;
        //   ContentValues newValues[] = new ContentValues[diaryObjects.size()];
        for (DiaryObject serverObject : diaryObjects) {
            // newValues[i++] = oneValue.getContentValues();
            try {
//                serverObject.setRowCounter(diaryData.getServerCounter());
                contentProviderClient.insert(Uri.parse(AstroContract.DIARY_URI + "/diary_edit"), serverObject.getContentValues());
                Log.d("astro", "INSERT diaryData = " + diaryObjects.toString());
            } catch (SQLiteConstraintException e) {

                // doslo ke konfliktu, guid už v telefonu existuje
                // ziskat záznam z databáze telefonu
                DiaryObject deviceObject = facade.getOneByGuid(serverObject.getGuid());
                // pokud je záznam synchronizovaný, syncOK = 1, provede se update
                if (deviceObject.getSyncOk() == 1) {
                    String selection = AstroDbHelper.KEY_DIARY_GUID + "=?";
                    String[] selectionArgs = {serverObject.getGuid()};
                    contentProviderClient.update(getUri(), serverObject.getContentValues(), selection, selectionArgs);
                    Log.d("astro", "Diary updated: " + serverObject.toString());
                } else {
                    // pokud není záznam synchronizovaný, syncOK = 0, je třeba řešit konflikt
                    solveConflict(deviceObject, new DiaryObject());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        //zobrazit aktuální data
        Intent intent = new Intent();
        intent.setAction(DiaryActivity.REFRESH_DIARY_LIST);
        Log.d("Response", "SEND DIARY BROADCAST *");
        getContext().sendBroadcast(intent);

    }

    /**
     * @param deviceObject
     * @param serverObject
     * @// TODO: 28.11.2016 dořešit konflikty
     */
    private void solveConflict(DiaryObject deviceObject, DiaryObject serverObject) {

        Log.d("astro", "Solve conflict on guid=" + deviceObject.getGuid());

    }

    private Uri getUri() {
        return Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
    }
}
