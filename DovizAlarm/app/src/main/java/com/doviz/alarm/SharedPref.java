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
    public String ALTIN_SATIS = "ALTIN_SATIS";
    public String ALTIN_ALIS = "ALTIN_ALIS";
    public String ALARM_SURE = "ALARM_SURE";
    public String KAYNAK = "KAYNAK";
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
        if (!value.equals("") && !value.equals("0")) {
            editor.putString(kurType, value);
            editor.commit();
        } else {
            editor.remove(kurType);
            editor.commit();
        }
    }

    public void updateAlarmTime(String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        if (!value.equals("") && !value.equals("0")) {
            editor.putString(ALARM_SURE, value);
            editor.commit();
        }
    }

    public String getAlarmTime() {
        String data = getPrefs().getString(ALARM_SURE, "");
        if (data == null || data.equals(""))
            data = "60000";
        return data;
    }

    public String getAlarmValue(String kurType) {
        String data = getPrefs().getString(kurType, "");
        if (data == null || data.equals(""))
            data = "0";
        return data;
    }

    //kaynak
    public void updateKaynak(String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        if (!value.equals("") && !value.equals("0")) {
            editor.putString(KAYNAK, value);
            editor.commit();
        }
    }

    public String getKaynak() {
        String data = getPrefs().getString(KAYNAK, "");
        if (data == null || data.equals(""))
            data = "1";
        return data;
    }
}
