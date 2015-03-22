package com.doviz.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.doviz.alarm.response.DovizResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class DovizAlarmService extends Service {
    Timer timer;
    Handler handler;
    SharedPref shrp;
    String notficationMessage = "";

    public static long LOOP_TIME = 1800000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        shrp = new SharedPref(getApplicationContext());
        timer = new Timer();
        handler = new Handler(Looper.getMainLooper());
        LOOP_TIME = Long.valueOf(shrp.getAlarmTime());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                bilgiVer();
            }
        }, 0, LOOP_TIME);
    }

    private void bilgiVer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                refreshRequest();
            }
        });
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    /**
     * Request
     */

    private RequestQueue rq;
    JsonObjectRequest jReq;
    String DOVIZ_URL = "http://www.doviz.gen.tr/doviz_json.asp?version=1.2";

    public void refreshRequest() {
        if (internetConnectionCheck()) {
            rq = Volley.newRequestQueue(DovizAlarmService.this);
            jReq = new JsonObjectRequest(DOVIZ_URL, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<DovizResponse>() {
                                }.getType();
                                DovizResponse data = null;
                                String tempStr = response.toString();
                                try {
                                    URLEncoder.encode(tempStr, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                data = gson.fromJson(tempStr, type);
                                if (data != null) {
                                    notficationMessage = "";
                                    alarmControl(data.dolar, shrp.DOLAR_ALIS, true);
                                    alarmControl(data.dolar2, shrp.DOLAR_SATIS, false);
                                    alarmControl(data.euro, shrp.EURO_ALIS, true);
                                    alarmControl(data.euro2, shrp.EURO_SATIS, false);
                                    if (!notficationMessage.equals(""))
                                        callAlarmManager(notficationMessage);
                                }
                            }
                        }
                    }

                    , new Response.ErrorListener()

            {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley hatasi service: ", error.toString());
                }
            }
            );
            rq.add(jReq);
        }
    }

    private void alarmControl(String value, String type, boolean isAlisCheck) {
        String temp = shrp.getAlarmValue(type);

        if (temp != null && !temp.equals("") && !temp.equals("0") && !temp.equals("0.0000")) {
            temp = temp.replace(".", "");
            value = value.replace(".", "");
            int valueInt = 0;
            int tempInt = 0;
            tempInt = Integer.parseInt(temp);
            valueInt = Integer.parseInt(value);
            if (isAlisCheck) {
                if (valueInt >= tempInt) {
                    if (type.equals(shrp.DOLAR_ALIS)) {
                        if (!notficationMessage.equals(""))
                            notficationMessage = notficationMessage + ",";
                        notficationMessage = notficationMessage + getResources().getString(R.string.dolar_max_alarm);
                    } else if (type.equals(shrp.EURO_ALIS)) {
                        if (!notficationMessage.equals(""))
                            notficationMessage = notficationMessage + ",";
                        notficationMessage = notficationMessage + getResources().getString(R.string.euro_max_alarm);
                    }
                }
            } else {
                if (valueInt <= tempInt) {
                    if (type.equals(shrp.DOLAR_SATIS)) {
                        if (!notficationMessage.equals(""))
                            notficationMessage = notficationMessage + ",";
                        notficationMessage = notficationMessage + getResources().getString(R.string.dolar_min_alarm);
                    } else if (type.equals(shrp.EURO_SATIS)) {
                        if (!notficationMessage.equals(""))
                            notficationMessage = notficationMessage + ",";
                        notficationMessage = notficationMessage + getResources().getString(R.string.euro_min_alarm);
                    }
                }
            }
        }
    }

    private void callAlarmManager(String message) {

        Intent intent = new Intent(DovizAlarmService.this, DovizAlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        intent.putExtras(bundle);
        PendingIntent pin = PendingIntent.getBroadcast(DovizAlarmService.this, 12, intent, 0);
        AlarmManager alarmManager = (AlarmManager) DovizAlarmService.this.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pin);
    }

    public boolean internetConnectionCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}