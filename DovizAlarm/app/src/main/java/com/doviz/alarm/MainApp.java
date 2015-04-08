package com.doviz.alarm;

import android.app.Application;

/**
 * Created by ercanpinar on 4/8/15.
 */
public class MainApp extends Application {


    public MainApp() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GaUtil.init(getApplicationContext());
    }
}
