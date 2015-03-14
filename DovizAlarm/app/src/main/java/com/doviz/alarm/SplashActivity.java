package com.doviz.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by ercanpinar on 2/22/15.
 */
public class SplashActivity extends BaseActivity {
    /**
     * Duration of wait *
     */
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    Intent mainIntent = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mainIntent = new Intent(this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(mainIntent);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().hide();
    }
}
