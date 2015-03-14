package com.doviz.alarm;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by ercanpinar on 3/14/15.
 */
public class BaseActivity extends ActionBarActivity {
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("BaseActivity", "onStart");
    }
}
