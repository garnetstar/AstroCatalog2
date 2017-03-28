package cz.uhk.machacek.library.Synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import cz.uhk.machacek.AbstactBaseActivity;
import cz.uhk.machacek.AstroContract;
import cz.uhk.machacek.AuthenticatorActivity;
import cz.uhk.machacek.DiaryActivity;
import cz.uhk.machacek.Exception.AccessTokenExpiredException;
import cz.uhk.machacek.Exception.Api400ErrorException;
import cz.uhk.machacek.Exception.ApiErrorException;
import cz.uhk.machacek.Model.AstroDbHelper;
import cz.uhk.machacek.Model.DiaryFacade;
import cz.uhk.machacek.Model.DiaryObject;
import cz.uhk.machacek.R;

/**
 * @author Jan Macháček
 *         Created on 12.10.2016.
 *         https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
 *         http://stackoverflow.com/questions/11362233/syncadapter-syncresult
 *         https://books.google.cz/books?id=NgTqCwAAQBAJ&pg=PA272&lpg=PA272&dq=android+getContentResolver().notifyChange&source=bl&ots=i3YRGv-ojU&sig=tXrfEWRhQMFH1jQooGmLjTKs4zA&hl=cs&sa=X&ved=0ahUKEwjRnof_kIrSAhWJthQKHcBMC1gQ6AEIUjAH#v=onepage&q=android%20getContentResolver().notifyChange&f=false
 */
public class DiarySyncAdapter extends AbstractThreadedSyncAdapter {


    private final AccountManager mAccountManager;

    private DiaryData diaryData;

    private DiaryFacade facade;

    private boolean syncFromServerOK;

    public DiarySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        Log.d("astro", "ZAHÁJENÍ SYNCHRONIZACE DIARY");

        syncFromServerOK = true;

        facade = new DiaryFacade(contentProviderClient);
        String idToken = null;
        String authToken = null;

        try {
            idToken = mAccountManager.getUserData(account, AuthenticatorActivity.ID_TOKEN);
            authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);

            Log.d("astro", "SYNC: zahájení synchronizace");
            diaryData = new DiaryData(contentProviderClient, idToken);
            Log.d("astro", "SYNC: stahování dat ze serveru");
            syncFromServer(contentProviderClient);
            Log.d("astro", "SYNC: odeslání dat ne server");
            syncToServer(contentProviderClient, diaryData.getUserId(), syncResult);

