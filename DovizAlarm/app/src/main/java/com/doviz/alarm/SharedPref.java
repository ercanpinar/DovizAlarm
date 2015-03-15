package com.doviz.alarm;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ercanpinar on 3/15/15.
 */
public class SharedPref {
    public String DOLAR_SATIS = "DOLAR_SATIS";
    public String DOLAR_ALIS = "DOLAR_ALIS";
    public String EURO_SATIS = "EURO_SATIS";
    public String EURO_ALIS = "EURO_ALIS";
    Context context;
    private String SHARED_PREFS_FILE_NAME = "doviz_alarm_shared_prefs";

    public SharedPref() {
    }

    public SharedPref(Context context) {
        this.context = context;
    }

    private SharedPreferences getPrefs() {
        return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void updateAlarm(String kurType, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();

        if (!value.equals("")) {
            editor.putString(kurType, value);
            editor.commit();
        } else {
            editor.remove(kurType);
            editor.commit();
        }
    }

    public String getAlarmValue(String kurType) {
        String data = getPrefs().getString(kurType, "");

        return data;
    }
}
