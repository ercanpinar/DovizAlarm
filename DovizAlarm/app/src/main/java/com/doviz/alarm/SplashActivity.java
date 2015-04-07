package com.doviz.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

/**
 * Created by ercanpinar on 2/22/15.
 */
public class SplashActivity extends ActionBarActivity {
    /**
     * Duration of wait *
     */
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    Intent mainIntent = null;
    private ImageView foundDevice;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();

        mainIntent = new Intent(this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(mainIntent);
                overridePendingTransition(R.anim.bottom_enter, R.anim.wait);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().hide();
    }
}
