package com.bubelov.coins;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.bubelov.coins.database.DatabaseHelper;
import com.bubelov.coins.manager.MerchantSyncManager;
import com.bubelov.coins.receiver.SyncMerchantsWakefulReceiver;
import com.bubelov.coins.service.MerchantsSyncService;
import com.bubelov.coins.util.MainThreadBus;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Author: Igor Bubelov
 * Date: 03/11/13
 */

public class App extends Application {
    private static App instance;

    private Bus bus;

    private Api api;

    private SQLiteOpenHelper databaseHelper;

    private MerchantsCache merchantsCache;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        bus = new MainThreadBus();
        initApi();
        databaseHelper = new DatabaseHelper(this);
        merchantsCache = new MerchantsCache(databaseHelper.getReadableDatabase());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        MerchantSyncManager syncManager = new MerchantSyncManager(this);

        if (syncManager.getLastSyncMillis() == 0) {
            SyncMerchantsWakefulReceiver.startWakefulService(this, MerchantsSyncService.makeIntent(this));
        }
    }

    public static App getInstance() {
        return instance;
    }

    public Bus getBus() {
        return bus;
    }

    public Api getApi() {
        return api;
    }

    public SQLiteOpenHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public MerchantsCache getMerchantsCache() {
        return merchantsCache;
    }

    private void initApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.api_url))
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        api = restAdapter.create(Api.class);
    }
}