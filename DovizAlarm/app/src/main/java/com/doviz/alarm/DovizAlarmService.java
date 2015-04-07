package com.doviz.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

    public static long LOOP_TIME = 60000;

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
                if (shrp.getKaynak().equals("1"))
                    new ParseUrlAsyncTaskInr().execute(new String[]{"http://kur.doviz.com/serbest-piyasa/"});
                else
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

    private String addZero(String str) {

        String tmp = str.replace(".", "");
        int leng = tmp.length();

        if (leng < 5) {
            leng = 5 - leng;
            for (int i = 0; i < leng; i++) {
                tmp = tmp + "0";
            }
        } else {
            tmp = tmp.substring(0, 5);
        }
        int len = tmp.length();
        Character[] array = new Character[len];
        for (int i = 0; i < len; i++) {
            array[i] = new Character(tmp.charAt(i));
        }

        String ret = array[0].toString() + ".";
        for (int i = 1; i < array.length; i++) {
            ret = ret + array[i].toString();
        }

        return ret;
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

    private class ParseUrlAsyncTaskInr extends AsyncTask<String, Void, String> {
        String dolarAl, dolarSat;
        String euroAl, euroSat;
        //    String altinAl, altinSat;

        @Override
        protected String doInBackground(String... strings) {
            StringBuffer buffer = new StringBuffer();
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                /**
                 * Dolar-Euro
                 * */
                Elements divs = doc.select("div");

                for (Element div : divs) {
                    if (div.attr("class").equals("cevirici-select")) {
                        Elements lis = div.select("ul").select("li").select("ul");
                        for (Element li : lis) {

                            if (li.attr("data-code").equals("USD")) {
                                dolarAl = li.attr("data-buying");
                                buffer.append(dolarAl + ":");

                                dolarSat = li.attr("data-selling");
                                buffer.append(dolarSat + ":");
                            } else if (li.attr("data-code").equals("EUR")) {
                                euroAl = li.attr("data-buying");
                                buffer.append(euroAl + ":");

                                euroSat = li.attr("data-selling");
                                buffer.append(euroSat);
                            }
                        }
                    }
                }
//            Document docAltn = Jsoup.connect("http://altin.doviz.com/gram-altin").get();
                /**
                 * Altın
                 * */
//            Elements divAs = docAltn.select("div");
//            for (Element div : divAs) {
//                Log.i("**altin div ***", div.text());
//                if (div.attr("class").equals("doviz-column btgold")) {
//                    Elements lis = div.select("ul").select("li");
//
//                    for (Element li : lis) {
//                        Log.i("**altin ***", li.select("div").text());
//                        if (li.select("div").select("h1").text().equals("Gram Altın")) {
//                            Elements dvs = li.select("div");
//                            altinAl = dvs.get(4).text();
//                            buffer.append(altinAl + ":");
//                            Log.i("**altin al***", altinAl);
//
//                            altinSat = dvs.get(5).text();
//                            buffer.append(altinSat + "-");
//                            Log.i("**altin sat***", altinSat);
//                        }
//                    }
//                }
//            }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return buffer.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String[] deger = s.split(":");
            if (deger.length == 4) {
                notficationMessage = "";
                alarmControl(addZero(deger[0]), shrp.DOLAR_ALIS, true);
                alarmControl(addZero(deger[1]), shrp.DOLAR_SATIS, false);
                alarmControl(addZero(deger[2]), shrp.EURO_ALIS, true);
                alarmControl(addZero(deger[3]), shrp.EURO_SATIS, false);
                if (!notficationMessage.equals(""))
                    callAlarmManager(notficationMessage);
            }
        }
    }
}