            if (syncFromServerOK) {
                //zobrazit aktuální data
                refreshDiaryList();

                //zobrazit stav synchronizace
                Intent intent = new Intent();
                intent.setAction(AbstactBaseActivity.FILTER_SHOW_MESSAGE);
                intent.putExtra("message", "Synchronizace proběhla v pořádku");
                getContext().sendBroadcast(intent);
            } else {
                Log.d("astro", "SYNC PROBLEM: synchronizace nebyla dokončena");
            }


        } catch (AccessTokenExpiredException e) {
            String message = "ERROR: neaktuální přihlašovací údaje " + e.getMessage();
            Log.d("astro", "INVALIDACE TOKENU");
            Log.d("astro", "-----------------");
            mAccountManager.invalidateAuthToken(getContext().getString(R.string.accountType), authToken);
            syncResult.delayUntil = 10;
            syncResult.fullSyncRequested = true;
            Log.d("astro", "delay-10 " + syncResult.stats.toString());
        } catch (Exception e) {
            syncProblem(syncResult, e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncToServer(ContentProviderClient contentProviderClient, int userId, SyncResult syncResult) throws RemoteException {

        // ziskat všechny objekty se sync_OK = 0
        ArrayList<DiaryObject> objects = facade.getObjectForSync();

        // doplnit guid a rowCounter tam, kde chybí
        for (int i = 0; i < objects.size(); i++) {

            if (objects.get(i).getGuid() == null) {
//                nextId++;
//                String newGuid = Integer.toString(nextId) + "-" + Integer.toString(userId);
                String newGuid = UUID.randomUUID().toString();
                objects.get(i).setGuid(newGuid);
                objects.get(i).setIsNew(true);
                objects.get(i).setRowCounter(diaryData.getServerCounter());
                Log.d("astro", "NEWGUID " + objects.get(i).toString());
            }
        }

        try {
            // odeslat je na server
            diaryData.sendDataToServer(objects, diaryData.getServerCounter());

            // pokud nedojde k chybě při stahování dat, je třeba uložit hodnoty do databáze přístroje
            String selection = AstroDbHelper.KEY_DIARY_ID + "=?";
            // ulozit nove guid a sync_ok do databaze pristroje
            for (int i = 0; i < objects.size(); i++) {
                String[] selectionArgs = {Integer.toString(objects.get(i).getId())};
                objects.get(i).setSyncOk(1);

                contentProviderClient.update(getUri(), objects.get(i).getContentValues(), selection, selectionArgs);
                Log.d("astro", "Update SnyncOK=1: \n " + objects.get(i).toString());
            }


        } catch (Api400ErrorException e) {
            String message = "CONFLICT: " + e.getMessage();
            syncProblem(syncResult, message);
        } catch (Exception e) {
            syncProblem(syncResult, e.getMessage());
        }
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

        // ulozit serverCounter do clientCounter
        ContentValues val = new ContentValues();
        val.put(AstroDbHelper.KEY_SETTINGS_VALUE, diaryData.getServerCounter());
        contentProviderClient.update(Uri.parse(AstroContract.DIARY_URI + "/settings"), val, AstroDbHelper.KEY_SETTINGS_KEY + "=?", new String[]{"client_counter"});
        Log.d("astro", "client counter is now = " + diaryData.getServerCounter());


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
//                    String selection = AstroDbHelper.KEY_DIARY_GUID + "=?";
//                    String[] selectionArgs = {serverObject.getGuid()};
//                    contentProviderClient.update(getUri(), serverObject.getContentValues(), selection, selectionArgs);
                    saveObject(contentProviderClient, serverObject);
                    Log.d("astro", "Diary updated: " + serverObject.toString());
                } else {
                    // pokud není záznam synchronizovaný, syncOK = 0, je třeba řešit konflikt

                    solveConflict(contentProviderClient, deviceObject, serverObject);
                }
            } catch (RemoteException e) {

                Log.d("astro", "RemonteException: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Reload objektů v activity
     */
    private void refreshDiaryList() {
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
    private void solveConflict(ContentProviderClient client, DiaryObject deviceObject, DiaryObject serverObject) throws RemoteException {

        Log.d("astro", "CONFLICT: Solve conflict on guid=" + deviceObject.getGuid());

        // nastavit row counter
        // podle timestamp  vyresit konflikt
        int compatation = serverObject.getTimestamp().compareTo(deviceObject.getTimestamp());
        if(compatation >= 0) {
            //server timestamp je větší nebo stejný
            Log.d("astro", "server wins");
            serverObject.setRowCounter(diaryData.getServerCounter());
            saveObject(client, serverObject);
        } else {
            //device timestamp je větší
            Log.d("astro", "device wins");
            deviceObject.setRowCounter(diaryData.getServerCounter());
            saveObject(client, deviceObject);
        }


    }

    private Uri getUri() {
        return Uri.parse(AstroContract.DIARY_URI + "/diary_edit");
    }

    /**
     * @param client
     * @param object
     * @throws RemoteException
     */
    private void saveObject(ContentProviderClient client, DiaryObject object) throws RemoteException {
        String selection = AstroDbHelper.KEY_DIARY_GUID + "=?";
        String[] selectionArgs = {object.getGuid()};
        client.update(getUri(), object.getContentValues(), selection, selectionArgs);
    }

    private void syncProblem(SyncResult syncResult, String message) {
        //chyba http://stackoverflow.com/questions/14828998/how-to-show-sync-failed-message
//        syncResult.stats.numAuthExceptions++;

        syncFromServerOK = false;
        Log.d("astro", "SYNC CONFLICT 1: " + message);
        syncResult.stats.numConflictDetectedExceptions++;
//        syncResult.delayUntil = 10;


        Intent intent = new Intent();
        intent.setAction(AbstactBaseActivity.FILTER_SHOW_MESSAGE);
        intent.putExtra("message", message);
        getContext().sendBroadcast(intent);

    }

}