package com.doviz.alarm;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by ercanpinar on 3/14/15.
 */
public class MainActivity extends BaseActivity {
    FragmentManager fm;
    FragmentTransaction ft;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fm = this.getSupportFragmentManager();
        replaceFragment(new MainFragment());
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void replaceFragment(BaseFragment fragment) {
        ft = fm.beginTransaction();
        ft.replace(R.id.frame_main, fragment);
        ft.commit();
    }
}
