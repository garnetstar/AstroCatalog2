package cz.uhk.janMachacek.library.Synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import cz.uhk.janMachacek.AstroContract;
import cz.uhk.janMachacek.Exception.AccessTokenExpiredException;
import cz.uhk.janMachacek.Exception.ApiErrorException;
import cz.uhk.janMachacek.Model.AstroDbHelper;
import cz.uhk.janMachacek.Model.AstroObject;
import cz.uhk.janMachacek.Model.DiaryFacade;
import cz.uhk.janMachacek.Model.DiaryObject;

/**
 * @author Jan Macháček
 *         Created on 12.10.2016.
 */
public class DiarySyncAdapter extends AbstractThreadedSyncAdapter {

    private final AccountManager mAccountManager;

    public DiarySyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        Log.d("astro", "Synchronizace diary_edit");
        Log.d("astro", contentProviderClient.getLocalContentProvider().getClass().toString());

        try {
            String authToken = mAccountManager.blockingGetAuthToken(account, "baerer", true);

            updateObjects(authToken, contentProviderClient);

        } catch (Exception e) {
            Log.d("astro", e.toString());
            e.printStackTrace();
        }


//            //test
//            ContentValues val = new ContentValues();
//            val.put(AstroDbHelper.KEY_SETTINGS_VALUE, ++client_counter);
//            contentProviderClient.update(uri,val,AstroDbHelper.KEY_SETTINGS_KEY + "=?", new String[]{"client_counter"});
    }

    /**
     * Synchronizace objektů v zařízení se serverem
     *
     * @param authToken
     * @param contentProviderClient
     * @throws ApiErrorException
     * @throws AccessTokenExpiredException
     * @throws RemoteException
     */
    private void updateObjects(String authToken, ContentProviderClient contentProviderClient) throws ApiErrorException, AccessTokenExpiredException, RemoteException {
        DiaryData diaryData = new DiaryData(contentProviderClient, authToken);

        ArrayList<DiaryObject> diaryObjects = diaryData.getDataFromServer();
        DiaryFacade facade = new DiaryFacade(contentProviderClient);

        // Vlozit data ziskana ze serveru
        int i = 0;
        //   ContentValues newValues[] = new ContentValues[diaryObjects.size()];
        for (DiaryObject serverObject : diaryObjects) {
            // newValues[i++] = oneValue.getContentValues();
            try {
                Log.d("astro", "Insert will " + serverObject.toString());
                Log.d("astro", "insert CV " + serverObject.getContentValues().toString());
                contentProviderClient.insert(Uri.parse(AstroContract.DIARY_URI + "/diary_edit"), serverObject.getContentValues());
                Log.d("astro", "Insert just now " + serverObject.toString());
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
        Log.d("astro", "INSERT diaryData = " + diaryObjects.toString());
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
