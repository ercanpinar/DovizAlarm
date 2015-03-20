package com.doviz.alarm;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.doviz.alarm.response.DovizResponse;
import com.gc.materialdesign.views.LayoutRipple;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ercanpinar on 3/14/15.
 */
public class MainFragment extends BaseFragment {
    String DOVIZ_URL = "http://www.doviz.gen.tr/doviz_json.asp?version=1.2";
    private RequestQueue rq;
    JsonObjectRequest jReq;
    //_______
    @InjectView(R.id.tv_dolar_alis)
    TextView mTvDolarAlis;
    @InjectView(R.id.tv_dolar_satis)
    TextView mTvDolarSatis;
    @InjectView(R.id.tv_euro_alis)
    TextView mTvEuroAlis;
    @InjectView(R.id.tv_euro_satis)
    TextView mTvEuroSatis;
    @InjectView(R.id.tv_altin_alis)
    TextView mTvAltinAlis;
    @InjectView(R.id.tv_altin_satis)
    TextView mTvAltinSatis;
    @InjectView(R.id.tv_guncelleme)
    TextView mTvGuncelleme;
    @InjectView(R.id.tv_dolar_alis_alarm_degeri)
    TextView mTvDolarAlisalarm;
    @InjectView(R.id.tv_dolar_satis_alarm_degeri)
    TextView mTvDolarSatisalarm;
    @InjectView(R.id.tv_euro_alis_alarm_degeri)
    TextView mTvEuroAlisalarm;
    @InjectView(R.id.tv_euro_satis_alarm_degeri)
    TextView mTvEuroSatisalarm;
    @InjectView(R.id.chck_alarm)
    CheckBox mChckAlarm;
    @InjectView(R.id.chck_alarm_time_five_min)
    CheckBox mChckAlarm5dk;
    @InjectView(R.id.chck_alarm_time_fifty_min)
    CheckBox mChckAlarm15dk;
    @InjectView(R.id.chck_alarm_time_thirty_min)
    CheckBox mChckAlarm30dk;
    @InjectView(R.id.chck_alarm_time_one_hour)
    CheckBox mChckAlarm60dk;
    MyTimerTask yourTask;
    Timer t;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        rq = Volley.newRequestQueue(getActivity());
        refreshRequest();
        yourTask = new MyTimerTask();
        t = new Timer();
        t.scheduleAtFixedRate(yourTask, 0, 30000);
        SharedPref shrpT = new SharedPref(getActivity());

        mTvDolarAlisalarm.setText(shrpT.getAlarmValue(shrpT.DOLAR_ALIS) + " TL");
        mTvDolarSatisalarm.setText(shrpT.getAlarmValue(shrpT.DOLAR_SATIS) + " TL");
        mTvEuroAlisalarm.setText(shrpT.getAlarmValue(shrpT.EURO_ALIS) + " TL");
        mTvEuroSatisalarm.setText(shrpT.getAlarmValue(shrpT.EURO_SATIS) + " TL");

        if (servisCalisiyorMu())
            mChckAlarm.setChecked(true);
        else
            mChckAlarm.setChecked(false);

        mChckAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mChckAlarm5dk.isChecked() || mChckAlarm15dk.isChecked() || mChckAlarm30dk.isChecked() || mChckAlarm60dk.isChecked())
                        serviceStart();
                    else
                        mChckAlarm.setChecked(false);
                } else {
                    serviceStop();
                }
            }
        });
        //1sn=1000ms suanda 5dk=300sn icin 3sn olacak /100 seklinde olacak.
        if (DovizAlarmService.LOOP_TIME == 300000) {
            mChckAlarm5dk.setChecked(true);
        } else if (DovizAlarmService.LOOP_TIME == 900000) {
            mChckAlarm15dk.setChecked(true);
        } else if (DovizAlarmService.LOOP_TIME == 3600000) {
            mChckAlarm60dk.setChecked(true);
        } else {
            mChckAlarm30dk.setChecked(true);
        }
        mChckAlarm5dk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DovizAlarmService.LOOP_TIME = 300000;
                    mChckAlarm15dk.setChecked(false);
                    mChckAlarm30dk.setChecked(false);
                    mChckAlarm60dk.setChecked(false);
                } else {
                    mChckAlarm.setChecked(false);
                }
            }
        });
        mChckAlarm15dk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DovizAlarmService.LOOP_TIME = 900000;
                    mChckAlarm5dk.setChecked(false);
                    mChckAlarm30dk.setChecked(false);
                    mChckAlarm60dk.setChecked(false);
                } else {
                    mChckAlarm.setChecked(false);
                }
            }
        });
        mChckAlarm30dk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DovizAlarmService.LOOP_TIME = 1800000;
                    mChckAlarm5dk.setChecked(false);
                    mChckAlarm15dk.setChecked(false);
                    mChckAlarm60dk.setChecked(false);
                } else {
                    mChckAlarm.setChecked(false);
                }
            }
        });
        mChckAlarm60dk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DovizAlarmService.LOOP_TIME = 3600000;
                    mChckAlarm5dk.setChecked(false);
                    mChckAlarm15dk.setChecked(false);
                    mChckAlarm30dk.setChecked(false);
                } else {
                    mChckAlarm.setChecked(false);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        t.cancel();
        Crouton.cancelAllCroutons();
        ButterKnife.reset(this);
        this.rq.cancelAll(this);
        super.onDestroyView();
    }

    /**
     * OnClick
     */
    @OnClick(R.id.lr_alarm)
    public void alarmBtnClick(View view) {
        alarmDialog();
    }

    private void alarmDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_alarm);
        final EditText dolarAlisAlrm = (EditText) dialog.findViewById(R.id.edt_dolar_alis_alarm);
        final EditText dolarSatisAlrm = (EditText) dialog.findViewById(R.id.edt_dolar_satis_alarm);
        final EditText euroAlisAlrm = (EditText) dialog.findViewById(R.id.edt_euro_alis_alarm);
        final EditText euroSatisAlrm = (EditText) dialog.findViewById(R.id.edt_euro_satis_alarm);
        LayoutRipple dialogButton = (LayoutRipple) dialog.findViewById(R.id.lr_dialogButton);
        SharedPref shrpT = new SharedPref(getActivity());

        dolarAlisAlrm.setText(shrpT.getAlarmValue(shrpT.DOLAR_ALIS));
        dolarSatisAlrm.setText(shrpT.getAlarmValue(shrpT.DOLAR_SATIS));
        euroAlisAlrm.setText(shrpT.getAlarmValue(shrpT.EURO_ALIS));
        euroSatisAlrm.setText(shrpT.getAlarmValue(shrpT.EURO_SATIS));
