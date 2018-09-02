package com.undoredo.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.undoredo.app.main.MyLockActivity;
import com.undoredo.app.model.MyObjectBox;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class MyApplication extends Application {

    private static MyApplication mInstance;

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase Crash Report
        Fabric.with(this, new Crashlytics());

        mInstance = this;

        // Initialize ObjectBox Database
        boxStore = MyObjectBox.builder().androidContext(this).build();
        if (BuildConfig.DEBUG) {
            // if the app is in debug mode, this will show a notification on the phone
            // allowing you to click on it and view in a intenet browser the database content.
            // Comment out this block to disable it
            new AndroidObjectBrowser(boxStore).start(this);
        }

        // Initialize the Pin/FingerPrint Lock
        LockManager<MyLockActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, MyLockActivity.class);
        lockManager.getAppLock().setLogoId(R.drawable.security_lock);

    }


    public static MyApplication getInstance(){
        return mInstance;
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }

}
