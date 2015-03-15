package com.doviz.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by ercanpinar on 3/15/15.
 */
public class DovizAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            String message = bundle.getString("message");
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "Bir hata olustu.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