//       // dolarAlisAlrm.setText(shrpT.getAlarmValue(shrpT.ALTIN_ALIS));
//        //dolarAlisAlrm.setText(shrpT.getAlarmValue(shrpT.ALTIN_SATIS));

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPref shrp = new SharedPref(getActivity());
                boolean serviceActv = false;
                if (stringIsNotEmptyAndZero(dolarAlisAlrm)) {
                    shrp.updateAlarm(shrp.DOLAR_ALIS, addZero(dolarAlisAlrm.getText().toString()));
                    if (!addZero(dolarAlisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }
                if (stringIsNotEmptyAndZero(dolarSatisAlrm)) {
                    shrp.updateAlarm(shrp.DOLAR_SATIS, addZero(dolarSatisAlrm.getText().toString()));
                    if (!addZero(dolarSatisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }
                if (stringIsNotEmptyAndZero(euroAlisAlrm)) {
                    shrp.updateAlarm(shrp.EURO_ALIS, addZero(euroAlisAlrm.getText().toString()));
                    if (!addZero(euroAlisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }
                if (stringIsNotEmptyAndZero(euroSatisAlrm)) {
                    shrp.updateAlarm(shrp.EURO_SATIS, addZero(euroSatisAlrm.getText().toString()));
                    if (!addZero(euroSatisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }

                if (serviceActv) {
                    serviceStart();
                    mChckAlarm.setChecked(true);
                } else {
                    mChckAlarm.setChecked(false);
                    serviceStop();
                }

                mTvDolarAlisalarm.setText(shrp.getAlarmValue(shrp.DOLAR_ALIS) + " TL");
                mTvDolarSatisalarm.setText(shrp.getAlarmValue(shrp.DOLAR_SATIS) + " TL");
                mTvEuroAlisalarm.setText(shrp.getAlarmValue(shrp.EURO_ALIS) + " TL");
                mTvEuroSatisalarm.setText(shrp.getAlarmValue(shrp.EURO_SATIS) + " TL");

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean stringIsNotEmptyAndZero(EditText editText) {

        if (!editText.getText().toString().trim().equals("") && !editText.getText().toString().trim().equals("0"))
            return true;
        else
            return false;
    }

    private String addZero(String str) {
        String tmp = str.replace(".", "");
        int leng = tmp.length();

        if (leng < 5) {
            leng = 5 - leng;
            for (int i = 0; i < leng; i++) {
                tmp = tmp + "0";
            }
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

    /**
     * Request
     */
    public void refreshRequest() {
        if (((MainActivity) getActivity()).internetConnectionCheck()) {
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
                                    mTvDolarAlis.setText(data.dolar + " TL");
                                    mTvDolarSatis.setText(data.dolar2 + " TL");
                                    mTvEuroAlis.setText(data.euro + " TL");
                                    mTvEuroSatis.setText(data.euro2 + " TL");
                                    mTvGuncelleme.setText(data.guncelleme);
                                } else {
                                    Crouton.cancelAllCroutons();
                                    Crouton.makeText(
                                            MainFragment.this.getActivity(), "Bir sorun oluştu.",
                                            Style.ALERT).show();
                                }
                            }
                        }
                    }

                    , new Response.ErrorListener()

            {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley hatasi ", error.toString());
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(
                            MainFragment.this.getActivity(), "Bir hata oluştu.",
                            Style.ALERT).show();
                }
            }
            );
            rq.add(jReq);
        } else {
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "İnternet bağlantınızı kontrol ediniz.",
                    Style.ALERT).show();
        }
    }

    private class MyTimerTask extends TimerTask {
        public void run() {
            refreshRequest();
        }
    }

    private void serviceStart() {
        if (!servisCalisiyorMu()) {
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm aktif edildi.",
                    Style.INFO).show();
            getActivity().startService(new Intent(getActivity(), DovizAlarmService.class));
        }
    }

    private boolean servisCalisiyorMu() {
        ActivityManager servisYoneticisi = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo servis : servisYoneticisi.getRunningServices(Integer.MAX_VALUE)) {
            if (getActivity().getPackageName().equals(servis.service.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Service start-stop
     */
    public void serviceStartStop() {
        if (servisCalisiyorMu()) {
            getActivity().stopService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm durduruldu.",
                    Style.INFO).show();
        } else

        {
            getActivity().startService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm aktif edildi.",
                    Style.INFO).show();
        }
    }

    public void serviceStop() {
        if (servisCalisiyorMu()) {
            getActivity().stopService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm durduruldu.",
                    Style.INFO).show();
        }
    }
}
